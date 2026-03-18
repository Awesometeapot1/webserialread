package com.example.webserialread.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

    val serial by vm.serial.collectAsState()
    val chapters by vm.chapters.collectAsState()
    val syncing by vm.syncing.collectAsState()
    val syncError by vm.syncError.collectAsState()
    val downloadProgress by vm.downloadProgress.collectAsState()
    val isDownloading = downloadProgress != null

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            serial?.title ?: "Chapters",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        if (isDownloading) {
                            IconButton(onClick = { vm.cancelDownload() }) {
                                Icon(Icons.Default.Close, "Cancel download")
                            }
                        } else {
                            IconButton(onClick = { vm.downloadAll() }, enabled = !syncing) {
                                Icon(Icons.Default.Download, "Download all for offline")
                            }
                        }
                        IconButton(onClick = { vm.sync() }, enabled = !syncing && !isDownloading) {
                            Icon(Icons.Default.Refresh, "Sync")
                        }
                    }
                )
                // Download progress bar
                if (isDownloading) {
                    val (done, total) = downloadProgress!!
                    LinearProgressIndicator(
                        progress = { done.toFloat() / total.coerceAtLeast(1) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        "Downloading $done / $total chapters…",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = syncing,
            onRefresh = { vm.sync() },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            if (chapters.isEmpty() && !syncing) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(chapters, key = { it.id }) { chapter ->
                        ChapterRow(chapter = chapter, onClick = { onChapterClick(chapter.id) })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }

        if (syncError != null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.BottomCenter) {
                Snackbar(
                    action = { TextButton(onClick = { vm.clearSyncError() }) { Text("Dismiss") } },
                    modifier = Modifier.padding(16.dp)
                ) { Text("Sync error: $syncError") }
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
                Text(
                    formatDate(chapter.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (chapter.content != null) {
                    Text(
                        " • Downloaded",
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
