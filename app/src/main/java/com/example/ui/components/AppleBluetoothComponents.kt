package com.example.ui.components

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
                    text = "הצג מכשירים מוסתרים ($hiddenDeviceCount)",
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
            .testTag("phone_card_${phone.id}")
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
                .testTag("btn_hide_device_${phone.id}"),
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
                            .testTag("btn_disconnect_${phone.id}"),
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
                            .testTag("btn_connect_${phone.id}"),
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
                                        text = "${state.progressPercent}%",
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
                        title = "הצג מכשירים מוסתרים ($hiddenDeviceCount)",
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
}
