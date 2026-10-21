package com.example.export

data class ExportableProjectFile(
    val id: String,
    val title: String,
    val relativePath: String,
    val languageBadge: String,
    val description: String,
    val content: String
)

object NativeExportCatalog {
    val FILES: List<ExportableProjectFile> = listOf(
        ExportableProjectFile(
            id = "main_activity",
            title = "MainActivity.kt",
            relativePath = "app/src/main/java/com/example/MainActivity.kt",
            languageBadge = "Kotlin",
            description = "Main Activity & 1280x720 RTL Screen Host",
            content = """package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AppleTopHeader
import com.example.ui.components.ExportProjectModalDialog
import com.example.ui.components.MinimalSettingsModalSheet
import com.example.ui.components.PairedPhonesCenterStage
import com.example.ui.components.SmartSwitchPopupOverlay
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OledBlack
import com.example.viewmodel.LarkSwitcherViewModel
import com.example.viewmodel.LarkUiState

class MainActivity : ComponentActivity() {

    private val viewModel: LarkSwitcherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Request runtime Bluetooth permissions on Android 12+ (API 31+).
                // On Android 10 & 11, BLUETOOTH and BLUETOOTH_ADMIN are install-time permissions,
                // and KILL_BACKGROUND_PROCESSES is a normal permission on all versions.
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.onPermissionsResult()
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                            )
                        )
                    } else {
                        viewModel.onPermissionsResult()
                    }
                }

                BackHandler(enabled = uiState.isExportModalOpen || uiState.isSettingsSheetOpen) {
                    when {
                        uiState.isExportModalOpen -> viewModel.closeExportModal()
                        uiState.isSettingsSheetOpen -> viewModel.closeSettingsSheet()
                    }
                }

                // Enforce Hebrew RTL Layout Direction across the entire 1280x720 Car Head Unit UI
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    AppleBluetoothSwitcherScreen(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun AppleBluetoothSwitcherScreen(
    uiState: LarkUiState,
    viewModel: LarkSwitcherViewModel
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(OledBlack),
        containerColor = OledBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack)
                .padding(innerPadding)
                .safeDrawingPadding()
        ) {
            // 1. Minimal Top Header (RTL: Title + Status Pill on Right, 🔄 & ⚙️ on Left)
            AppleTopHeader(
                isAnyConnected = uiState.isAnyPhoneConnected,
                isRefreshing = uiState.isRefreshingList,
                onRefreshClick = viewModel::refreshPairedPhonesWithFeedback,
                onSettingsClick = viewModel::openSettingsSheet
            )

            // 2. Center Stage: Large, Side-by-Side Paired Phone Touch Cards
            PairedPhonesCenterStage(
                phones = uiState.pairedPhones,
                hiddenDeviceCount = uiState.hiddenDeviceCount,
                onConnectClick = viewModel::connectToPhoneSmartSwitch,
                onDisconnectClick = viewModel::disconnectPhone,
                onHideDeviceClick = viewModel::hideDevice,
                onRestoreHiddenDevicesClick = viewModel::restoreAllHiddenDevices,
                modifier = Modifier.weight(1f)
            )
        }

        // 3. Smooth "Smart Switch" Glassmorphic Popup Overlay (25% -> 60% -> 90% -> 100%)
        SmartSwitchPopupOverlay(
            state = uiState.smartSwitchModalState
        )

        // 4. Minimal Settings Sheet (Opened via ⚙️ Icon)
        MinimalSettingsModalSheet(
            isOpen = uiState.isSettingsSheetOpen,
            statusBannerHe = uiState.settingsStatusBannerHe,
            isRestartingBt = uiState.isRestartingBt,
            hiddenDeviceCount = uiState.hiddenDeviceCount,
            onDismiss = viewModel::closeSettingsSheet,
            onPairNewPhoneClick = { viewModel.openNativeBluetoothPairing(context) },
            onRestartBluetoothClick = viewModel::restartBluetoothAdapter,
            onRefreshSystemClick = viewModel::refreshSystemMemory,
            onRestoreHiddenDevicesClick = viewModel::restoreAllHiddenDevices,
            onOpenExportProjectClick = viewModel::openExportModal
        )

        // Discrete Export Android APK Project Modal (Opened from Settings Sheet)
        ExportProjectModalDialog(
            isOpen = uiState.isExportModalOpen,
            diagnosticLogs = uiState.diagnosticLogs,
            bluetoothStackDiagnostics = uiState.bluetoothStackDiagnostics,
            onDismiss = viewModel::closeExportModal
        )
    }
}"""
        ),
        ExportableProjectFile(
            id = "lark_models",
            title = "LarkModels.kt",
            relativePath = "app/src/main/java/com/example/model/LarkModels.kt",
            languageBadge = "Kotlin",
            description = "Bluetooth Profile Constants, Factory Packages & UI Models",
            content = """package com.example.model

import android.bluetooth.BluetoothDevice

/**
 * Standard and hidden Automotive BluetoothProfile IDs used on ASR lark_evb (arm64-v8a):
 * - HEADSET = 1, A2DP = 2
 * - A2DP_SINK = 11, AVRCP_CONTROLLER = 12, HEADSET_CLIENT = 16, PBAP_CLIENT = 17
 */
object LarkBtProfiles {
    const val HEADSET = 1
    const val A2DP = 2
    const val A2DP_SINK = 11
    const val AVRCP_CONTROLLER = 12
    const val HEADSET_CLIENT = 16
    const val PBAP_CLIENT = 17

    val ALL_MANAGED_PROFILES = listOf(
        HEADSET,
        A2DP,
        A2DP_SINK,
        AVRCP_CONTROLLER,
        HEADSET_CLIENT,
        PBAP_CLIENT
    )

    val ACTIVE_CONNECT_CHECK_PROFILES = listOf(
        HEADSET,
        A2DP,
        A2DP_SINK,
        AVRCP_CONTROLLER,
        HEADSET_CLIENT
    )

    val CALL_PROFILES = listOf(HEADSET_CLIENT, HEADSET)
    val MEDIA_PROFILES = listOf(A2DP_SINK, A2DP, AVRCP_CONTROLLER)
}

/**
 * Stuck background Bluetooth & CarPlay/AndroidAuto bridge processes on ASR lark_evb head units.
 * Note: "com.android.bluetooth" is intentionally excluded so we never kill the core Android BT daemon
 * or invalidate our own bound BluetoothProfile proxies.
 */
object FactoryBtPackages {
    val TARGET_PACKAGES = listOf(
        "com.ts.bt",
        "com.asr.bt",
        "com.zjinnova.zlink",
        "com.autochips.bluetooth",
        "com.syu.bt",
        "com.my.bt"
    )
}

data class PairedPhoneItem(
    val id: String,
    val name: String,
    val isConnected: Boolean,
    val bluetoothDevice: BluetoothDevice? = null,
    val isHardwareOnlyPreviewPlaceholder: Boolean = false
)

sealed class SmartSwitchModalState {
    data object Hidden : SmartSwitchModalState()

    data class Switching(
        val targetPhoneName: String,
        val step: Int, // 1, 2, 3, 4
        val progressPercent: Int, // 25, 60, 90, 100
        val statusTextHe: String,
        val isComplete: Boolean = false,
        val isFailed: Boolean = false
    ) : SmartSwitchModalState()
}"""
        ),
        ExportableProjectFile(
            id = "hardware_manager",
            title = "LarkHardwareManager.kt",
            relativePath = "app/src/main/java/com/example/hardware/LarkHardwareManager.kt",
            languageBadge = "Kotlin",
            description = "Reflection Engine, Profile Proxies, 250ms Polling & Bluetooth Stack Diagnostics",
            content = """package com.example.hardware

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.FactoryBtPackages
import com.example.model.LarkBtProfiles
import com.example.model.PairedPhoneItem
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import org.xmlpull.v1.XmlPullParser

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

    private val mainHandler = Handler(Looper.getMainLooper())
    private val diagTimeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private val proxyRequestStatus = ConcurrentHashMap<Int, String>()
    private val proxyCallbackStatus = ConcurrentHashMap<Int, String>()
    private val proxyCallbackFired = ConcurrentHashMap<Int, Boolean>()
    private val pendingTimeoutRunnables = ConcurrentHashMap<Int, Runnable>()
    private val serviceBindProbeStatus = ConcurrentHashMap<String, String>()
    private val pendingBindTimeoutRunnables = ConcurrentHashMap<String, Runnable>()

    private fun nowTime(): String = diagTimeFormat.format(Date())

    private fun logEvent(msg: String) {
        Log.i("LarkBT", msg)
        onLog(msg)
    }

    private val profileServiceListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            pendingTimeoutRunnables.remove(profile)?.let { mainHandler.removeCallbacks(it) }
            val ts = nowTime()
            proxyCallbackFired[profile] = true
            proxyCallbackStatus[profile] = "onServiceConnected at ${'$'}ts (class=${'$'}{proxy.javaClass.simpleName})"
            profileProxies[profile] = proxy
            logEvent("Proxy bound: profile=${'$'}profile class=${'$'}{proxy.javaClass.simpleName} (onServiceConnected at ${'$'}ts)")
            if (profile == LarkBtProfiles.PBAP_CLIENT) {
                blockPbapClientOnAllDevices()
            }
            onHardwareStateChanged()
        }

        override fun onServiceDisconnected(profile: Int) {
            pendingTimeoutRunnables.remove(profile)?.let { mainHandler.removeCallbacks(it) }
            val ts = nowTime()
            proxyCallbackFired[profile] = true
            proxyCallbackStatus[profile] = "onServiceDisconnected at ${'$'}ts"
            profileProxies.remove(profile)
            logEvent("Proxy unbound: profile=${'$'}profile (onServiceDisconnected at ${'$'}ts)")
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
                    logEvent("Broadcast ACTION_STATE_CHANGED: state=${'$'}state")
                    if (state == BluetoothAdapter.STATE_ON) {
                        bindAllProfileProxies()
                    }
                    onHardwareStateChanged()
                }

                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    device?.address?.let { aclConnectedAddresses.add(it) }
                    logEvent("Broadcast ACTION_ACL_CONNECTED: device=${'$'}{device?.address}")
                    if (device != null) {
                        blockPbapClientForDevice(device)
                    }
                    onHardwareStateChanged()
                }

                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    device?.address?.let { aclConnectedAddresses.remove(it) }
                    logEvent("Broadcast ACTION_ACL_DISCONNECTED: device=${'$'}{device?.address}")
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
                    logEvent("Broadcast ${'$'}{action.substringAfterLast('.')}: device=${'$'}{device?.address} ${'$'}prevState->${'$'}state")
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
        logBluetoothStackDiagnostics("startup")
    }

    fun release() {
        pendingTimeoutRunnables.values.forEach { mainHandler.removeCallbacks(it) }
        pendingTimeoutRunnables.clear()
        pendingBindTimeoutRunnables.values.forEach { mainHandler.removeCallbacks(it) }
        pendingBindTimeoutRunnables.clear()
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

    private fun scheduleTenSecondCallbackCheck(profileId: Int) {
        pendingTimeoutRunnables.remove(profileId)?.let { mainHandler.removeCallbacks(it) }
        val runnable = Runnable {
            pendingTimeoutRunnables.remove(profileId)
            if (proxyCallbackFired[profileId] != true) {
                val ts = nowTime()
                proxyCallbackStatus[profileId] = "NO CALLBACK after 10s (at ${'$'}ts)"
                logEvent("[Diag #3] Profile ${'$'}profileId: getProfileProxy=(${'$'}{proxyRequestStatus[profileId] ?: "not called"}) -> NO CALLBACK after 10s (at ${'$'}ts)")
                onHardwareStateChanged()
            }
        }
        pendingTimeoutRunnables[profileId] = runnable
        mainHandler.postDelayed(runnable, 10_000L)
    }

    fun bindAllProfileProxies() {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            val ts = nowTime()
            logEvent("bindAllProfileProxies: BluetoothAdapter is NULL (Web Preview / Emulator without BT hardware)")
            for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
                proxyCallbackFired.putIfAbsent(profileId, false)
                proxyRequestStatus[profileId] = "NOT_CALLED (BluetoothAdapter is NULL at ${'$'}ts)"
                if (!proxyCallbackStatus.containsKey(profileId)) {
                    proxyCallbackStatus[profileId] = "waiting 10s (started at ${'$'}ts)..."
                }
                scheduleTenSecondCallbackCheck(profileId)
            }
            return
        }
        if (!hasBluetoothConnectPermission()) {
            val ts = nowTime()
            logEvent("bindAllProfileProxies: BLUETOOTH_CONNECT permission not granted yet")
            for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
                proxyCallbackFired.putIfAbsent(profileId, false)
                proxyRequestStatus[profileId] = "SKIPPED (BLUETOOTH_CONNECT denied at ${'$'}ts)"
                if (!proxyCallbackStatus.containsKey(profileId)) {
                    proxyCallbackStatus[profileId] = "waiting 10s (started at ${'$'}ts)..."
                }
                scheduleTenSecondCallbackCheck(profileId)
            }
            return
        }

        for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
            if (!profileProxies.containsKey(profileId)) {
                val ts = nowTime()
                proxyCallbackFired[profileId] = false
                try {
                    val requested = adapter.getProfileProxy(context, profileServiceListener, profileId)
                    proxyRequestStatus[profileId] = "returned ${'$'}requested at ${'$'}ts"
                    proxyCallbackStatus[profileId] = "waiting for callback (10s timer started at ${'$'}ts)..."
                    logEvent("getProfileProxy(profile=${'$'}profileId) -> requested=${'$'}requested (at ${'$'}ts)")
                    scheduleTenSecondCallbackCheck(profileId)
                } catch (e: Throwable) {
                    proxyRequestStatus[profileId] = "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message}) at ${'$'}ts"
                    proxyCallbackStatus[profileId] = "waiting for callback (10s timer started at ${'$'}ts)..."
                    logEvent("getProfileProxy(profile=${'$'}profileId) -> exception=${'$'}{e.javaClass.simpleName}: ${'$'}{e.message}")
                    scheduleTenSecondCallbackCheck(profileId)
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
                            logEvent("getConnectionState(profile=${'$'}profileId, device=${'$'}{device.address}) -> exception=${'$'}{e.javaClass.simpleName}")
                        }
                    }
                    anyProfileConnected
                } else {
                    // Fallback ONLY while profile proxies (1, 2, 11, 12, 16) are not loaded yet
                    try {
                        val isConnectedMethod = device.javaClass.getMethod("isConnected")
                        val fallbackConnected = isConnectedMethod.invoke(device) as? Boolean == true
                        logEvent("Fallback BluetoothDevice.isConnected(${'$'}{device.address}) [proxies not loaded yet] -> ${'$'}fallbackConnected")
                        fallbackConnected
                    } catch (e: Throwable) {
                        logEvent("Fallback BluetoothDevice.isConnected(${'$'}{device.address}) -> exception=${'$'}{e.javaClass.simpleName}")
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
                logEvent("killBackgroundProcesses(\"${'$'}pkg\") -> dispatched")
            } catch (e: Throwable) {
                logEvent("killBackgroundProcesses(\"${'$'}pkg\") -> failed: ${'$'}{e.javaClass.simpleName}")
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
            logEvent("Reflect disconnect(profile=${'$'}profileId, device=null) -> false [No physical BluetoothDevice in preview]")
            return false
        }
        val proxy = profileProxies[profileId]
        if (proxy == null) {
            logEvent("Reflect disconnect(profile=${'$'}profileId, device=${'$'}{device.address}) -> false [Proxy not bound]")
            return false
        }
        return try {
            val disconnectMethod: Method = proxy.javaClass.getMethod(
                "disconnect",
                BluetoothDevice::class.java
            )
            disconnectMethod.isAccessible = true
            val result = disconnectMethod.invoke(proxy, device) as? Boolean ?: false
            logEvent("Reflect ${'$'}{proxy.javaClass.simpleName}.disconnect(profile=${'$'}profileId, device=${'$'}{device.address}) -> ${'$'}result")
            result
        } catch (e: Throwable) {
            logEvent("Reflect disconnect(profile=${'$'}profileId, device=${'$'}{device.address}) -> exception=${'$'}{e.javaClass.simpleName}: ${'$'}{e.message}")
            false
        }
    }

    /**
     * Uses Java Reflection to force-connect a specific BluetoothProfile proxy to a BluetoothDevice.
     */
    fun reflectConnectProfile(profileId: Int, device: BluetoothDevice?): Boolean {
        if (device == null) {
            logEvent("Reflect connect(profile=${'$'}profileId, device=null) -> false [No physical BluetoothDevice in preview]")
            return false
        }
        val proxy = profileProxies[profileId]
        if (proxy == null) {
            logEvent("Reflect connect(profile=${'$'}profileId, device=${'$'}{device.address}) -> false [Proxy not bound]")
            return false
        }
        return try {
            val connectMethod: Method = proxy.javaClass.getMethod(
                "connect",
                BluetoothDevice::class.java
            )
            connectMethod.isAccessible = true
            val result = connectMethod.invoke(proxy, device) as? Boolean ?: false
            logEvent("Reflect ${'$'}{proxy.javaClass.simpleName}.connect(profile=${'$'}profileId, device=${'$'}{device.address}) -> ${'$'}result")
            result
        } catch (e: Throwable) {
            logEvent("Reflect connect(profile=${'$'}profileId, device=${'$'}{device.address}) -> exception=${'$'}{e.javaClass.simpleName}: ${'$'}{e.message}")
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
                stateParts.add("p${'$'}profileId=NO_PROXY")
            } else {
                try {
                    val state = proxy.getConnectionState(device)
                    stateParts.add("p${'$'}profileId=state(${'$'}state)")
                    if (state == BluetoothProfile.STATE_CONNECTED) {
                        anyConnected = true
                    }
                } catch (e: Throwable) {
                    stateParts.add("p${'$'}profileId=ERR(${'$'}{e.javaClass.simpleName})")
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
            logEvent("PBAP Guard (profile=17, device=${'$'}{device.address}) -> proxy not bound")
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
                logEvent("Reflect ${'$'}{proxy.javaClass.simpleName}.setConnectionPolicy(${'$'}{device.address}, 0) -> ${'$'}policyRes")
            }.onFailure {
                runCatching {
                    val setPriority = proxy.javaClass.getMethod(
                        "setPriority",
                        BluetoothDevice::class.java,
                        Int::class.javaPrimitiveType
                    )
                    val prioRes = setPriority.invoke(proxy, device, 0) // PRIORITY_OFF
                    logEvent("Reflect ${'$'}{proxy.javaClass.simpleName}.setPriority(${'$'}{device.address}, 0) -> ${'$'}prioRes")
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
            logEvent("BluetoothAdapter.disable() -> returned ${'$'}result")
            result
        } catch (se: SecurityException) {
            logEvent("BluetoothAdapter.disable() -> blocked by SecurityException: ${'$'}{se.message}")
            false
        } catch (t: Throwable) {
            logEvent("BluetoothAdapter.disable() -> exception ${'$'}{t.javaClass.simpleName}: ${'$'}{t.message}")
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
            logEvent("BluetoothAdapter.enable() -> returned ${'$'}result")
            result
        } catch (se: SecurityException) {
            logEvent("BluetoothAdapter.enable() -> blocked by SecurityException: ${'$'}{se.message}")
            false
        } catch (t: Throwable) {
            logEvent("BluetoothAdapter.enable() -> exception ${'$'}{t.javaClass.simpleName}: ${'$'}{t.message}")
            false
        }
    }

    private fun safeDeviceName(device: BluetoothDevice): String {
        return runCatching {
            device.name?.takeIf { it.isNotBlank() } ?: "סמארטפון מותאם"
        }.getOrDefault("סמארטפון מותאם")
    }

    /**
     * Builds the read-only 6-point Bluetooth stack diagnostics snapshot with real values or real exceptions.
     */
    fun buildBluetoothStackDiagnosticsSnapshot(): List<String> {
        val lines = mutableListOf<String>()

        // 1. Device identity + ro.build.version.sdk via reflection on android.os.SystemProperties
        val roBuildVersionSdk = try {
            val spClass = Class.forName("android.os.SystemProperties")
            val getMethod = spClass.getMethod("get", String::class.java)
            val value = getMethod.invoke(null, "ro.build.version.sdk") as? String
            value ?: "null"
        } catch (e: Throwable) {
            "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
        }
        lines.add(
            "[1. Device Identity] MANUFACTURER=${'$'}{Build.MANUFACTURER} | MODEL=${'$'}{Build.MODEL} | DEVICE=${'$'}{Build.DEVICE} | HARDWARE=${'$'}{Build.HARDWARE} | BOARD=${'$'}{Build.BOARD}"
        )
        lines.add(
            "[1. Device Identity] VERSION.RELEASE=${'$'}{Build.VERSION.RELEASE} | VERSION.SDK_INT=${'$'}{Build.VERSION.SDK_INT} vs SystemProperties(ro.build.version.sdk)=${'$'}roBuildVersionSdk | FINGERPRINT=${'$'}{Build.FINGERPRINT}"
        )

        // 2. BluetoothAdapter.getDefaultAdapter(): null or not, isEnabled, state, name
        @Suppress("DEPRECATION")
        val defaultAdapterResult = try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            if (adapter == null) {
                "defaultAdapter=NULL | isEnabled=N/A | state=N/A | name=N/A"
            } else {
                val isEnabledStr = try {
                    adapter.isEnabled.toString()
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                }
                val stateStr = try {
                    adapter.state.toString()
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                }
                val nameStr = try {
                    adapter.name ?: "null"
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                }
                "defaultAdapter=NOT_NULL | isEnabled=${'$'}isEnabledStr | state=${'$'}stateStr | name=\"${'$'}nameStr\""
            }
        } catch (e: Throwable) {
            "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
        }
        lines.add("[2. BluetoothAdapter.getDefaultAdapter()] ${'$'}defaultAdapterResult")

        // 3. Profiles 1, 2, 11, 12, 16, 17: getProfileProxy result + callback within 10s (or NO CALLBACK after 10s)
        for (profileId in LarkBtProfiles.ALL_MANAGED_PROFILES) {
            val req = proxyRequestStatus[profileId] ?: "not called yet"
            val cb = proxyCallbackStatus[profileId] ?: "no callback yet"
            lines.add("[3. Profile ${'$'}profileId] getProfileProxy=(${'$'}req) | callback=(${'$'}cb)")
        }

        // 4. PackageManager: installed packages containing bluetooth, bt, blue, lark, asr, zlink, syu
        val keywords = listOf("bluetooth", "bt", "blue", "lark", "asr", "zlink", "syu")
        try {
            val pm = context.packageManager
            @Suppress("DEPRECATION")
            val installed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
            } else {
                pm.getInstalledPackages(0)
            }
            val matched = installed.filter { pkgInfo ->
                val lowerName = pkgInfo.packageName.lowercase(Locale.US)
                keywords.any { kw -> lowerName.contains(kw) }
            }.sortedBy { it.packageName }

            if (matched.isEmpty()) {
                lines.add("[4. PackageManager] 0 matching packages found (scanned ${'$'}{installed.size} visible packages)")
            } else {
                lines.add("[4. PackageManager] Found ${'$'}{matched.size} matching packages (scanned ${'$'}{installed.size} visible packages):")
                for (pkgInfo in matched) {
                    val flags = pkgInfo.applicationInfo?.flags ?: 0
                    val isSystem = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    val isUpdatedSystem = (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    val verName = pkgInfo.versionName ?: "null"
                    lines.add(
                        "[4. PackageManager] package=${'$'}{pkgInfo.packageName} | versionName=${'$'}verName | isSystemApp=${'$'}isSystem (updatedSystem=${'$'}isUpdatedSystem, flags=0x${'$'}{Integer.toHexString(flags)})"
                    )
                }
            }
        } catch (e: Throwable) {
            lines.add("[4. PackageManager] EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})")
        }

        // 5. adapter.getBondedDevices(): name, address, device type
        @Suppress("DEPRECATION")
        val adapterForBonded = try {
            BluetoothAdapter.getDefaultAdapter() ?: bluetoothAdapter
        } catch (_: Throwable) {
            bluetoothAdapter
        }
        if (adapterForBonded == null) {
            lines.add("[5. adapter.getBondedDevices()] adapter=NULL")
        } else {
            try {
                val bonded = adapterForBonded.bondedDevices
                if (bonded == null) {
                    lines.add("[5. adapter.getBondedDevices()] returned NULL")
                } else if (bonded.isEmpty()) {
                    lines.add("[5. adapter.getBondedDevices()] returned EMPTY set (0 devices)")
                } else {
                    lines.add("[5. adapter.getBondedDevices()] count=${'$'}{bonded.size}:")
                    for (device in bonded) {
                        val devName = try {
                            device.name ?: "null"
                        } catch (e: Throwable) {
                            "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                        }
                        val devAddress = try {
                            device.address ?: "null"
                        } catch (e: Throwable) {
                            "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                        }
                        val devType = try {
                            when (val t = device.type) {
                                BluetoothDevice.DEVICE_TYPE_CLASSIC -> "DEVICE_TYPE_CLASSIC(1)"
                                BluetoothDevice.DEVICE_TYPE_LE -> "DEVICE_TYPE_LE(2)"
                                BluetoothDevice.DEVICE_TYPE_DUAL -> "DEVICE_TYPE_DUAL(3)"
                                BluetoothDevice.DEVICE_TYPE_UNKNOWN -> "DEVICE_TYPE_UNKNOWN(0)"
                                else -> "TYPE(${'$'}t)"
                            }
                        } catch (e: Throwable) {
                            "EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})"
                        }
                        lines.add("[5. adapter.getBondedDevices()] name=\"${'$'}devName\" | address=${'$'}devAddress | deviceType=${'$'}devType")
                    }
                }
            } catch (e: Throwable) {
                lines.add("[5. adapter.getBondedDevices()] EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})")
            }
        }

        // 6. Whether this app runs as a system app (ApplicationInfo.flags & FLAG_SYSTEM) and runtime permissions granted
        try {
            val appInfo = context.applicationInfo
            val flags = appInfo.flags
            val isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val isUpdatedSystemApp = (flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            lines.add(
                "[6. App System Flag] package=${'$'}{context.packageName} | (ApplicationInfo.flags & FLAG_SYSTEM)!=0 -> isSystemApp=${'$'}isSystemApp | isUpdatedSystemApp=${'$'}isUpdatedSystemApp | flags=0x${'$'}{Integer.toHexString(flags)}"
            )
        } catch (e: Throwable) {
            lines.add("[6. App System Flag] EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})")
        }

        try {
            val permsToCheck = listOf(
                android.Manifest.permission.BLUETOOTH,
                android.Manifest.permission.BLUETOOTH_ADMIN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.KILL_BACKGROUND_PROCESSES,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val permReport = permsToCheck.joinToString(" | ") { perm ->
                val status = try {
                    if (ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED) {
                        "GRANTED"
                    } else {
                        "DENIED"
                    }
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.simpleName})"
                }
                "${'$'}{perm.substringAfterLast('.')}=${'$'}status"
            }
            lines.add("[6. Granted Permissions] ${'$'}permReport")
        } catch (e: Throwable) {
            lines.add("[6. Granted Permissions] EXCEPTION(${'$'}{e.javaClass.simpleName}: ${'$'}{e.message})")
        }

        // 7. Vendor Bluetooth packages
        val vendorBtPackages = listOf(
            "com.android.bluetooth",
            "com.bt.bc03",
            "com.zjinnova.zlink",
            "com.asrmicro.boltboost",
            "vendor.asr.wcn"
        )
        val discoveredManifestBtActions = linkedSetOf<String>()
        val pm = context.packageManager

        lines.add("[7. Vendor Bluetooth packages] --- Target packages: ${'$'}{vendorBtPackages.joinToString(", ")} ---")
        for (pkg in vendorBtPackages) {
            try {
                @Suppress("DEPRECATION")
                val compFlags = PackageManager.GET_SERVICES or
                    PackageManager.GET_RECEIVERS or
                    PackageManager.GET_ACTIVITIES or
                    PackageManager.GET_PROVIDERS
                @Suppress("DEPRECATION")
                val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(compFlags.toLong()))
                } else {
                    pm.getPackageInfo(pkg, compFlags)
                }

                val appEnabledSetting = try {
                    pm.getApplicationEnabledSetting(pkg).toString()
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                }
                val appInfoEnabled = try {
                    pkgInfo.applicationInfo?.enabled?.toString() ?: "null"
                } catch (e: Throwable) {
                    "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                }
                lines.add(
                    "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | getApplicationEnabledSetting=${'$'}appEnabledSetting | ApplicationInfo.enabled=${'$'}appInfoEnabled | versionName=${'$'}{pkgInfo.versionName ?: "null"}"
                )

                val services = pkgInfo.services.orEmpty()
                lines.add("[7. Vendor Bluetooth packages] pkg=${'$'}pkg | services count=${'$'}{services.size}")
                for (svc in services) {
                    val compEnabled = try {
                        pm.getComponentEnabledSetting(ComponentName(pkg, svc.name)).toString()
                    } catch (e: Throwable) {
                        "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    }
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | SERVICE class=${'$'}{svc.name} | exported=${'$'}{svc.exported} | getComponentEnabledSetting=${'$'}compEnabled (info.enabled=${'$'}{svc.enabled}) | permission=${'$'}{svc.permission ?: "none"}"
                    )
                }

                val receivers = pkgInfo.receivers.orEmpty()
                lines.add("[7. Vendor Bluetooth packages] pkg=${'$'}pkg | receivers count=${'$'}{receivers.size}")
                for (rcv in receivers) {
                    val compEnabled = try {
                        pm.getComponentEnabledSetting(ComponentName(pkg, rcv.name)).toString()
                    } catch (e: Throwable) {
                        "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    }
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | RECEIVER class=${'$'}{rcv.name} | exported=${'$'}{rcv.exported} | getComponentEnabledSetting=${'$'}compEnabled (info.enabled=${'$'}{rcv.enabled}) | permission=${'$'}{rcv.permission ?: "none"}"
                    )
                }

                val activities = pkgInfo.activities.orEmpty()
                lines.add("[7. Vendor Bluetooth packages] pkg=${'$'}pkg | activities count=${'$'}{activities.size}")
                for (act in activities) {
                    val compEnabled = try {
                        pm.getComponentEnabledSetting(ComponentName(pkg, act.name)).toString()
                    } catch (e: Throwable) {
                        "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    }
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | ACTIVITY class=${'$'}{act.name} | exported=${'$'}{act.exported} | getComponentEnabledSetting=${'$'}compEnabled (info.enabled=${'$'}{act.enabled}) | permission=${'$'}{act.permission ?: "none"}"
                    )
                }

                val providers = pkgInfo.providers.orEmpty()
                lines.add("[7. Vendor Bluetooth packages] pkg=${'$'}pkg | providers count=${'$'}{providers.size}")
                for (prv in providers) {
                    val compEnabled = try {
                        pm.getComponentEnabledSetting(ComponentName(pkg, prv.name)).toString()
                    } catch (e: Throwable) {
                        "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    }
                    val permStr = listOfNotNull(
                        prv.readPermission?.let { "readPermission=${'$'}it" },
                        prv.writePermission?.let { "writePermission=${'$'}it" }
                    ).joinToString(", ").ifEmpty { "none" }
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | PROVIDER class=${'$'}{prv.name} | exported=${'$'}{prv.exported} | getComponentEnabledSetting=${'$'}compEnabled (info.enabled=${'$'}{prv.enabled}) | permission=${'$'}permStr"
                    )
                }

                // 7.3 requestedPermissions
                try {
                    @Suppress("DEPRECATION")
                    val permPkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong()))
                    } else {
                        pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS)
                    }
                    val reqPerms = permPkgInfo.requestedPermissions
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | requestedPermissions=${'$'}{reqPerms?.joinToString(", ") ?: "none"}"
                    )
                } catch (e: Throwable) {
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | requestedPermissions -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    )
                }

                // Parse manifest <action> tags containing "bluetooth" or "bt" for section 8.1
                try {
                    val pkgCtx = context.createPackageContext(pkg, 0)
                    val parser = pkgCtx.assets.openXmlResourceParser("AndroidManifest.xml")
                    val pkgActions = linkedSetOf<String>()
                    try {
                        var eventType = parser.eventType
                        while (eventType != XmlPullParser.END_DOCUMENT) {
                            if (eventType == XmlPullParser.START_TAG && parser.name == "action") {
                                val actionName = parser.getAttributeValue(
                                    "http://schemas.android.com/apk/res/android",
                                    "name"
                                ) ?: parser.getAttributeValue(null, "name")
                                if (!actionName.isNullOrBlank()) {
                                    val lower = actionName.lowercase(Locale.US)
                                    if (lower.contains("bluetooth") || lower.contains("bt")) {
                                        pkgActions.add(actionName)
                                        discoveredManifestBtActions.add(actionName)
                                    }
                                }
                            }
                            eventType = parser.next()
                        }
                    } finally {
                        runCatching { parser.close() }
                    }
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | manifest <action> containing bluetooth/bt=${'$'}{if (pkgActions.isEmpty()) "NONE" else pkgActions.joinToString(", ")}"
                    )
                } catch (e: Throwable) {
                    lines.add(
                        "[7. Vendor Bluetooth packages] pkg=${'$'}pkg | manifest <action> scan -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    )
                }
            } catch (e: Throwable) {
                lines.add(
                    "[7. Vendor Bluetooth packages] pkg=${'$'}pkg -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                )
            }
        }

        // 8. Bluetooth binding
        lines.add("[8. Bluetooth binding] --- queryIntentServices, bindService, Settings & SystemProperties ---")
        val actionsToQuery = linkedSetOf("android.bluetooth.IBluetooth").apply {
            addAll(discoveredManifestBtActions)
        }
        for (actionStr in actionsToQuery) {
            try {
                @Suppress("DEPRECATION")
                val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentServices(Intent(actionStr), PackageManager.ResolveInfoFlags.of(0L))
                } else {
                    pm.queryIntentServices(Intent(actionStr), 0)
                }
                if (resolved.isEmpty()) {
                    lines.add("[8. Bluetooth binding] queryIntentServices(Intent(\"${'$'}actionStr\")) -> NONE")
                } else {
                    val resolvedSummary = resolved.joinToString(", ") { ri ->
                        "${'$'}{ri.serviceInfo?.packageName}/${'$'}{ri.serviceInfo?.name}"
                    }
                    lines.add(
                        "[8. Bluetooth binding] queryIntentServices(Intent(\"${'$'}actionStr\")) -> ${'$'}resolvedSummary"
                    )
                }
            } catch (e: Throwable) {
                lines.add(
                    "[8. Bluetooth binding] queryIntentServices(Intent(\"${'$'}actionStr\")) -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                )
            }
        }

        if (serviceBindProbeStatus.isEmpty()) {
            lines.add("[8. Bluetooth binding] bindService probes -> NONE (no resolved services to bind)")
        } else {
            for ((probeKey, status) in serviceBindProbeStatus.toSortedMap()) {
                lines.add("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}status")
            }
        }

        // 8.3 Settings.Global "bluetooth_on" and any global/secure setting whose name contains "bluetooth"
        val btOnVal = try {
            Settings.Global.getString(context.contentResolver, "bluetooth_on") ?: "null"
        } catch (e: Throwable) {
            "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
        }
        lines.add("[8. Bluetooth binding] Settings.Global.getString(\"bluetooth_on\")=${'$'}btOnVal")

        val globalBtKeys = linkedSetOf<String>()
        try {
            context.contentResolver.query(
                Settings.Global.CONTENT_URI,
                arrayOf("name"),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx >= 0) {
                        val key = cursor.getString(nameIdx)
                        if (!key.isNullOrBlank() && key.lowercase(Locale.US).contains("bluetooth")) {
                            globalBtKeys.add(key)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            lines.add(
                "[8. Bluetooth binding] Settings.Global cursor scan -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            )
        }
        try {
            for (field in Settings.Global::class.java.fields) {
                if (field.type == String::class.java) {
                    val key = runCatching { field.get(null) as? String }.getOrNull()
                    if (!key.isNullOrBlank() && key.lowercase(Locale.US).contains("bluetooth")) {
                        globalBtKeys.add(key)
                    }
                }
            }
        } catch (e: Throwable) {
            lines.add(
                "[8. Bluetooth binding] Settings.Global fields scan -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            )
        }
        for (key in globalBtKeys.sorted()) {
            if (key == "bluetooth_on") continue
            try {
                val v = Settings.Global.getString(context.contentResolver, key)
                if (v != null) {
                    lines.add("[8. Bluetooth binding] Settings.Global.getString(\"${'$'}key\")=${'$'}v")
                }
            } catch (e: Throwable) {
                lines.add(
                    "[8. Bluetooth binding] Settings.Global.getString(\"${'$'}key\") -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                )
            }
        }

        val secureBtKeys = linkedSetOf<String>()
        try {
            context.contentResolver.query(
                Settings.Secure.CONTENT_URI,
                arrayOf("name"),
                null,
                null,
                null
            )?.use { cursor ->
                val nameIdx = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (nameIdx >= 0) {
                        val key = cursor.getString(nameIdx)
                        if (!key.isNullOrBlank() && key.lowercase(Locale.US).contains("bluetooth")) {
                            secureBtKeys.add(key)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            lines.add(
                "[8. Bluetooth binding] Settings.Secure cursor scan -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            )
        }
        try {
            for (field in Settings.Secure::class.java.fields) {
                if (field.type == String::class.java) {
                    val key = runCatching { field.get(null) as? String }.getOrNull()
                    if (!key.isNullOrBlank() && key.lowercase(Locale.US).contains("bluetooth")) {
                        secureBtKeys.add(key)
                    }
                }
            }
        } catch (e: Throwable) {
            lines.add(
                "[8. Bluetooth binding] Settings.Secure fields scan -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            )
        }
        for (key in secureBtKeys.sorted()) {
            val globalRead = try {
                Settings.Global.getString(context.contentResolver, key)
            } catch (e: Throwable) {
                "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            }
            val secureRead = try {
                Settings.Secure.getString(context.contentResolver, key)
            } catch (e: Throwable) {
                "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            }
            if (globalRead != null || secureRead != null) {
                lines.add(
                    "[8. Bluetooth binding] Secure/Global setting \"${'$'}key\" -> Global.getString=${'$'}{globalRead ?: "null"} | Secure.getString=${'$'}{secureRead ?: "null"}"
                )
            }
        }

        // 8.4 SystemProperties reflection for ro.build.display.id, ro.product.name, ro.product.brand, ro.board.platform, ro.bluetooth.*
        val fixedPropKeys = listOf(
            "ro.build.display.id",
            "ro.product.name",
            "ro.product.brand",
            "ro.board.platform"
        )
        try {
            val spClass = Class.forName("android.os.SystemProperties")
            val getMethod = spClass.getMethod("get", String::class.java)
            for (propKey in fixedPropKeys) {
                try {
                    val propVal = getMethod.invoke(null, propKey) as? String
                    if (!propVal.isNullOrEmpty()) {
                        lines.add("[8. Bluetooth binding] SystemProperties.get(\"${'$'}propKey\")=${'$'}propVal")
                    } else {
                        lines.add("[8. Bluetooth binding] SystemProperties.get(\"${'$'}propKey\")=<empty>")
                    }
                } catch (e: Throwable) {
                    lines.add(
                        "[8. Bluetooth binding] SystemProperties.get(\"${'$'}propKey\") -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    )
                }
            }

            // Discover real ro.bluetooth.* keys from getprop without guessing keys
            val discoveredRoBtKeys = linkedSetOf<String>()
            try {
                val proc = ProcessBuilder("getprop").redirectErrorStream(true).start()
                val propRegex = Regex("^\\[(ro\\.bluetooth\\.[^\\]]+)\\]:\\s*\\[(.*)\\]${'$'}")
                proc.inputStream.bufferedReader().useLines { seq ->
                    seq.forEach { rawLine ->
                        val match = propRegex.find(rawLine.trim())
                        if (match != null) {
                            discoveredRoBtKeys.add(match.groupValues[1])
                        }
                    }
                }
                proc.waitFor()
            } catch (e: Throwable) {
                lines.add(
                    "[8. Bluetooth binding] getprop ro.bluetooth.* enumeration -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                )
            }

            var loggedRoBtCount = 0
            for (btPropKey in discoveredRoBtKeys.sorted()) {
                try {
                    val propVal = getMethod.invoke(null, btPropKey) as? String
                    if (!propVal.isNullOrEmpty()) {
                        lines.add("[8. Bluetooth binding] SystemProperties.get(\"${'$'}btPropKey\")=${'$'}propVal")
                        loggedRoBtCount++
                    }
                } catch (e: Throwable) {
                    lines.add(
                        "[8. Bluetooth binding] SystemProperties.get(\"${'$'}btPropKey\") -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                    )
                    loggedRoBtCount++
                }
            }
            if (loggedRoBtCount == 0) {
                lines.add("[8. Bluetooth binding] SystemProperties ro.bluetooth.* -> NONE (0 keys returned a value)")
            }
        } catch (e: Throwable) {
            lines.add(
                "[8. Bluetooth binding] Class.forName(\"android.os.SystemProperties\") -> EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
            )
        }

        return lines
    }

    /**
     * Runs read-only bindService probes for the first resolved service of Intent("android.bluetooth.IBluetooth")
     * and any manifest action containing "bluetooth" or "bt". Logs boolean result and whether
     * onServiceConnected / onNullBinding / onBindingDied arrives within 10s, then unbinds immediately
     * without calling any method on the binder.
     */
    private fun runReadOnlyBluetoothBindingProbes() {
        val vendorBtPackages = listOf(
            "com.android.bluetooth",
            "com.bt.bc03",
            "com.zjinnova.zlink",
            "com.asrmicro.boltboost",
            "vendor.asr.wcn"
        )
        val actionsToQuery = linkedSetOf("android.bluetooth.IBluetooth")
        val pm = context.packageManager

        for (pkg in vendorBtPackages) {
            runCatching {
                val pkgCtx = context.createPackageContext(pkg, 0)
                val parser = pkgCtx.assets.openXmlResourceParser("AndroidManifest.xml")
                try {
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "action") {
                            val actionName = parser.getAttributeValue(
                                "http://schemas.android.com/apk/res/android",
                                "name"
                            ) ?: parser.getAttributeValue(null, "name")
                            if (!actionName.isNullOrBlank()) {
                                val lower = actionName.lowercase(Locale.US)
                                if (lower.contains("bluetooth") || lower.contains("bt")) {
                                    actionsToQuery.add(actionName)
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                } finally {
                    runCatching { parser.close() }
                }
            }
        }

        for (actionStr in actionsToQuery) {
            try {
                @Suppress("DEPRECATION")
                val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    pm.queryIntentServices(Intent(actionStr), PackageManager.ResolveInfoFlags.of(0L))
                } else {
                    pm.queryIntentServices(Intent(actionStr), 0)
                }
                val firstSvc = resolved.firstOrNull()?.serviceInfo ?: continue
                val probeKey = "${'$'}actionStr -> ${'$'}{firstSvc.packageName}/${'$'}{firstSvc.name}"
                pendingBindTimeoutRunnables.remove(probeKey)?.let { mainHandler.removeCallbacks(it) }

                val callbackReceived = AtomicBoolean(false)
                var bindBoolResult = false
                val bindStartTs = nowTime()

                val conn = object : ServiceConnection {
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        if (callbackReceived.compareAndSet(false, true)) {
                            pendingBindTimeoutRunnables.remove(probeKey)?.let { mainHandler.removeCallbacks(it) }
                            val ts = nowTime()
                            val unbindMsg = try {
                                context.unbindService(this)
                                "unbindService=ok"
                            } catch (e: Throwable) {
                                "unbindService=EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                            }
                            val msg = "bindService=${'$'}bindBoolResult (at ${'$'}bindStartTs) | onServiceConnected at ${'$'}ts (binderNonNull=${'$'}{service != null}) | ${'$'}unbindMsg"
                            serviceBindProbeStatus[probeKey] = msg
                            logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}msg")
                            onHardwareStateChanged()
                        }
                    }

                    override fun onNullBinding(name: ComponentName?) {
                        if (callbackReceived.compareAndSet(false, true)) {
                            pendingBindTimeoutRunnables.remove(probeKey)?.let { mainHandler.removeCallbacks(it) }
                            val ts = nowTime()
                            val unbindMsg = try {
                                context.unbindService(this)
                                "unbindService=ok"
                            } catch (e: Throwable) {
                                "unbindService=EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                            }
                            val msg = "bindService=${'$'}bindBoolResult (at ${'$'}bindStartTs) | onNullBinding at ${'$'}ts | ${'$'}unbindMsg"
                            serviceBindProbeStatus[probeKey] = msg
                            logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}msg")
                            onHardwareStateChanged()
                        }
                    }

                    override fun onBindingDied(name: ComponentName?) {
                        if (callbackReceived.compareAndSet(false, true)) {
                            pendingBindTimeoutRunnables.remove(probeKey)?.let { mainHandler.removeCallbacks(it) }
                            val ts = nowTime()
                            val unbindMsg = try {
                                context.unbindService(this)
                                "unbindService=ok"
                            } catch (e: Throwable) {
                                "unbindService=EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                            }
                            val msg = "bindService=${'$'}bindBoolResult (at ${'$'}bindStartTs) | onBindingDied at ${'$'}ts | ${'$'}unbindMsg"
                            serviceBindProbeStatus[probeKey] = msg
                            logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}msg")
                            onHardwareStateChanged()
                        }
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        val ts = nowTime()
                        logEvent("[8. Bluetooth binding] onServiceDisconnected(${'$'}probeKey) at ${'$'}ts")
                    }
                }

                try {
                    val explicitIntent = Intent(actionStr).apply {
                        component = ComponentName(firstSvc.packageName, firstSvc.name)
                    }
                    bindBoolResult = context.bindService(explicitIntent, conn, Context.BIND_AUTO_CREATE)
                    serviceBindProbeStatus[probeKey] = "bindService=${'$'}bindBoolResult (at ${'$'}bindStartTs) | waiting 10s for callback..."
                    logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> returned ${'$'}bindBoolResult at ${'$'}bindStartTs")

                    val timeoutRunnable = Runnable {
                        pendingBindTimeoutRunnables.remove(probeKey)
                        if (callbackReceived.compareAndSet(false, true)) {
                            val ts = nowTime()
                            val unbindMsg = try {
                                context.unbindService(conn)
                                "unbindService=ok"
                            } catch (e: Throwable) {
                                "unbindService=EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                            }
                            val msg = "bindService=${'$'}bindBoolResult (at ${'$'}bindStartTs) | NO CALLBACK after 10s (at ${'$'}ts) | ${'$'}unbindMsg"
                            serviceBindProbeStatus[probeKey] = msg
                            logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}msg")
                            onHardwareStateChanged()
                        }
                    }
                    pendingBindTimeoutRunnables[probeKey] = timeoutRunnable
                    mainHandler.postDelayed(timeoutRunnable, 10_000L)
                } catch (e: Throwable) {
                    val ts = nowTime()
                    runCatching { context.unbindService(conn) }
                    val msg = "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message}) at ${'$'}ts"
                    serviceBindProbeStatus[probeKey] = msg
                    logEvent("[8. Bluetooth binding] bindService(${'$'}probeKey) -> ${'$'}msg")
                }
            } catch (e: Throwable) {
                val msg = "EXCEPTION(${'$'}{e.javaClass.name}: ${'$'}{e.message})"
                serviceBindProbeStatus["query(${'$'}actionStr)"] = msg
                logEvent("[8. Bluetooth binding] query/bind(${'$'}actionStr) -> ${'$'}msg")
            }
        }
    }

    fun logBluetoothStackDiagnostics(trigger: String) {
        runReadOnlyBluetoothBindingProbes()
        logEvent("--- BEGIN Bluetooth stack diagnostics (${'$'}trigger) ---")
        buildBluetoothStackDiagnosticsSnapshot().forEach { line ->
            logEvent(line)
        }
        logEvent("--- END Bluetooth stack diagnostics (${'$'}trigger) ---")
    }
}"""
        ),
        ExportableProjectFile(
            id = "switcher_viewmodel",
            title = "LarkSwitcherViewModel.kt",
            relativePath = "app/src/main/java/com/example/viewmodel/LarkSwitcherViewModel.kt",
            languageBadge = "Kotlin",
            description = "4-Step Smart Switch State Machine & 1000-Entry Diagnostic Logger",
            content = """package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.hardware.LarkHardwareManager
import com.example.model.LarkBtProfiles
import com.example.model.PairedPhoneItem
import com.example.model.SmartSwitchModalState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LarkUiState(
    val pairedPhones: List<PairedPhoneItem> = emptyList(),
    val isAnyPhoneConnected: Boolean = false,
    val isRefreshingList: Boolean = false,
    val isSettingsSheetOpen: Boolean = false,
    val isExportModalOpen: Boolean = false,
    val settingsStatusBannerHe: String? = null,
    val isRestartingBt: Boolean = false,
    val smartSwitchModalState: SmartSwitchModalState = SmartSwitchModalState.Hidden,
    val diagnosticLogs: List<String> = emptyList(),
    val bluetoothStackDiagnostics: List<String> = emptyList(),
    val isHardwarePreviewMode: Boolean = false,
    val hiddenDeviceCount: Int = 0
)

class LarkSwitcherViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.applicationContext.getSharedPreferences(
        "lark_bt_hidden_devices_prefs",
        Context.MODE_PRIVATE
    )

    private val _uiState = MutableStateFlow(LarkUiState())
    val uiState: StateFlow<LarkUiState> = _uiState.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)
    private var switchJob: Job? = null
    private var resetJob: Job? = null

    // Pauses the 2.5s background sync while a Smart Switch or adapter reset is running
    private val isBackgroundSyncPaused = AtomicBoolean(false)

    // Preview placeholders shown ONLY when host has 0 bonded Bluetooth devices (marked disconnected & hardware-only)
    private val previewPairedPhones = MutableStateFlow(
        listOf(
            PairedPhoneItem(
                id = "preview_phone_1",
                name = "iPhone 16 Pro (תצוגה מקדימה - חומרה בלבד)",
                isConnected = false,
                bluetoothDevice = null,
                isHardwareOnlyPreviewPlaceholder = true
            ),
            PairedPhoneItem(
                id = "preview_phone_2",
                name = "Samsung Galaxy S24 (תצוגה מקדימה - חומרה בלבד)",
                isConnected = false,
                bluetoothDevice = null,
                isHardwareOnlyPreviewPlaceholder = true
            ),
            PairedPhoneItem(
                id = "preview_phone_3",
                name = "Google Pixel 9 Pro (תצוגה מקדימה - חומרה בלבד)",
                isConnected = false,
                bluetoothDevice = null,
                isHardwareOnlyPreviewPlaceholder = true
            )
        )
    )

    private val hardwareManager = LarkHardwareManager(
        context = application.applicationContext,
        onLog = { msg -> appendDiagnosticLog(msg) },
        onHardwareStateChanged = {
            if (!isBackgroundSyncPaused.get()) {
                syncPairedPhones(force = false)
            }
        }
    )

    init {
        appendDiagnosticLog("ViewModel init: initializing LarkHardwareManager")
        hardwareManager.initialize()
        syncPairedPhones(force = true)
        startBackgroundMonitor()
    }

    private fun getHiddenDeviceIds(): Set<String> {
        return sharedPrefs.getStringSet("hidden_device_ids", emptySet())?.toSet().orEmpty()
    }

    fun hideDevice(phone: PairedPhoneItem) {
        val updated = getHiddenDeviceIds() + phone.id
        sharedPrefs.edit().putStringSet("hidden_device_ids", updated).apply()
        appendDiagnosticLog("Hidden device card: ${'$'}{phone.name} (${'$'}{phone.id}) | Total hidden: ${'$'}{updated.size}")
        syncPairedPhones(force = true)
    }

    fun restoreAllHiddenDevices() {
        val count = getHiddenDeviceIds().size
        sharedPrefs.edit().remove("hidden_device_ids").apply()
        appendDiagnosticLog("Restored ${'$'}count hidden devices")
        _uiState.update {
            it.copy(settingsStatusBannerHe = "כל המכשירים המוסתרים שוחזרו")
        }
        syncPairedPhones(force = true)
    }

    private fun appendDiagnosticLog(message: String) {
        val line = "[${'$'}{timeFormat.format(Date())}] ${'$'}message"
        _uiState.update { state ->
            state.copy(diagnosticLogs = (state.diagnosticLogs + line).takeLast(1000))
        }
    }

    fun clearDiagnosticLogs() {
        _uiState.update { it.copy(diagnosticLogs = emptyList()) }
        appendDiagnosticLog("Diagnostic log cleared")
    }

    fun onPermissionsResult() {
        appendDiagnosticLog("onPermissionsResult: re-binding profile proxies")
        hardwareManager.bindAllProfileProxies()
        hardwareManager.logBluetoothStackDiagnostics("onPermissionsResult")
        syncPairedPhones(force = true)
    }

    /**
     * Top Header Refresh Button (🔄):
     * Re-binds BluetoothProfile proxies and refreshes the paired phones list.
     */
    fun refreshPairedPhonesWithFeedback() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingList = true) }
            appendDiagnosticLog("Manual refresh requested by user")
            hardwareManager.bindAllProfileProxies()
            syncPairedPhones(force = true)
            delay(500)
            _uiState.update { it.copy(isRefreshingList = false) }
        }
    }

    fun openSettingsSheet() {
        _uiState.update { it.copy(isSettingsSheetOpen = true, settingsStatusBannerHe = null) }
    }

    fun closeSettingsSheet() {
        _uiState.update { it.copy(isSettingsSheetOpen = false, settingsStatusBannerHe = null) }
    }

    fun openExportModal() {
        val snapshot = hardwareManager.buildBluetoothStackDiagnosticsSnapshot()
        _uiState.update {
            it.copy(
                isSettingsSheetOpen = false,
                isExportModalOpen = true,
                bluetoothStackDiagnostics = snapshot
            )
        }
    }

    fun closeExportModal() {
        _uiState.update { it.copy(isExportModalOpen = false) }
    }

    /**
     * Settings Sheet Button 1: "צימוד טלפון חדש" (Pair New Phone)
     */
    fun openNativeBluetoothPairing(context: Context) {
        closeSettingsSheet()
        appendDiagnosticLog("Launching Intent(Settings.ACTION_BLUETOOTH_SETTINGS)")
        runCatching {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure { e ->
            appendDiagnosticLog("ACTION_BLUETOOTH_SETTINGS failed: ${'$'}{e.javaClass.simpleName}: ${'$'}{e.message}")
        }
    }

    /**
     * Settings Sheet Button 2: "איפוס בלוטות' מהיר" (Restart Bluetooth - checks boolean & SecurityException)
     * Pauses background sync during reset and resumes in finally{}.
     */
    fun restartBluetoothAdapter() {
        if (_uiState.value.isRestartingBt) return
        resetJob?.cancel()
        resetJob = viewModelScope.launch {
            isBackgroundSyncPaused.set(true)
            appendDiagnosticLog("Background 2.5s sync PAUSED for Hard Reset BT Adapter")
            try {
                appendDiagnosticLog("Hard Reset BT Adapter requested")
                _uiState.update {
                    it.copy(
                        isRestartingBt = true,
                        settingsStatusBannerHe = "מכבה בלוטות' ומנקה חיבורים תקועים..."
                    )
                }

                val disabledOk = withContext(Dispatchers.IO) {
                    hardwareManager.freeSystemMemoryAndKillStuckBtTasks()
                    hardwareManager.disableBluetoothAdapter()
                }

                if (!disabledOk) {
                    appendDiagnosticLog("Hard Reset FAILED: BluetoothAdapter.disable() returned false or was blocked by OS -> הפעולה נחסמה על ידי המערכת")
                    _uiState.update {
                        it.copy(
                            isRestartingBt = false,
                            settingsStatusBannerHe = "הפעולה נחסמה על ידי המערכת"
                        )
                    }
                    return@launch
                }

                delay(3000L)
                _uiState.update {
                    it.copy(settingsStatusBannerHe = "מפעיל מחדש את הבלוטות'...")
                }

                val enabledOk = withContext(Dispatchers.IO) {
                    hardwareManager.enableBluetoothAdapter()
                }

                if (!enabledOk) {
                    appendDiagnosticLog("Hard Reset FAILED: BluetoothAdapter.enable() returned false or was blocked by OS -> הפעולה נחסמה על ידי המערכת")
                    _uiState.update {
                        it.copy(
                            isRestartingBt = false,
                            settingsStatusBannerHe = "הפעולה נחסמה על ידי המערכת"
                        )
                    }
                    return@launch
                }

                delay(600L)
                hardwareManager.bindAllProfileProxies()
                appendDiagnosticLog("Hard Reset SUCCEEDED: disable()=true, enable()=true")
                _uiState.update {
                    it.copy(
                        isRestartingBt = false,
                        settingsStatusBannerHe = "איפוס הבלוטות' הושלם בהצלחה"
                    )
                }
            } finally {
                _uiState.update { it.copy(isRestartingBt = false) }
                isBackgroundSyncPaused.set(false)
                appendDiagnosticLog("Background 2.5s sync RESUMED after Hard Reset BT Adapter")
                syncPairedPhones(force = true)
            }
        }
    }

    /**
     * Settings Sheet Button 3: "רענון זיכרון מערכת" (Refresh System Memory)
     */
    fun refreshSystemMemory() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(settingsStatusBannerHe = "מנקה תהליכי רקע וזיכרון מערכת...")
            }
            withContext(Dispatchers.IO) {
                hardwareManager.freeSystemMemoryAndKillStuckBtTasks()
            }
            delay(450L)
            _uiState.update {
                it.copy(settingsStatusBannerHe = "זיכרון המערכת רוענן בהצלחה")
            }
        }
    }

    /**
     * Card Button ("ניתוק") on a currently connected phone:
     * Cleanly disconnects the active phone across all profiles.
     */
    fun disconnectPhone(phone: PairedPhoneItem) {
        viewModelScope.launch {
            appendDiagnosticLog("Manual disconnect requested for ${'$'}{phone.name} (${'$'}{phone.id})")
            withContext(Dispatchers.IO) {
                hardwareManager.forceDisconnectAllProfiles(phone.bluetoothDevice)
            }
            syncPairedPhones(force = true)
        }
    }

    /**
     * Card Button ("התחבר לטלפון זה") on a disconnected phone card:
     * Runs the 4-Step Smooth "Smart Switch" sequence with honest 250ms x 3500ms STATE_CONNECTED polling.
     * Pauses the 2.5s background sync while running and resumes it in finally{}.
     */
    fun connectToPhoneSmartSwitch(targetPhone: PairedPhoneItem) {
        if (_uiState.value.smartSwitchModalState is SmartSwitchModalState.Switching) return

        switchJob?.cancel()
        switchJob = viewModelScope.launch {
            isBackgroundSyncPaused.set(true)
            appendDiagnosticLog("Background 2.5s sync PAUSED for Smart Switch")
            try {
                appendDiagnosticLog("=== Smart Switch START -> Target: ${'$'}{targetPhone.name} (${'$'}{targetPhone.id}) ===")

                // STEP 1 (25%): "מנתק את הטלפון הקודם..."
                _uiState.update {
                    it.copy(
                        smartSwitchModalState = SmartSwitchModalState.Switching(
                            targetPhoneName = targetPhone.name,
                            step = 1,
                            progressPercent = 25,
                            statusTextHe = "מנתק את הטלפון הקודם...",
                            isComplete = false,
                            isFailed = false
                        )
                    )
                }

                withContext(Dispatchers.IO) {
                    // 1a. Free system memory and kill stuck Chinese factory BT/bridge packages
                    hardwareManager.freeSystemMemoryAndKillStuckBtTasks()
                    // 1b. Force-disconnect previous active phone across all profiles (1, 2, 11, 12, 16, 17)
                    val currentlyConnected = _uiState.value.pairedPhones.filter { it.isConnected }
                    currentlyConnected.forEach { activePhone ->
                        hardwareManager.forceDisconnectAllProfiles(activePhone.bluetoothDevice)
                    }
                    hardwareManager.forceDisconnectAllProfiles(targetPhone.bluetoothDevice)
                }

                syncPairedPhones(force = true)
                delay(500L)

                // STEP 2 (60%): "מרענן את חיבור הבלוטות'..." (1800ms hardware buffer flush)
                appendDiagnosticLog("Smart Switch Step 2: Waiting 1800ms for ASR lark_evb UART buffer flush...")
                _uiState.update {
                    it.copy(
                        smartSwitchModalState = SmartSwitchModalState.Switching(
                            targetPhoneName = targetPhone.name,
                            step = 2,
                            progressPercent = 60,
                            statusTextHe = "מרענן את חיבור הבלוטות'...",
                            isComplete = false,
                            isFailed = false
                        )
                    )
                }
                delay(1800L)

                // STEP 3 (90%): "מתחבר אל [Phone Name]..."
                appendDiagnosticLog("Smart Switch Step 3: Sequential connect for ${'$'}{targetPhone.name}")
                _uiState.update {
                    it.copy(
                        smartSwitchModalState = SmartSwitchModalState.Switching(
                            targetPhoneName = targetPhone.name,
                            step = 3,
                            progressPercent = 90,
                            statusTextHe = "מתחבר אל ${'$'}{targetPhone.name}...",
                            isComplete = false,
                            isFailed = false
                        )
                    )
                }

                val triedProfileResults = LinkedHashMap<Int, Boolean>()

                withContext(Dispatchers.IO) {
                    // Block PBAP_CLIENT (17) so contact sync never freezes the switch
                    hardwareManager.blockPbapClientForDevice(targetPhone.bluetoothDevice)
                    // Connect Calls / HFP first (16 & 1)
                    for (callProfile in LarkBtProfiles.CALL_PROFILES) {
                        val res = hardwareManager.reflectConnectProfile(callProfile, targetPhone.bluetoothDevice)
                        triedProfileResults[callProfile] = res
                    }
                }

                // Wait 600ms stagger before connecting Media / A2DP
                delay(600L)

                withContext(Dispatchers.IO) {
                    // Connect Media / A2DP (11, 2, 12)
                    for (mediaProfile in LarkBtProfiles.MEDIA_PROFILES) {
                        val res = hardwareManager.reflectConnectProfile(mediaProfile, targetPhone.bluetoothDevice)
                        triedProfileResults[mediaProfile] = res
                    }
                    // Re-verify PBAP_CLIENT (17) stays disconnected
                    hardwareManager.blockPbapClientForDevice(targetPhone.bluetoothDevice)
                }

                // Poll every 250ms for up to 3500ms until at least one of profiles 1, 2, 11, 12, or 16 reports STATE_CONNECTED
                val pollIntervalMs = 250L
                val maxPollMs = 3500L
                var elapsedPollMs = 0L
                var verifiedConnected = false
                var lastStateSummary = ""

                while (elapsedPollMs <= maxPollMs) {
                    val (connectedNow, breakdown) = withContext(Dispatchers.IO) {
                        hardwareManager.checkAnyTargetProfileConnected(targetPhone.bluetoothDevice)
                    }
                    lastStateSummary = breakdown
                    appendDiagnosticLog("Poll +${'$'}{elapsedPollMs}ms [${'$'}{targetPhone.id}]: connected=${'$'}connectedNow | ${'$'}breakdown")
                    if (connectedNow) {
                        verifiedConnected = true
                        break
                    }
                    if (elapsedPollMs < maxPollMs) {
                        delay(pollIntervalMs)
                    }
                    elapsedPollMs += pollIntervalMs
                }

                syncPairedPhones(force = true)

                if (verifiedConnected) {
                    appendDiagnosticLog("Smart Switch SUCCEEDED for ${'$'}{targetPhone.name}: ${'$'}lastStateSummary")
                    _uiState.update {
                        it.copy(
                            smartSwitchModalState = SmartSwitchModalState.Switching(
                                targetPhoneName = targetPhone.name,
                                step = 4,
                                progressPercent = 100,
                                statusTextHe = "החיבור הושלם!",
                                isComplete = true,
                                isFailed = false
                            )
                        )
                    }
                    delay(1250L)
                } else {
                    val triedSummary = triedProfileResults.entries.joinToString(", ") {
                        "profile(${'$'}{it.key})->connect()=${'$'}{it.value}"
                    }
                    appendDiagnosticLog(
                        "Smart Switch FAILED for ${'$'}{targetPhone.name} after ${'$'}{maxPollMs}ms | Tried: [${'$'}triedSummary] | Final states: [${'$'}lastStateSummary]"
                    )
                    _uiState.update {
                        it.copy(
                            smartSwitchModalState = SmartSwitchModalState.Switching(
                                targetPhoneName = targetPhone.name,
                                step = 4,
                                progressPercent = 100,
                                statusTextHe = "לא ניתן להתחבר, ודא שהבלוטות' בטלפון פעיל",
                                isComplete = false,
                                isFailed = true
                            )
                        )
                    }
                    delay(2400L)
                }
            } finally {
                _uiState.update {
                    it.copy(smartSwitchModalState = SmartSwitchModalState.Hidden)
                }
                isBackgroundSyncPaused.set(false)
                appendDiagnosticLog("Background 2.5s sync RESUMED after Smart Switch")
                syncPairedPhones(force = true)
            }
        }
    }

    private fun startBackgroundMonitor() {
        viewModelScope.launch {
            while (isActive) {
                if (!isBackgroundSyncPaused.get()) {
                    syncPairedPhones(force = false)
                }
                delay(2500L)
            }
        }
    }

    private fun syncPairedPhones(force: Boolean) {
        if (!force && isBackgroundSyncPaused.get()) return

        val hiddenIds = getHiddenDeviceIds()
        val hwBonded = hardwareManager.queryHardwareBondedPhones()
        val isPreview = hwBonded.isEmpty()
        val allPhones = if (hwBonded.isNotEmpty()) {
            hwBonded
        } else {
            previewPairedPhones.value
        }
        val visiblePhones = allPhones.filterNot { hiddenIds.contains(it.id) }
        val hiddenCount = allPhones.count { hiddenIds.contains(it.id) }
        val anyConnected = allPhones.any { it.isConnected }

        val stackSnapshot = hardwareManager.buildBluetoothStackDiagnosticsSnapshot()

        _uiState.update {
            it.copy(
                pairedPhones = visiblePhones,
                isAnyPhoneConnected = anyConnected,
                bluetoothStackDiagnostics = stackSnapshot,
                isHardwarePreviewMode = isPreview,
                hiddenDeviceCount = hiddenCount
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        hardwareManager.release()
    }
}"""
        ),
        ExportableProjectFile(
            id = "apple_bt_components",
            title = "AppleBluetoothComponents.kt",
            relativePath = "app/src/main/java/com/example/ui/components/AppleBluetoothComponents.kt",
            languageBadge = "Kotlin",
            description = "Minimalist Apple-Style RTL Header, Cards, Settings & Smart Switch Overlay",
            content = """package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.PairedPhoneItem
import com.example.model.SmartSwitchModalState
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleCardBorder
import com.example.ui.theme.AppleCardElevated
import com.example.ui.theme.AppleCardSurface
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleMutedGray
import com.example.ui.theme.AppleRed
import com.example.ui.theme.AppleSubtleButtonBg
import com.example.ui.theme.AppleWhite
import com.example.ui.theme.OledBlack

/**
 * 1. Minimal Top Header (RTL Layout):
 * - Right side (Start in RTL): Bold title "מנהל בלוטות'" + Single status pill ("מחובר" Green or "לא מחובר" Gray).
 * - Left side (End in RTL): Two simple circular icon buttons: Refresh (🔄) and Settings (⚙️).
 */
@Composable
fun AppleTopHeader(
    isAnyConnected: Boolean,
    isRefreshing: Boolean,
    onRefreshClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotationAngle by animateFloatAsState(
        targetValue = if (isRefreshing) 360f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "refreshRotation"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 22.dp)
            .testTag("apple_top_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right Side (Start in RTL): Title + Single Status Pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "מנהל בלוטות'",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AppleWhite
            )

            ConnectionStatusPill(isConnected = isAnyConnected)
        }

        // Left Side (End in RTL): Two Circular Icon Buttons (🔄 Refresh & ⚙️ Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            CircularHeaderIconButton(
                icon = Icons.Default.Refresh,
                contentDescription = "רענון רשימת טלפונים",
                onClick = onRefreshClick,
                iconModifier = Modifier.rotate(rotationAngle),
                testTag = "btn_refresh_phones"
            )

            CircularHeaderIconButton(
                icon = Icons.Default.Settings,
                contentDescription = "הגדרות בלוטות'",
                onClick = onSettingsClick,
                testTag = "btn_open_settings"
            )
        }
    }
}

@Composable
private fun ConnectionStatusPill(isConnected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotGlow"
    )

    val pillColor by animateColorAsState(
        targetValue = if (isConnected) AppleGreen.copy(alpha = 0.16f) else AppleCardSurface,
        label = "pillBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isConnected) AppleGreen.copy(alpha = 0.45f) else AppleCardBorder,
        label = "pillBorder"
    )
    val dotColor = if (isConnected) AppleGreen else AppleMutedGray
    val labelText = if (isConnected) "מחובר" else "לא מחובר"

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(pillColor)
            .border(1.dp, borderColor, CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("header_status_pill"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (isConnected) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AppleGreen.copy(alpha = glowAlpha * 0.35f))
                )
            }
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
        Text(
            text = labelText,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            color = if (isConnected) AppleGreen else AppleMutedGray
        )
    }
}

@Composable
private fun CircularHeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    iconModifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(AppleCardSurface)
            .border(1.dp, AppleCardBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = AppleWhite,
            modifier = iconModifier.size(24.dp)
        )
    }
}

/**
 * 2. Center Stage: Big Side-by-Side Paired Phone Cards (BluetoothAdapter.bondedDevices)
 * Each card contains ONLY 3 clean elements + small hide action:
 * 1) Sleek Smartphone Vector Icon at the top
 * 2) Phone Name in large, bold, high-contrast white text
 * 3) Status & Single Action Button:
 *    - Connected: Green badge "מחובר כעת" + subtle dark button "ניתוק"
 *    - Disconnected: Full-width Apple-Blue button "התחבר לטלפון זה"
 */
@Composable
fun PairedPhonesCenterStage(
    phones: List<PairedPhoneItem>,
    hiddenDeviceCount: Int,
    onConnectClick: (PairedPhoneItem) -> Unit,
    onDisconnectClick: (PairedPhoneItem) -> Unit,
    onHideDeviceClick: (PairedPhoneItem) -> Unit,
    onRestoreHiddenDevicesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 36.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (phones.isEmpty() && hiddenDeviceCount > 0) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(AppleCardSurface)
                    .border(1.dp, AppleCardBorder, RoundedCornerShape(16.dp))
                    .clickable(onClick = onRestoreHiddenDevicesClick)
                    .padding(horizontal = 24.dp, vertical = 14.dp)
                    .testTag("btn_restore_hidden_empty_state"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = "הצג מכשירים מוסתרים",
                    tint = AppleBlue,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "הצג מכשירים מוסתרים (${'$'}hiddenDeviceCount)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = AppleWhite
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("paired_phones_row"),
                horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(phones, key = { it.id }) { phone ->
                    ApplePairedPhoneCard(
                        phone = phone,
                        onConnectClick = { onConnectClick(phone) },
                        onDisconnectClick = { onDisconnectClick(phone) },
                        onHideClick = { onHideDeviceClick(phone) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ApplePairedPhoneCard(
    phone: PairedPhoneItem,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onHideClick: () -> Unit
) {
    val isConnected = phone.isConnected
    val borderColor by animateColorAsState(
        targetValue = if (isConnected) AppleGreen.copy(alpha = 0.65f) else AppleCardBorder,
        animationSpec = tween(300),
        label = "cardBorder"
    )

    Box(
        modifier = Modifier
            .width(345.dp)
            .height(360.dp)
            .shadow(
                elevation = if (isConnected) 20.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isConnected) AppleGreen.copy(alpha = 0.25f) else Color.Black
            )
            .clip(RoundedCornerShape(24.dp))
            .background(AppleCardSurface)
            .border(
                width = if (isConnected) 1.8.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .testTag("phone_card_${'$'}{phone.id}")
    ) {
        // Small unobtrusive "hide this device" action in top-end corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(AppleSubtleButtonBg.copy(alpha = 0.7f))
                .clickable(onClick = onHideClick)
                .testTag("btn_hide_device_${'$'}{phone.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = "הסתר מכשיר זה",
                tint = AppleMutedGray,
                modifier = Modifier.size(16.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Element 1: Sleek Smartphone Vector Icon at the top
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(
                        if (isConnected) {
                            Brush.radialGradient(
                                colors = listOf(
                                    AppleGreen.copy(alpha = 0.24f),
                                    AppleCardElevated
                                )
                            )
                        } else {
                            Brush.radialGradient(
                                colors = listOf(
                                    AppleBlue.copy(alpha = 0.16f),
                                    AppleCardElevated
                                )
                            )
                        }
                    )
                    .border(
                        width = 1.dp,
                        color = if (isConnected) AppleGreen.copy(alpha = 0.45f) else AppleCardBorder,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneIphone,
                    contentDescription = phone.name,
                    tint = if (isConnected) AppleGreen else AppleWhite,
                    modifier = Modifier.size(42.dp)
                )
            }

            // Element 2: Phone Name in large, bold, high-contrast white text
            Text(
                text = phone.name,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AppleWhite,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            // Element 3: Status & Single Action Button
            if (isConnected) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Green badge "מחובר כעת"
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AppleGreen.copy(alpha = 0.16f))
                            .border(1.dp, AppleGreen.copy(alpha = 0.45f), CircleShape)
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AppleGreen)
                        )
                        Text(
                            text = "מחובר כעת",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                            color = AppleGreen
                        )
                    }

                    // Subtle dark button "ניתוק"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppleSubtleButtonBg)
                            .border(1.dp, AppleCardBorder, RoundedCornerShape(16.dp))
                            .clickable(onClick = onDisconnectClick)
                            .testTag("btn_disconnect_${'$'}{phone.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ניתוק",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AppleMutedGray
                        )
                    }
                }
            } else {
                // Disconnected: Prominent, full-width Apple-Blue button "התחבר לטלפון זה"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .shadow(
                                elevation = 10.dp,
                                shape = RoundedCornerShape(16.dp),
                                spotColor = AppleBlue.copy(alpha = 0.4f)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .background(AppleBlue)
                            .clickable(onClick = onConnectClick)
                            .testTag("btn_connect_${'$'}{phone.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "התחבר לטלפון זה",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = AppleWhite
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Smooth "Smart Switch" Glassmorphic Popup Overlay (AirPods connection sheet style):
 * - Step 1 (25%): "מנתק את הטלפון הקודם..."
 * - Step 2 (60%): "מרענן את חיבור הבלוטות'..."
 * - Step 3 (90%): "מתחבר אל [Phone Name]..."
 * - Step 4 (100%): Polls every 250ms up to 3500ms for STATE_CONNECTED on profiles 1, 2, 11, 12, or 16:
 *   - Success: Animated Green Checkmark "החיבור הושלם!"
 *   - Failure: Red Icon "לא ניתן להתחבר, ודא שהבלוטות' בטלפון פעיל"
 */
@Composable
fun SmartSwitchPopupOverlay(
    state: SmartSwitchModalState
) {
    if (state !is SmartSwitchModalState.Switching) return

    val animatedProgress by animateFloatAsState(
        targetValue = state.progressPercent / 100f,
        animationSpec = tween(durationMillis = 420, easing = FastOutSlowInEasing),
        label = "ringProgress"
    )

    val ringColor = when {
        state.isFailed -> AppleRed
        state.isComplete -> AppleGreen
        else -> AppleBlue
    }

    Dialog(
        onDismissRequest = { /* Modal is non-dismissable during active switch */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack.copy(alpha = 0.72f))
                .testTag("smart_switch_popup_overlay"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(420.dp)
                    .shadow(28.dp, RoundedCornerShape(28.dp))
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF252528),
                                AppleCardSurface
                            )
                        )
                    )
                    .border(1.dp, Color(0xFF3A3A3C), RoundedCornerShape(28.dp))
                    .padding(horizontal = 32.dp, vertical = 34.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // Progress Ring + Center Icon / Animated Checkmark or Failure Icon
                Box(
                    modifier = Modifier.size(108.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Track circle
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = AppleCardBorder,
                        strokeWidth = 7.dp
                    )

                    // Animated progress arc
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = ringColor,
                        strokeWidth = 7.dp,
                        strokeCap = StrokeCap.Round
                    )

                    AnimatedContent(
                        targetState = Pair(state.isComplete, state.isFailed),
                        transitionSpec = {
                            (scaleIn(animationSpec = tween(260)) + fadeIn(tween(220)))
                                .togetherWith(fadeOut(tween(150)))
                        },
                        label = "centerIconTransition"
                    ) { (completed, failed) ->
                        when {
                            completed -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(AppleGreen.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "החיבור הושלם",
                                        tint = AppleGreen,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                            failed -> {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(AppleRed.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "החיבור נכשל",
                                        tint = AppleRed,
                                        modifier = Modifier.size(42.dp)
                                    )
                                }
                            }
                            else -> {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "${'$'}{state.progressPercent}%",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = AppleBlue
                                    )
                                }
                            }
                        }
                    }
                }

                // Target Phone Name
                Text(
                    text = state.targetPhoneName,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AppleWhite,
                    textAlign = TextAlign.Center
                )

                // Step Status Text in Hebrew
                Text(
                    text = state.statusTextHe,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = when {
                        state.isFailed -> AppleRed
                        state.isComplete -> AppleGreen
                        else -> AppleMutedGray
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 4. Minimal Settings Sheet (Opened via ⚙️ Icon):
 * Contains the 3 straightforward buttons:
 * 1) "צימוד טלפון חדש" (Pair New Phone)
 * 2) "איפוס בלוטות' מהיר" (Restart Bluetooth)
 * 3) "רענון זיכרון מערכת" (Refresh System)
 * Plus the discrete "ייצוא קוד APK (Export Android Project)" button.
 */
@Composable
fun MinimalSettingsModalSheet(
    isOpen: Boolean,
    statusBannerHe: String?,
    isRestartingBt: Boolean,
    hiddenDeviceCount: Int,
    onDismiss: () -> Unit,
    onPairNewPhoneClick: () -> Unit,
    onRestartBluetoothClick: () -> Unit,
    onRefreshSystemClick: () -> Unit,
    onRestoreHiddenDevicesClick: () -> Unit,
    onOpenExportProjectClick: () -> Unit
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OledBlack.copy(alpha = 0.72f))
                .clickable(onClick = onDismiss)
                .testTag("settings_modal_sheet"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(0.88f)
                    .shadow(24.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(AppleCardSurface)
                    .border(1.dp, AppleCardBorder, RoundedCornerShape(24.dp))
                    .clickable(enabled = false) {}
                    .padding(horizontal = 26.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "הגדרות בלוטות'",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppleWhite
                    )

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AppleSubtleButtonBg)
                            .clickable(onClick = onDismiss)
                            .testTag("btn_close_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "סגור",
                            tint = AppleMutedGray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Feedback banner when Restart BT or Refresh Memory is tapped
                if (statusBannerHe != null) {
                    val isBlockedError = statusBannerHe.contains("נחסמה")
                    val bannerAccent = if (isBlockedError) AppleRed else AppleBlue
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(bannerAccent.copy(alpha = 0.14f))
                            .border(1.dp, bannerAccent.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (isRestartingBt) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = bannerAccent,
                                strokeWidth = 2.dp
                            )
                        }
                        Text(
                            text = statusBannerHe,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                            color = if (isBlockedError) AppleRed else AppleWhite
                        )
                    }
                }

                // Button 1: "צימוד טלפון חדש" (Pair New Phone)
                SettingsSheetActionRow(
                    title = "צימוד טלפון חדש",
                    icon = Icons.Default.AddLink,
                    accentColor = AppleBlue,
                    onClick = onPairNewPhoneClick,
                    testTag = "btn_settings_pair_new"
                )

                // Button 2: "איפוס בלוטות' מהיר" (Restart Bluetooth)
                SettingsSheetActionRow(
                    title = "איפוס בלוטות' מהיר",
                    icon = Icons.Default.RestartAlt,
                    accentColor = AppleGreen,
                    onClick = onRestartBluetoothClick,
                    testTag = "btn_settings_restart_bt"
                )

                // Button 3: "רענון זיכרון מערכת" (Refresh System)
                SettingsSheetActionRow(
                    title = "רענון זיכרון מערכת",
                    icon = Icons.Default.Memory,
                    accentColor = AppleWhite,
                    onClick = onRefreshSystemClick,
                    testTag = "btn_settings_refresh_system"
                )

                // Show hidden devices again if any are hidden
                if (hiddenDeviceCount > 0) {
                    SettingsSheetActionRow(
                        title = "הצג מכשירים מוסתרים (${'$'}hiddenDeviceCount)",
                        icon = Icons.Default.Visibility,
                        accentColor = AppleBlue,
                        onClick = onRestoreHiddenDevicesClick,
                        testTag = "btn_settings_restore_hidden"
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Discrete "ייצוא קוד APK (Export Android Project)" button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(OledBlack.copy(alpha = 0.55f))
                        .border(1.dp, AppleCardBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onOpenExportProjectClick)
                        .padding(horizontal = 16.dp)
                        .testTag("btn_settings_export_apk"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = AppleMutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ייצוא קוד APK (Export Android Project)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppleMutedGray
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSheetActionRow(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AppleCardElevated)
            .border(1.dp, AppleCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            ),
            color = AppleWhite
        )
    }
}"""
        ),
        ExportableProjectFile(
            id = "export_screen",
            title = "ExportProjectScreen.kt",
            relativePath = "app/src/main/java/com/example/ui/components/ExportProjectScreen.kt",
            languageBadge = "Kotlin",
            description = "Full Project Source Code & Read-Only Bluetooth Stack Diagnostics Viewer",
            content = """package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.export.ExportableProjectFile
import com.example.export.NativeExportCatalog
import com.example.ui.theme.AppleBlue
import com.example.ui.theme.AppleCardBorder
import com.example.ui.theme.AppleCardElevated
import com.example.ui.theme.AppleCardSurface
import com.example.ui.theme.AppleGreen
import com.example.ui.theme.AppleMutedGray
import com.example.ui.theme.AppleSubtleButtonBg
import com.example.ui.theme.AppleWhite
import com.example.ui.theme.OledBlack

@Composable
fun ExportProjectModalDialog(
    isOpen: Boolean,
    diagnosticLogs: List<String> = emptyList(),
    bluetoothStackDiagnostics: List<String> = emptyList(),
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val context = LocalContext.current
    val files = NativeExportCatalog.FILES
    var selectedFileId by remember { mutableStateOf(files.first().id) }
    var showDiagnosticLogTab by remember { mutableStateOf(false) }

    val selectedFile = files.firstOrNull { it.id == selectedFileId } ?: files.first()
    val stackDiagnosticsText = if (bluetoothStackDiagnostics.isEmpty()) {
        "Collecting Bluetooth stack diagnostics..."
    } else {
        bluetoothStackDiagnostics.joinToString("\n")
    }
    val liveLogText = if (diagnosticLogs.isEmpty()) {
        "No diagnostic events recorded yet."
    } else {
        diagnosticLogs.joinToString("\n")
    }
    val displayedContent = if (showDiagnosticLogTab) {
        buildString {
            appendLine("=== Bluetooth stack diagnostics (READ-ONLY) ===")
            appendLine(stackDiagnosticsText)
            appendLine("===============================================")
            appendLine()
            appendLine("=== Live Diagnostic Log (${'$'}{diagnosticLogs.size} / 1000 entries) ===")
            append(liveLogText)
        }
    } else {
        selectedFile.content
    }
    val displayedTitle = if (showDiagnosticLogTab) {
        "Live Diagnostic Log (${'$'}{diagnosticLogs.size} / 1000 entries)"
    } else {
        selectedFile.title
    }
    val displayedSubtitle = if (showDiagnosticLogTab) {
        "Includes READ-ONLY Bluetooth stack diagnostics + every Java Reflection call, 10s proxy callback check, and connection poll."
    } else {
        "${'$'}{selectedFile.description} • ${'$'}{selectedFile.content.lines().size} lines"
    }

    val saveDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(displayedContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    "Saved ${'$'}{if (showDiagnosticLogTab) "diagnostic_log.txt" else selectedFile.relativePath}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(OledBlack.copy(alpha = 0.85f))
                    .padding(24.dp)
                    .testTag("export_native_apk_modal"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(AppleCardSurface)
                        .border(1.dp, AppleCardBorder, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = null,
                                tint = AppleBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Export Native Android APK Project (${'$'}{files.size} Full Source Files + Diagnostic Log)",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = AppleWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(AppleSubtleButtonBg)
                                .clickable(onClick = onDismiss)
                                .testTag("btn_close_export_modal"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = AppleWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left Column: Scrollable File List + Live Diagnostic Log item
                        val listScrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .width(340.dp)
                                .fillMaxHeight()
                                .verticalScroll(listScrollState),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Diagnostic Log Selector
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (showDiagnosticLogTab) AppleGreen.copy(alpha = 0.18f)
                                        else AppleCardElevated
                                    )
                                    .border(
                                        width = if (showDiagnosticLogTab) 1.5.dp else 1.dp,
                                        color = if (showDiagnosticLogTab) AppleGreen else AppleCardBorder,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable { showDiagnosticLogTab = true }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("export_tab_diagnostic_log")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Terminal,
                                        contentDescription = null,
                                        tint = AppleGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Live Diagnostic Log (${'$'}{diagnosticLogs.size})",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AppleGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Reflection & Connection Poll Trace",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = AppleMutedGray
                                )
                            }

                            files.forEach { file ->
                                val isSelected = !showDiagnosticLogTab && file.id == selectedFile.id
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (isSelected) AppleBlue.copy(alpha = 0.18f)
                                            else AppleCardElevated
                                        )
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) AppleBlue else AppleCardBorder,
                                            shape = RoundedCornerShape(14.dp)
                                        )
                                        .clickable {
                                            showDiagnosticLogTab = false
                                            selectedFileId = file.id
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                        .testTag("export_file_${'$'}{file.id}")
                                ) {
                                    Text(
                                        text = file.relativePath,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) AppleBlue else AppleWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${'$'}{file.languageBadge} • ${'$'}{file.content.lines().size} lines",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppleMutedGray
                                    )
                                }
                            }
                        }

                        // Right Column: Code Viewer & Export Actions
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(16.dp))
                                .background(OledBlack)
                                .border(1.dp, AppleCardBorder, RoundedCornerShape(16.dp))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = displayedTitle,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AppleWhite
                                    )
                                    Text(
                                        text = displayedSubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = AppleMutedGray
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    ExportPillButton(
                                        label = "Copy",
                                        icon = Icons.Default.ContentCopy,
                                        accent = AppleBlue,
                                        onClick = {
                                            copyToClipboard(
                                                context,
                                                if (showDiagnosticLogTab) "diagnostic_log.txt" else selectedFile.relativePath,
                                                displayedContent
                                            )
                                        }
                                    )
                                    ExportPillButton(
                                        label = "Download",
                                        icon = Icons.Default.Download,
                                        accent = AppleGreen,
                                        onClick = {
                                            val fileName = if (showDiagnosticLogTab) {
                                                "diagnostic_log.txt"
                                            } else {
                                                selectedFile.relativePath.substringAfterLast('/')
                                            }
                                            saveDocumentLauncher.launch(fileName)
                                        }
                                    )
                                    ExportPillButton(
                                        label = "Share",
                                        icon = Icons.Default.Share,
                                        accent = AppleWhite,
                                        onClick = {
                                            shareTextContent(
                                                context,
                                                if (showDiagnosticLogTab) "diagnostic_log.txt" else selectedFile.relativePath,
                                                displayedContent
                                            )
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val vScroll = rememberScrollState()
                            val hScroll = rememberScrollState()

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0A0A0C))
                                    .padding(12.dp)
                            ) {
                                if (showDiagnosticLogTab) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(vScroll)
                                            .horizontalScroll(hScroll),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // READ-ONLY Bluetooth stack diagnostics section
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(AppleCardElevated)
                                                .border(1.dp, AppleGreen.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                                .padding(12.dp)
                                                .testTag("bluetooth_stack_diagnostics_section")
                                        ) {
                                            Text(
                                                text = "Bluetooth stack diagnostics (READ-ONLY)",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AppleGreen
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = stackDiagnosticsText,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFFE5F0FF)
                                            )
                                        }

                                        // Live Diagnostic Event Trace section
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color(0xFF0D0D10))
                                                .border(1.dp, AppleCardBorder, RoundedCornerShape(10.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "Live Event Trace (${'$'}{diagnosticLogs.size} / 1000 max)",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AppleBlue
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = liveLogText,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFFE5F0FF)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = displayedContent,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFFE5F0FF),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(vScroll)
                                            .horizontalScroll(hScroll)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportPillButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = AppleWhite
        )
    }
}

private fun copyToClipboard(context: Context, label: String, content: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, content))
    Toast.makeText(context, "Copied ${'$'}label", Toast.LENGTH_SHORT).show()
}

private fun shareTextContent(context: Context, subject: String, content: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export ${'$'}subject"))
}"""
        ),
        ExportableProjectFile(
            id = "theme_color",
            title = "Color.kt",
            relativePath = "app/src/main/java/com/example/ui/theme/Color.kt",
            languageBadge = "Kotlin",
            description = "OLED Black & Apple Accent Palette",
            content = """package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Apple-Inspired Minimalist OLED Dark Palette (1280x720 Car Head Unit)
val OledBlack = Color(0xFF000000)
val AppleCardSurface = Color(0xFF1C1C1E)
val AppleCardElevated = Color(0xFF242426)
val AppleCardBorder = Color(0xFF2C2C2E)
val AppleGlassOverlay = Color(0xE61C1C1E)

// Status & Action Accents
val AppleGreen = Color(0xFF30D158)
val AppleBlue = Color(0xFF0A84FF)
val AppleMutedGray = Color(0xFF8E8E93)
val AppleSubtleButtonBg = Color(0xFF2C2C2E)
val AppleRed = Color(0xFFFF453A)

// Text Hierarchy
val AppleWhite = Color(0xFFFFFFFF)
val AppleSecondaryLabel = Color(0xFFEBEBF5)"""
        ),
        ExportableProjectFile(
            id = "theme_type",
            title = "Type.kt",
            relativePath = "app/src/main/java/com/example/ui/theme/Type.kt",
            languageBadge = "Kotlin",
            description = "Heebo & JetBrains Mono Typography",
            content = """package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val HeeboFontFamily = FontFamily(
    Font(R.font.heebo, FontWeight.Normal),
    Font(R.font.heebo, FontWeight.Medium),
    Font(R.font.heebo, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Medium),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.3).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.2).sp
    ),
    titleLarge = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = HeeboFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = JetBrainsMonoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)"""
        ),
        ExportableProjectFile(
            id = "theme_main",
            title = "Theme.kt",
            relativePath = "app/src/main/java/com/example/ui/theme/Theme.kt",
            languageBadge = "Kotlin",
            description = "Material 3 Dark Car Theme",
            content = """package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppleDarkColorScheme = darkColorScheme(
    primary = AppleBlue,
    onPrimary = AppleWhite,
    primaryContainer = Color(0xFF003870),
    onPrimaryContainer = AppleWhite,
    secondary = AppleGreen,
    onSecondary = Color(0xFF00210B),
    secondaryContainer = Color(0xFF113D1E),
    onSecondaryContainer = AppleWhite,
    background = OledBlack,
    onBackground = AppleWhite,
    surface = AppleCardSurface,
    onSurface = AppleWhite,
    surfaceVariant = AppleCardElevated,
    onSurfaceVariant = AppleMutedGray,
    outline = AppleCardBorder,
    error = AppleRed,
    onError = AppleWhite
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppleDarkColorScheme,
        typography = Typography,
        content = content
    )
}"""
        ),
        ExportableProjectFile(
            id = "android_manifest",
            title = "AndroidManifest.xml",
            relativePath = "app/src/main/AndroidManifest.xml",
            languageBadge = "XML",
            description = "Bluetooth Permissions, Package Queries & Landscape Car Launcher",
            content = """<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Legacy Bluetooth Permissions (Android 10-11 & Car Head Unit MCU stacks) -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

    <!-- Android 12-13+ Runtime Bluetooth Permissions -->
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission
        android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation"
        tools:targetApi="s" />

    <!-- Background Memory Guard: Kill stuck Bluetooth & ZLink bridge tasks -->
    <uses-permission android:name="android.permission.KILL_BACKGROUND_PROCESSES" />

    <uses-feature
        android:name="android.hardware.bluetooth"
        android:required="false" />

    <!-- Package visibility for Android 11+ so killBackgroundProcesses can target stuck car BT/bridge daemons -->
    <queries>
        <package android:name="com.ts.bt" />
        <package android:name="com.asr.bt" />
        <package android:name="com.zjinnova.zlink" />
        <package android:name="com.autochips.bluetooth" />
        <package android:name="com.syu.bt" />
        <package android:name="com.my.bt" />
        <package android:name="com.android.bluetooth" />
    </queries>

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.MyApplication">
        <activity
            android:name=".MainActivity"
            android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|uiMode"
            android:exported="true"
            android:label="@string/app_name"
            android:launchMode="singleTop"
            android:screenOrientation="sensorLandscape"
            android:theme="@style/Theme.MyApplication">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.CAR_LAUNCHER" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
        </activity>
    </application>

</manifest>"""
        ),
        ExportableProjectFile(
            id = "app_build_gradle",
            title = "app/build.gradle.kts",
            relativePath = "app/build.gradle.kts",
            languageBadge = "Kotlin DSL",
            description = "App Module Gradle Configuration",
            content = """import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.larkbtswitcher.xkrvqp"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  // Do not remove or modify these signingConfigs. They are necessary for building, installing,
  // and updating Android apps in AI Studio.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${'$'}{rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${'$'}{rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  // implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  // Uncomment to use Firestore:
  // implementation(libs.firebase.firestore)

  // Uncomment ALL FOUR of the following dependencies together to use Firebase Auth and Google
  // Sign-In via Credential Manager:
  // implementation(libs.firebase.auth)
  // implementation(libs.androidx.credentials)
  // implementation(libs.androidx.credentials.play.services)
  // implementation(libs.googleid)
  // implementation(libs.firebase.appcheck.recaptcha)
  // implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  // "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}"""
        ),
        ExportableProjectFile(
            id = "settings_gradle",
            title = "settings.gradle.kts",
            relativePath = "settings.gradle.kts",
            languageBadge = "Kotlin DSL",
            description = "Root Project Gradle Settings",
            content = """pluginManagement {
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

dependencyResolutionManagement {
  repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
  repositories {
    google()
    mavenCentral()
  }
}

rootProject.name = "LarkBT Switcher"

include(":app")"""
        ),
        ExportableProjectFile(
            id = "github_workflow",
            title = "build-apk.yml",
            relativePath = ".github/workflows/build-apk.yml",
            languageBadge = "YAML",
            description = "GitHub Actions Automated APK Build Workflow",
            content = """name: Build LarkBT Switcher APK (ASR8601 lark_evb)

on:
  push:
    branches: [ "main", "master", "fix/workflow-build-apk" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build-apk:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Source
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3
        with:
          gradle-version: '9.3.1'

      - name: Generate Debug Keystore if missing
        run: |
          if [ ! -f debug.keystore ]; then
            keytool -genkey -v -keystore debug.keystore -storepass android -alias androiddebugkey -keypass android -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
          fi

      - name: Build Debug APK for lark_evb Head Unit
        run: gradle assembleDebug --stacktrace

      - name: Upload Ready-to-Install Car Head Unit APK
        uses: actions/upload-artifact@v4
        with:
          name: LarkBT-Switcher-ASR8601-debug
          path: app/build/outputs/apk/debug/*.apk"""
        )
    )
}
