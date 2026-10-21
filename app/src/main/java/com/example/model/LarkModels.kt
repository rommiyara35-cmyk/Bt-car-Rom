package com.example.model

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
}
