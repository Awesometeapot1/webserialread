package com.example.webserialread.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webserialread.data.local.entity.ChapterEntity
import com.example.webserialread.ui.viewmodel.ChapterListViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    serialId: Long,
    onChapterClick: (Long) -> Unit,
    onBack: () -> Unit,
    vm: ChapterListViewModel = viewModel()
) {
    LaunchedEffect(serialId) { vm.init(serialId) }

    val serial          by vm.serial.collectAsState()
    val chapters        by vm.chapters.collectAsState()
    val syncing         by vm.syncing.collectAsState()
    val syncError       by vm.syncError.collectAsState()
    val downloadProgress by vm.downloadProgress.collectAsState()
    val isDownloading   = downloadProgress != null

    val coverBg = coverColor(serial?.title ?: "")
    val firstUnreadId = remember(chapters) {
        chapters.firstOrNull { !it.isRead }?.id ?: chapters.firstOrNull()?.id
    }
    val downloadedCount = remember(chapters) { chapters.count { it.content != null } }

    Scaffold { padding ->
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = { vm.sync() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // ── Hero header ──────────────────────────────────────────────
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        // Cover image or colour placeholder
                        Box(
                            modifier = Modifier.fillMaxSize().background(coverBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (serial?.siteIconUrl != null) {
                                AsyncImage(
                                    model = serial!!.siteIconUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text(
                                    text = (serial?.title ?: "").take(2).uppercase(),
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.2f)
                                )
                            }
                        }

                        // Gradient overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Black.copy(0.3f), Color.Black.copy(0.75f))
                                    )
                                )
                        )

                        // Back button
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        // Title + chapter count at bottom
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = serial?.title ?: "",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "${chapters.size} chapters  •  $downloadedCount downloaded",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // ── Action buttons ───────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { firstUnreadId?.let(onChapterClick) },
                            enabled = firstUnreadId != null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Read Now")
                        }

                        if (isDownloading) {
                            OutlinedButton(
                                onClick = { vm.cancelDownload() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Cancel")
                            }
                        } else {
                            OutlinedButton(
                                onClick = { vm.downloadAll() },
                                enabled = !syncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Download")
                            }
                        }

                        IconButton(
                            onClick = { vm.sync() },
                            enabled = !syncing && !isDownloading
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync")
                        }
                    }
                }

                // ── Download progress ────────────────────────────────────────
                if (isDownloading) {
                    item {
                        val (done, total) = downloadProgress!!
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                            LinearProgressIndicator(
                                progress = { done.toFloat() / total.coerceAtLeast(1) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Downloading $done / $total chapters…",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // ── Description ──────────────────────────────────────────────
                val desc = serial?.description?.takeIf { it.isNotBlank() }
                if (desc != null) {
                    item {
                        var expanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .clickable { expanded = !expanded }
                        ) {
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (expanded) Int.MAX_VALUE else 3,
                                overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (expanded) "Show less" else "Show more",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // ── Chapters header ──────────────────────────────────────────
                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        "Chapters",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Empty state ──────────────────────────────────────────────
                if (chapters.isEmpty() && !syncing) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("No chapters found", style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Pull down or tap sync to load",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // ── Chapter rows ─────────────────────────────────────────────
                items(chapters, key = { it.id }) { chapter ->
                    ChapterRow(chapter = chapter, onClick = { onChapterClick(chapter.id) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // Bottom padding
                item { Spacer(Modifier.height(24.dp)) }
            }

            // Sync error snackbar
            if (syncError != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                    Snackbar(
                        action = { TextButton(onClick = { vm.clearSyncError() }) { Text("Dismiss") } },
                        modifier = Modifier.padding(16.dp)
                    ) { Text("Sync error: $syncError") }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: ChapterEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Read indicator strip
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    if (chapter.isRead) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary
                )
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                chapter.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (chapter.isRead) FontWeight.Normal else FontWeight.Medium,
                color = if (chapter.isRead)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val date = formatDate(chapter.publishedAt)
                if (date.isNotBlank()) {
                    Text(
                        date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (chapter.content != null) {
                    if (date.isNotBlank()) Text(
                        " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "Downloaded",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
        if (chapter.isRead) {
            Spacer(Modifier.width(8.dp))
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Read",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatDate(epochMs: Long): String {
    if (epochMs == 0L) return ""
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(epochMs))
}
