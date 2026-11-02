package com.example.ui.screens

import android.content.Intent
import androidx.core.content.FileProvider
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AnimeViewModel
import com.example.ui.ExportUiState
import com.example.ui.theme.EmeraldSuccess
import java.io.File

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp

@Composable
fun ArchivesScreen(
    viewModel: AnimeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val animeList by viewModel.animeList.collectAsState()
    val exportState by viewModel.exportUiState.collectAsState()
    val tokenUsageState by viewModel.tokenUsageState.collectAsState()
    val selectedApiModel by viewModel.selectedApiModel.collectAsState()

    val totalTracked = animeList.size
    val totalEpisodesWatched = animeList.sumOf { it.episodesWatched }
    val totalMinutesWatched = totalEpisodesWatched * 24L
    val hoursWatched = totalMinutesWatched / 60
    val minsWatched = totalMinutesWatched % 60

    val completedCount = animeList.count { it.watchStatus.equals("Completed", ignoreCase = true) }
    val watchingCount = animeList.count { it.watchStatus.equals("Watching", ignoreCase = true) }
    val planToWatchCount = animeList.count { it.watchStatus.equals("Plan to Watch", ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PROGRESS ANALYTICS & ARCHIVES",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Generate personal records as official PDF documents or summary archive files for offline long-term record keeping.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Bento Grid Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Purple Bento Card: Total Watched Time
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("TOTAL WATCH TIME", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF21005D))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${hoursWatched}h ${minsWatched}m", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF21005D))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$totalEpisodesWatched total episodes", style = MaterialTheme.typography.labelSmall, color = Color(0xFF21005D).copy(alpha = 0.8f))
                    }
                }

                // Blue Bento Card: Total Anime Count
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD3E4FF)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("ANIME TRACKED", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF001D35))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("$totalTracked", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color(0xFF001D35))
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("$completedCount completed, $watchingCount watching", style = MaterialTheme.typography.labelSmall, color = Color(0xFF001D35).copy(alpha = 0.8f))
                    }
                }
            }
        }

        // Graph Charts Section
        item {
            AnimeWatchStatsChartCard(
                animeList = animeList,
                tokenUsageState = tokenUsageState,
                selectedApiModel = selectedApiModel
            )
        }

        // Export PDF Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("PDF Progress Report", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Generates an official PDF document with full watch history, status breakdown, score tables, and timestamp for your personal records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { viewModel.generatePdfReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = CircleShape,
                        enabled = exportState !is ExportUiState.Exporting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_pdf_report_button")
                    ) {
                        if (exportState is ExportUiState.Exporting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generating PDF...")
                        } else {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate PDF Progress Document", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Export JSON & Markdown Archives
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Summary Archives & Local Backups", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Export your anime data as JSON raw backup files or Markdown summary documents for easy offline viewing and long-term personal archives.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportMarkdownArchive() },
                            enabled = exportState !is ExportUiState.Exporting,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_markdown_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Markdown (.md)", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.exportJsonArchive() },
                            enabled = exportState !is ExportUiState.Exporting,
                            shape = CircleShape,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("export_json_button")
                        ) {
                            Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("JSON (.json)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Export Success Status Display Card
        if (exportState is ExportUiState.Success) {
            val success = exportState as ExportUiState.Success
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = EmeraldSuccess.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Archive File Ready!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(success.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Location: ${success.file.name}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // View / Open File Intent
                            Button(
                                onClick = {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            success.file
                                        )
                                        val mimeType = when (success.type) {
                                            "PDF" -> "application/pdf"
                                            "JSON" -> "application/json"
                                            else -> "text/plain"
                                        }
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(uri, mimeType)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Open File"))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Open File", color = Color.White, fontWeight = FontWeight.Bold)
                            }

                            // Share Intent
                            OutlinedButton(
                                onClick = {
                                    try {
                                        val uri = FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            success.file
                                        )
                                        val intent = Intent(Intent.ACTION_SEND).apply {
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            type = "application/*"
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(intent, "Share Archive"))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimeWatchStatsChartCard(
    animeList: List<com.example.data.model.AnimeEntity>,
    tokenUsageState: com.example.data.local.TokenUsageState,
    selectedApiModel: String
) {
    val total = animeList.size.coerceAtLeast(1)
    val watching = animeList.count { it.watchStatus.equals("Watching", ignoreCase = true) }
    val completed = animeList.count { it.watchStatus.equals("Completed", ignoreCase = true) }
    val planToWatch = animeList.count { it.watchStatus.equals("Plan to Watch", ignoreCase = true) }
    val onHold = animeList.count { it.watchStatus.equals("On Hold", ignoreCase = true) }
    val dropped = animeList.count { it.watchStatus.equals("Dropped", ignoreCase = true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watch_stats_chart_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ANIME LIBRARY GRAPH CHARTS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Text(
                        text = "⚡ $selectedApiModel",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Stacked Segmented Distribution Bar
            Text(
                text = "Watch Status Breakdown",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                val watchingWeight = watching.toFloat() / total
                val completedWeight = completed.toFloat() / total
                val planWeight = planToWatch.toFloat() / total
                val onHoldWeight = onHold.toFloat() / total
                val droppedWeight = dropped.toFloat() / total

                if (watchingWeight > 0) Box(modifier = Modifier.weight(watchingWeight).fillMaxHeight().background(Color(0xFF8B5CF6)))
                if (completedWeight > 0) Box(modifier = Modifier.weight(completedWeight).fillMaxHeight().background(Color(0xFF10B981)))
                if (planWeight > 0) Box(modifier = Modifier.weight(planWeight).fillMaxHeight().background(Color(0xFF3B82F6)))
                if (onHoldWeight > 0) Box(modifier = Modifier.weight(onHoldWeight).fillMaxHeight().background(Color(0xFFF59E0B)))
                if (droppedWeight > 0) Box(modifier = Modifier.weight(droppedWeight).fillMaxHeight().background(Color(0xFFEF4444)))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend / Bar Details
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ChartStatusItem("Watching", watching, total, Color(0xFF8B5CF6))
                ChartStatusItem("Completed", completed, total, Color(0xFF10B981))
                ChartStatusItem("Plan to Watch", planToWatch, total, Color(0xFF3B82F6))
                if (onHold > 0) ChartStatusItem("On Hold", onHold, total, Color(0xFFF59E0B))
                if (dropped > 0) ChartStatusItem("Dropped", dropped, total, Color(0xFFEF4444))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            // 2. API Quota & Usage Chart
            Text(
                text = "Daily Gemini AI API Usage & Quota Meter",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Requests Today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${tokenUsageState.requestsCount} / 50 requests used",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val requestsFraction = (tokenUsageState.requestsCount.toFloat() / 50f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { requestsFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = if (requestsFraction > 0.8f) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun ChartStatusItem(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count * 100) / total else 0
    val fraction = if (total > 0) count.toFloat() / total else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
            }
            Text("$count ($percentage%)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = color,
            trackColor = MaterialTheme.colorScheme.surface
        )
    }
}
