package com.example.viewmodel

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
        appendDiagnosticLog("Hidden device card: ${phone.name} (${phone.id}) | Total hidden: ${updated.size}")
        syncPairedPhones(force = true)
    }

    fun restoreAllHiddenDevices() {
        val count = getHiddenDeviceIds().size
        sharedPrefs.edit().remove("hidden_device_ids").apply()
        appendDiagnosticLog("Restored $count hidden devices")
        _uiState.update {
            it.copy(settingsStatusBannerHe = "כל המכשירים המוסתרים שוחזרו")
        }
        syncPairedPhones(force = true)
    }

    private fun appendDiagnosticLog(message: String) {
        val line = "[${timeFormat.format(Date())}] $message"
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
            appendDiagnosticLog("ACTION_BLUETOOTH_SETTINGS failed: ${e.javaClass.simpleName}: ${e.message}")
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
            appendDiagnosticLog("Manual disconnect requested for ${phone.name} (${phone.id})")
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
                appendDiagnosticLog("=== Smart Switch START -> Target: ${targetPhone.name} (${targetPhone.id}) ===")

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
                appendDiagnosticLog("Smart Switch Step 3: Sequential connect for ${targetPhone.name}")
                _uiState.update {
                    it.copy(
                        smartSwitchModalState = SmartSwitchModalState.Switching(
                            targetPhoneName = targetPhone.name,
                            step = 3,
                            progressPercent = 90,
                            statusTextHe = "מתחבר אל ${targetPhone.name}...",
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
                    appendDiagnosticLog("Poll +${elapsedPollMs}ms [${targetPhone.id}]: connected=$connectedNow | $breakdown")
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
                    appendDiagnosticLog("Smart Switch SUCCEEDED for ${targetPhone.name}: $lastStateSummary")
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
                        "profile(${it.key})->connect()=${it.value}"
                    }
                    appendDiagnosticLog(
                        "Smart Switch FAILED for ${targetPhone.name} after ${maxPollMs}ms | Tried: [$triedSummary] | Final states: [$lastStateSummary]"
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
}
