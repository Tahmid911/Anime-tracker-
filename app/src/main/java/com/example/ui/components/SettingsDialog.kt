package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TokenUsageState

@Composable
fun SettingsDialog(
    currentThemeMode: String,
    customApiKey: String,
    selectedApiModel: String = "gemini-2.5-flash",
    tokenUsageState: TokenUsageState,
    onThemeChange: (String) -> Unit,
    onSaveApiKey: (String) -> Unit,
    onApiModelChange: (String) -> Unit = {},
    onResetTokenUsage: () -> Unit,
    onAutoFetchTitleCards: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var apiKeyInput by remember { mutableStateOf(customApiKey) }
    var showTutorialDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "App Settings & AI Model",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Model selection, API keys & theme",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 0: Gemini API Model Selector (Token Saver)
                Text(
                    text = "GEMINI AI MODEL (TOKEN SAVER)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Switch model to save API tokens & stay under free daily limits:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Model Option 1: gemini-2.5-flash
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApiModelChange("gemini-2.5-flash") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedApiModel == "gemini-2.5-flash",
                                onClick = { onApiModelChange("gemini-2.5-flash") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("gemini-2.5-flash", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFD1FAE5), shape = CircleShape) {
                                        Text("⚡ Recommended / Low Token", color = Color(0xFF065F46), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Fastest response, minimal token usage (50+ searches/day)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Model Option 2: gemini-3.5-flash
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApiModelChange("gemini-3.5-flash") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedApiModel == "gemini-3.5-flash",
                                onClick = { onApiModelChange("gemini-3.5-flash") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("gemini-3.5-flash", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFE0E7FF), shape = CircleShape) {
                                        Text("🚀 High Performance", color = Color(0xFF3730A3), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("High reasoning speed & rich anime details", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Model Option 3: gemini-3.1-flash-lite-preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApiModelChange("gemini-3.1-flash-lite-preview") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedApiModel == "gemini-3.1-flash-lite-preview",
                                onClick = { onApiModelChange("gemini-3.1-flash-lite-preview") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("gemini-3.1-flash-lite", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFFEF3C7), shape = CircleShape) {
                                        Text("💡 Ultra Light", color = Color(0xFF92400E), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Lightweight model for maximum daily request quota", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Model Option 4: gemini-3.1-pro-preview
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApiModelChange("gemini-3.1-pro-preview") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedApiModel == "gemini-3.1-pro-preview",
                                onClick = { onApiModelChange("gemini-3.1-pro-preview") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("gemini-3.1-pro", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(color = Color(0xFFF3E8FF), shape = CircleShape) {
                                        Text("🧠 Pro Advanced", color = Color(0xFF6B21A8), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Maximum accuracy for complex photo scene recognition", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 1: Dark UI & Theme Mode
                Text(
                    text = "THEME MODE (DARK UI)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange("SYSTEM") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentThemeMode == "SYSTEM",
                                onClick = { onThemeChange("SYSTEM") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("System Default", fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange("LIGHT") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentThemeMode == "LIGHT",
                                onClick = { onThemeChange("LIGHT") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFF59E0B))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Light Bento Mode", fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onThemeChange("DARK") }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = currentThemeMode == "DARK",
                                onClick = { onThemeChange("DARK") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFA855F7))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Dark Cyber/Bento Mode 🌙", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 2: Gemini API Key Management
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CUSTOM GEMINI API KEY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Surface(
                        color = Color(0xFFEADDFF),
                        shape = CircleShape,
                        modifier = Modifier.clickable { showTutorialDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF21005D))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("How to Get Key?", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = { apiKeyInput = it },
                    label = { Text("Paste your Gemini API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    trailingIcon = {
                        Row {
                            TextButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData = clipboard.primaryClip
                                if (clipData != null && clipData.itemCount > 0) {
                                    apiKeyInput = clipData.getItemAt(0).text.toString().trim()
                                    Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Icon(Icons.Default.ContentPaste, contentDescription = "Paste", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onSaveApiKey(apiKeyInput)
                            Toast.makeText(context, "API Key Saved!", Toast.LENGTH_SHORT).show()
                        },
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Save Key", fontWeight = FontWeight.Bold)
                    }

                    if (customApiKey.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                apiKeyInput = ""
                                onSaveApiKey("")
                                Toast.makeText(context, "Cleared custom API key.", Toast.LENGTH_SHORT).show()
                            },
                            shape = CircleShape
                        ) {
                            Text("Reset Default")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 3: Token Usage & Meter Statistics
                Text(
                    text = "API TOKEN METER & QUOTA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (tokenUsageState.isWarning) Color(0xFFFFF3E0) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Estimated Tokens Used Today",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (tokenUsageState.isWarning) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tokenUsageState.usedTokens} / ${tokenUsageState.maxDailyTokens}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (tokenUsageState.isWarning) Color(0xFFB45309) else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { tokenUsageState.usagePercentage },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(CircleShape),
                            color = when {
                                tokenUsageState.isDepleted -> Color(0xFFDC2626)
                                tokenUsageState.isWarning -> Color(0xFFF59E0B)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${tokenUsageState.remainingTokens} tokens remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${tokenUsageState.requestsCount} requests",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Warning banner if tokens are near end
                        if (tokenUsageState.isWarning || tokenUsageState.isDepleted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = if (tokenUsageState.isDepleted) Color(0xFFFEE2E2) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (tokenUsageState.isDepleted) Color(0xFFDC2626) else Color(0xFFD97706),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (tokenUsageState.isDepleted)
                                            "Tokens depleted! Paste your custom API key above or wait for daily reset."
                                        else
                                            "Tokens running low (>80% used)! Consider pasting a custom API key.",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (tokenUsageState.isDepleted) Color(0xFF991B1B) else Color(0xFF92400E)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 4: Auto-Attach Title Cards & Official Posters
                Text(
                    text = "OFFICIAL TITLE CARDS & POSTERS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Auto-Download Missing Title Cards",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Automatically fetches and attaches high-res canonical anime posters from MyAnimeList & Kitsu for all items in your watchlist.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                onAutoFetchTitleCards()
                                Toast.makeText(context, "Auto-attaching title cards in background!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = CircleShape,
                            modifier = Modifier.fillMaxWidth().testTag("auto_fetch_title_cards_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Auto-Attach Real Title Cards Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Section 5: Google Drive & APK Download Guide
                Text(
                    text = "DOWNLOAD APK & GOOGLE DRIVE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "📲 How to Get APK on Phone Storage:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Click the 'Download APK' / 'Export' icon in the top right menu of the AI Studio top bar.\n" +
                                    "2. The compiled .apk file will save directly into your phone's 'Download' folder.\n" +
                                    "3. Tap the file in Downloads to install or test on your Android phone!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "☁️ How to Save APK to Google Drive:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "1. Open Google Drive on your phone (or tap 'Open Drive' below).\n" +
                                    "2. Tap the '+' button -> Upload -> select the downloaded .apk from Downloads.\n" +
                                    "3. You can now copy and share the Google Drive link with anyone!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Download Anime Tracker App")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Check out Anime Tracker built on Google AI Studio!"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share App Link / Save to Drive"))
                                },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).testTag("share_app_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share Link", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val driveIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/my-drive"))
                                    context.startActivity(driveIntent)
                                },
                                shape = CircleShape,
                                modifier = Modifier.weight(1f).testTag("open_gdrive_button")
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open Drive", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showTutorialDialog) {
        ApiKeyTutorialDialog(onDismiss = { showTutorialDialog = false })
    }
}

@Composable
fun ApiKeyTutorialDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val studioUrl = "https://aistudio.google.com/app/apikey"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to Get Free Gemini API Key", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = "Follow these 4 simple steps to claim a 100% free Gemini API key from Google AI Studio:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))

                TutorialStepCard(
                    stepNumber = 1,
                    title = "Visit Google AI Studio",
                    description = "Open aistudio.google.com in your web browser and sign in with your Google account."
                )

                Spacer(modifier = Modifier.height(8.dp))

                TutorialStepCard(
                    stepNumber = 2,
                    title = "Click 'Get API Key'",
                    description = "On the top navigation bar or dashboard, click the 'Get API key' button."
                )

                Spacer(modifier = Modifier.height(8.dp))

                TutorialStepCard(
                    stepNumber = 3,
                    title = "Create API Key in Project",
                    description = "Click 'Create API key in new project'. Google will generate a key starting with 'AIzaSy...'."
                )

                Spacer(modifier = Modifier.height(8.dp))

                TutorialStepCard(
                    stepNumber = 4,
                    title = "Copy & Paste into App",
                    description = "Copy the generated API key string and paste it into the Settings box in this app!"
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Link button
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(studioUrl))
                            context.startActivity(intent)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Open Google AI Studio in Browser",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = CircleShape) {
                Text("Got It!")
            }
        }
    )
}

@Composable
fun TutorialStepCard(stepNumber: Int, title: String, description: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
