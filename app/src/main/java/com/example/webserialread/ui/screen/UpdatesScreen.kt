package com.example.webserialread.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webserialread.ui.viewmodel.SerialItem
import com.example.webserialread.ui.viewmodel.UpdatesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdatesScreen(
    onSerialClick: (Long) -> Unit,
    vm: UpdatesViewModel = viewModel()
) {
    val updates by vm.updates.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Updates", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        if (updates.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("All caught up!", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "No unread chapters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(updates, key = { it.serial.id }) { item ->
                    UpdateRow(item = item, onClick = { onSerialClick(item.serial.id) })
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}

@Composable
private fun UpdateRow(item: SerialItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Color swatch matching the library grid cover
        Surface(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.medium,
            color = coverColor(item.serial.title)
        ) {}
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.serial.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${item.unreadCount} unread chapter${if (item.unreadCount != 1) "s" else ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
