package com.example.hardware

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.FactoryBtPackages
import com.example.model.LarkBtProfiles
import com.example.model.PairedPhoneItem
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

/**
 * Hidden Underlying Native Android Engine for ASR lark_evb (arm64-v8a).
 * Operates behind the clean Apple-inspired UI and records diagnostic logs for every reflection call:
 * - Binds BluetoothProfile proxies (1, 2, 11, 12, 16, 17).
 * - Automatically blocks PBAP_CLIENT (17) so contact sync never freezes the switch.
 * - Kills stuck background Bluetooth/bridge tasks (com.ts.bt, com.asr.bt, com.zjinnova.zlink, etc.) + System.gc().
 * - Uses Java Reflection (disconnect / connect) for sequential profile handoff.
 */
@SuppressLint("MissingPermission")
class LarkHardwareManager(
    private val context: Context,
    private val onLog: (String) -> Unit = {},
    private val onHardwareStateChanged: () -> Unit
) {
    private val activityManager: ActivityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager

    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val profileProxies = ConcurrentHashMap<Int, BluetoothProfile>()
    private val aclConnectedAddresses = ConcurrentHashMap.newKeySet<String>()
    private var receiverRegistered = false

    private fun logEvent(msg: String) {
        Log.i("LarkBT", msg)
        onLog(msg)
    }

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            profileProxies[profile] = proxy
            logEvent("Proxy bound: profile=$profile class=${proxy.javaClass.simpleName}")
            if (profile == LarkBtProfiles.PBAP_CLIENT) {
                blockPbapClientOnAllDevices()
            }
            onHardwareStateChanged()
        }

        override fun onServiceDisconnected(profile: Int) {
            profileProxies.remove(profile)
            logEvent("Proxy unbound: profile=$profile")
            onHardwareStateChanged()
        }
    }

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
            }

            when (action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    logEvent("Broadcast ACTION_STATE_CHANGED: state=$state")
                    if (state == BluetoothAdapter.STATE_ON) {
                        bindAllProfileProxies()
                    }
                    onHardwareStateChanged()
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    device?.address?.let { aclConnectedAddresses.add(it) }
                    logEvent("Broadcast ACTION_ACL_CONNECTED: device=${device?.address}")
                    if (device != null) {
                        blockPbapClientForDevice(device)
                    }
                    onHardwareStateChanged()
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    device?.address?.let { aclConnectedAddresses.remove(it) }
                    logEvent("Broadcast ACTION_ACL_DISCONNECTED: device=${device?.address}")
                    onHardwareStateChanged()
                }

                BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED,
                "android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED",
                "android.bluetooth.pbapclient.profile.action.CONNECTION_STATE_CHANGED" -> {
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1)
                    val prevState = intent.getIntExtra(BluetoothProfile.EXTRA_PREVIOUS_STATE, -1)
                    logEvent("Broadcast ${action.substringAfterLast('.')}: device=${device?.address} $prevState->$state")
                    if (action.contains("pbap", ignoreCase = true) &&
                        (state == BluetoothProfile.STATE_CONNECTING || state == BluetoothProfile.STATE_CONNECTED) &&
                        device != null
                    ) {
                        blockPbapClientForDevice(device)
                    }
                    onHardwareStateChanged()
                }
            }
        }
    }

    fun initialize() {
        registerReceivers()
        bindAllProfileProxies()
    }

    fun release() {
        if (receiverRegistered) {
            runCatching { context.unregisterReceiver(bluetoothReceiver) }
            receiverRegistered = false
        }
        val adapter = bluetoothAdapter ?: return
        profileProxies.forEach { (id, proxy) ->
            runCatching { adapter.closeProfileProxy(id, proxy) }
        }
        profileProxies.clear()
    }

    private fun registerReceivers() {
        if (receiverRegistered) return
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction("android.bluetooth.headsetclient.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.a2dp-sink.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.avrcp-controller.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.pbap.profile.action.CONNECTION_STATE_CHANGED")
            addAction("android.bluetooth.pbapclient.profile.action.CONNECTION_STATE_CHANGED")
        }
        ContextCompat.registerReceiver(
            context,
            bluetoothReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
        receiverRegistered = true
        logEvent("Registered Bluetooth BroadcastReceiver")
    }

    fun bindAllProfileProxies() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            logEvent("bindAllProfileProxies: BluetoothAdapter is NULL (Web Preview / Emulator without BT hardware)")
            return
        }
        if (!hasBluetoothConnectPermission()) {
            logEvent("bindAllProfileProxies: BLUETOOTH_CONNECT permission not granted yet")
            return
        }

        for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
            if (!profileProxies.containsKey(profileId)) {
                try {
                    val requested = adapter.getProfileProxy(context, profileServiceListener, profileId)
                    logEvent("getProfileProxy(profile=$profileId) -> requested=$requested")
                } catch (e: Throwable) {
                    logEvent("getProfileProxy(profile=$profileId) -> exception=${e.javaClass.simpleName}: ${e.message}")
                }
            }
        }
    }

    fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun queryHardwareBondedPhones(): List<PairedPhoneItem> {
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!hasBluetoothConnectPermission()) return emptyList()

        return runCatching {
            val bonded = adapter.bondedDevices?.toList().orEmpty()
            val loadedTargetProxies = LarkBtProfiles.ACTIVE_CONNECT_CHECK_PROFILES.mapNotNull { profileId ->
                profileProxies[profileId]?.let { proxy -> profileId to proxy }
            }

            bonded.map { device ->
                val connected = if (loadedTargetProxies.isNotEmpty()) {
                    var anyProfileConnected = false
                    for ((profileId, proxy) in loadedTargetProxies) {
                        try {
                            val state = proxy.getConnectionState(device)
                            if (state == BluetoothProfile.STATE_CONNECTED) {
                                anyProfileConnected = true
                                break
                            }
                        } catch (e: Throwable) {
                            logEvent("getConnectionState(profile=$profileId, device=${device.address}) -> exception=${e.javaClass.simpleName}")
                        }
                    }
                    anyProfileConnected
                } else {
                    // Fallback ONLY while profile proxies (1, 2, 11, 12, 16) are not loaded yet
                    try {
                        val isConnectedMethod = device.javaClass.getMethod("isConnected")
                        val fallbackConnected = isConnectedMethod.invoke(device) as? Boolean == true
                        logEvent("Fallback BluetoothDevice.isConnected(${device.address}) [proxies not loaded yet] -> $fallbackConnected")
                        fallbackConnected
                    } catch (e: Throwable) {
                        logEvent("Fallback BluetoothDevice.isConnected(${device.address}) -> exception=${e.javaClass.simpleName}")
                        false
                    }
                }

                PairedPhoneItem(
                    id = device.address,
                    name = safeDeviceName(device),
                    isConnected = connected,
                    bluetoothDevice = device,
                    isHardwareOnlyPreviewPlaceholder = false
                )
            }.sortedByDescending { it.isConnected }
        }.getOrDefault(emptyList())
    }

    /**
     * Kills stuck background Bluetooth and bridge processes (com.ts.bt, com.asr.bt, com.zjinnova.zlink, etc.)
     * without touching com.android.bluetooth, and invokes System.gc() to prevent memory freezes.
     */
    fun freeSystemMemoryAndKillStuckBtTasks() {
        for (pkg in FactoryBtPackages.TARGET_PACKAGES) {
            try {
                activityManager.killBackgroundProcesses(pkg)
                logEvent("killBackgroundProcesses(\"$pkg\") -> dispatched")
            } catch (e: Throwable) {
                logEvent("killBackgroundProcesses(\"$pkg\") -> failed: ${e.javaClass.simpleName}")
            }
        }
        System.gc()
        Runtime.getRuntime().gc()
        System.runFinalization()
        logEvent("System.gc() completed")
    }

    /**
     * Uses Java Reflection to force-disconnect a specific BluetoothProfile proxy from a BluetoothDevice.
     */
    fun reflectDisconnectProfile(profileId: Int, device: BluetoothDevice?): Boolean {
        if (device == null) {
            logEvent("Reflect disconnect(profile=$profileId, device=null) -> false [No physical BluetoothDevice in preview]")
            return false
        }
        val proxy = profileProxies[profileId]
        if (proxy == null) {
            logEvent("Reflect disconnect(profile=$profileId, device=${device.address}) -> false [Proxy not bound]")
            return false
        }
        return try {
            val disconnectMethod: Method = proxy.javaClass.getMethod(
                "disconnect",
                BluetoothDevice::class.java
            )
            disconnectMethod.isAccessible = true
            val result = disconnectMethod.invoke(proxy, device) as? Boolean ?: false
            logEvent("Reflect ${proxy.javaClass.simpleName}.disconnect(profile=$profileId, device=${device.address}) -> $result")
            result
        } catch (e: Throwable) {
            logEvent("Reflect disconnect(profile=$profileId, device=${device.address}) -> exception=${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    /**
     * Uses Java Reflection to force-connect a specific BluetoothProfile proxy to a BluetoothDevice.
     */
    fun reflectConnectProfile(profileId: Int, device: BluetoothDevice?): Boolean {
        if (device == null) {
            logEvent("Reflect connect(profile=$profileId, device=null) -> false [No physical BluetoothDevice in preview]")
            return false
        }
        val proxy = profileProxies[profileId]
        if (proxy == null) {
            logEvent("Reflect connect(profile=$profileId, device=${device.address}) -> false [Proxy not bound]")
            return false
        }
        return try {
            val connectMethod: Method = proxy.javaClass.getMethod(
                "connect",
                BluetoothDevice::class.java
            )
            connectMethod.isAccessible = true
            val result = connectMethod.invoke(proxy, device) as? Boolean ?: false
            logEvent("Reflect ${proxy.javaClass.simpleName}.connect(profile=$profileId, device=${device.address}) -> $result")
            result
        } catch (e: Throwable) {
            logEvent("Reflect connect(profile=$profileId, device=${device.address}) -> exception=${e.javaClass.simpleName}: ${e.message}")
            false
        }
    }

    /**
     * Checks whether at least one of profiles 1, 2, 11, 12, or 16 reports STATE_CONNECTED (2)
     * for the given device. Returns Pair(isConnected, diagnosticBreakdown).
     */
    fun checkAnyTargetProfileConnected(device: BluetoothDevice?): Pair<Boolean, String> {
        if (device == null) {
            return false to "device=null (Preview placeholder - requires physical car Bluetooth hardware)"
        }
        if (!hasBluetoothConnectPermission()) {
            return false to "BLUETOOTH_CONNECT permission missing"
        }

        val stateParts = mutableListOf<String>()
        var anyConnected = false
        for (profileId in LarkBtProfiles.ACTIVE_CONNECT_CHECK_PROFILES) {
            val proxy = profileProxies[profileId]
            if (proxy == null) {
                stateParts.add("p$profileId=NO_PROXY")
            } else {
                try {
                    val state = proxy.getConnectionState(device)
                    stateParts.add("p$profileId=state($state)")
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        anyConnected = true
                    }
                } catch (e: Throwable) {
                    stateParts.add("p$profileId=ERR(${e.javaClass.simpleName})")
                }
            }
        }
        return anyConnected to stateParts.joinToString(", ")
    }

    /**
     * Force-disconnects all profiles (1, 2, 11, 12, 16, 17) on the specified device.
     */
    fun forceDisconnectAllProfiles(device: BluetoothDevice?) {
        for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
            reflectDisconnectProfile(profileId, device)
        }
        if (hasBluetoothConnectPermission()) {
            for ((profileId, proxy) in profileProxies) {
                runCatching {
                    proxy.connectedDevices.orEmpty().forEach { activeDev ->
                        reflectDisconnectProfile(profileId, activeDev)
                    }
                }
            }
        }
    }

    /**
     * Blocks PBAP_CLIENT (17 - contact sync) so it never freezes the 800MB RAM during a phone switch.
     */
    fun blockPbapClientForDevice(device: BluetoothDevice?) {
        if (device == null) {
            logEvent("PBAP Guard (profile=17, device=null) -> skipped [Preview mode]")
            return
        }
        val proxy = profileProxies[LarkBtProfiles.PBAP_CLIENT]
        if (proxy == null) {
            logEvent("PBAP Guard (profile=17, device=${device.address}) -> proxy not bound")
            return
        }
        runCatching {
            runCatching {
                val setPolicy = proxy.javaClass.getMethod(
                    "setConnectionPolicy",
                    BluetoothDevice::class.java,
                    Int::class.javaPrimitiveType
                )
                val policyRes = setPolicy.invoke(proxy, device, 0) // CONNECTION_POLICY_FORBIDDEN
                logEvent("Reflect ${proxy.javaClass.simpleName}.setConnectionPolicy(${device.address}, 0) -> $policyRes")
            }.onFailure {
                runCatching {
                    val setPriority = proxy.javaClass.getMethod(
                        "setPriority",
                        BluetoothDevice::class.java,
                        Int::class.javaPrimitiveType
                    )
                    val prioRes = setPriority.invoke(proxy, device, 0) // PRIORITY_OFF
                    logEvent("Reflect ${proxy.javaClass.simpleName}.setPriority(${device.address}, 0) -> $prioRes")
                }
            }
            reflectDisconnectProfile(LarkBtProfiles.PBAP_CLIENT, device)
        }
    }

    private fun blockPbapClientOnAllDevices() {
        if (!hasBluetoothConnectPermission()) return
        val proxy = profileProxies[LarkBtProfiles.PBAP_CLIENT] ?: return
        runCatching {
            proxy.connectedDevices.orEmpty().forEach { device ->
                blockPbapClientForDevice(device)
            }
        }
    }

    @Suppress("DEPRECATION")
    fun disableBluetoothAdapter(): Boolean {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            logEvent("BluetoothAdapter.disable() -> false [BluetoothAdapter is NULL on this host]")
            return false
        }
        return try {
            val result = adapter.disable()
            logEvent("BluetoothAdapter.disable() -> returned $result")
            result
        } catch (se: SecurityException) {
            logEvent("BluetoothAdapter.disable() -> blocked by SecurityException: ${se.message}")
            false
        } catch (t: Throwable) {
            logEvent("BluetoothAdapter.disable() -> exception ${t.javaClass.simpleName}: ${t.message}")
            false
        }
    }

    @Suppress("DEPRECATION")
    fun enableBluetoothAdapter(): Boolean {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            logEvent("BluetoothAdapter.enable() -> false [BluetoothAdapter is NULL on this host]")
            return false
        }
        return try {
            val result = adapter.enable()
            logEvent("BluetoothAdapter.enable() -> returned $result")
            result
        } catch (se: SecurityException) {
            logEvent("BluetoothAdapter.enable() -> blocked by SecurityException: ${se.message}")
            false
        } catch (t: Throwable) {
            logEvent("BluetoothAdapter.enable() -> exception ${t.javaClass.simpleName}: ${t.message}")
            false
        }
    }

    private fun safeDeviceName(device: BluetoothDevice): String {
        return runCatching {
            device.name?.takeIf { it.isNotBlank() } ?: "סמארטפון מותאם"
        }.getOrDefault("סמארטפון מותאם")
    }
}
