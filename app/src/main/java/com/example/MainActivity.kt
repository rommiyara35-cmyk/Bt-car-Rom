package com.example

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
}
