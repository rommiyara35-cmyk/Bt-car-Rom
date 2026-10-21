package com.example.ui.components

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
            appendLine("=== Live Diagnostic Log (${diagnosticLogs.size} / 1000 entries) ===")
            append(liveLogText)
        }
    } else {
        selectedFile.content
    }
    val displayedTitle = if (showDiagnosticLogTab) {
        "Live Diagnostic Log (${diagnosticLogs.size} / 1000 entries)"
    } else {
        selectedFile.title
    }
    val displayedSubtitle = if (showDiagnosticLogTab) {
        "Includes READ-ONLY Bluetooth stack diagnostics + every Java Reflection call, 10s proxy callback check, and connection poll."
    } else {
        "${selectedFile.description} • ${selectedFile.content.lines().size} lines"
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
                    "Saved ${if (showDiagnosticLogTab) "diagnostic_log.txt" else selectedFile.relativePath}",
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
                                text = "Export Native Android APK Project (${files.size} Full Source Files + Diagnostic Log)",
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
                                        text = "Live Diagnostic Log (${diagnosticLogs.size})",
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
                                        .testTag("export_file_${file.id}")
                                ) {
                                    Text(
                                        text = file.relativePath,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) AppleBlue else AppleWhite
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "${file.languageBadge} • ${file.content.lines().size} lines",
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
                                                text = "Live Event Trace (${diagnosticLogs.size} / 1000 max)",
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
    Toast.makeText(context, "Copied $label", Toast.LENGTH_SHORT).show()
}

private fun shareTextContent(context: Context, subject: String, content: String) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, content)
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export $subject"))
}
