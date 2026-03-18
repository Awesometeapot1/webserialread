package com.example.webserialread.ui.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.webserialread.data.local.entity.SerialEntity
import com.example.webserialread.data.remote.KNOWN_SERIALS
import com.example.webserialread.ui.viewmodel.LibraryViewModel
import com.example.webserialread.ui.viewmodel.SerialItem


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    onSerialClick: (Long) -> Unit,
    vm: LibraryViewModel = viewModel()
) {
    val items by vm.serialItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showBrowseDialog by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<SerialEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showBrowseDialog = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Browse serials")
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add serial")
                    }
                }
            )
        }
    ) { padding ->
        if (items.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No serials yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Tap + to add a WordPress serial",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items, key = { it.serial.id }) { item ->
                    SerialGridItem(
                        item = item,
                        onClick = { onSerialClick(item.serial.id) },
                        onLongClick = { toDelete = item.serial }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSerialDialog(vm = vm, onDismiss = { showAddDialog = false; vm.resetAddState() })
    }

    if (showBrowseDialog) {
        BrowseDialog(vm = vm, onDismiss = { showBrowseDialog = false; vm.resetAddState() })
    }

    toDelete?.let { serial ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Remove serial") },
            text = { Text("Remove \"${serial.title}\" and all its downloaded chapters?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteSerial(serial); toDelete = null }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SerialGridItem(item: SerialItem, onClick: () -> Unit, onLongClick: () -> Unit) {
    val bg = coverColor(item.serial.title)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.65f)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        // Colored cover background
        Box(
            Modifier
                .fillMaxSize()
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = item.serial.title.take(2).uppercase(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.25f)
            )
        }

        // Gradient overlay + title at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
                .padding(8.dp)
        ) {
            Text(
                text = item.serial.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }

        // Unread badge
        if (item.unreadCount > 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary
            ) {
                Text(
                    text = if (item.unreadCount > 999) "999+" else item.unreadCount.toString(),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddSerialDialog(vm: LibraryViewModel, onDismiss: () -> Unit) {
    var url by remember { mutableStateOf("") }
    var tocUrl by remember { mutableStateOf("") }
    val addState by vm.addState.collectAsState()

    LaunchedEffect(addState) {
        if (addState is LibraryViewModel.AddState.Success) onDismiss()
    }

    AlertDialog(
        onDismissRequest = { if (addState !is LibraryViewModel.AddState.Loading) onDismiss() },
        title = { Text("Add web serial") },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Site URL") },
                    placeholder = { Text("e.g. katalepsis.net") },
                    singleLine = true,
                    enabled = addState !is LibraryViewModel.AddState.Loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = tocUrl,
                    onValueChange = { tocUrl = it },
                    label = { Text("Table of Contents URL (optional)") },
                    placeholder = { Text("e.g. katalepsis.net/table-of-contents/") },
                    singleLine = true,
                    enabled = addState !is LibraryViewModel.AddState.Loading,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (url.isNotBlank()) vm.addSerial(url, tocUrl) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "If the site blocks the WordPress API, paste its chapter list URL above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (addState is LibraryViewModel.AddState.Loading) {
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Fetching serial…", style = MaterialTheme.typography.bodySmall)
                    }
                }
                val errorState = addState as? LibraryViewModel.AddState.Error
                if (errorState != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { vm.addSerial(url, tocUrl) },
                enabled = url.isNotBlank() && addState !is LibraryViewModel.AddState.Loading
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = addState !is LibraryViewModel.AddState.Loading
            ) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BrowseDialog(vm: LibraryViewModel, onDismiss: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val addState by vm.addState.collectAsState()
    var addingTitle by remember { mutableStateOf("") }

    // Clear any leftover error from a previous add attempt
    LaunchedEffect(Unit) { vm.resetAddState() }

    LaunchedEffect(addState) {
        if (addState is LibraryViewModel.AddState.Success) onDismiss()
    }

    val filtered = remember(query) {
        if (query.isBlank()) KNOWN_SERIALS
        else KNOWN_SERIALS.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.author.contains(query, ignoreCase = true) ||
            it.genre.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = { if (addState !is LibraryViewModel.AddState.Loading) onDismiss() }) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Browse Serials",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = onDismiss,
                        enabled = addState !is LibraryViewModel.AddState.Loading
                    ) { Text("Close") }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search by title, author, genre…") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                if (addState is LibraryViewModel.AddState.Loading) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Adding \"$addingTitle\"…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val errorState = addState as? LibraryViewModel.AddState.Error
                if (errorState != null) {
                    Text(
                        errorState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filtered) { serial ->
                        ListItem(
                            headlineContent = {
                                Text(serial.title, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Column {
                                    Text(
                                        "${serial.author} · ${serial.genre}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        serial.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            trailingContent = {
                                FilledTonalButton(
                                    onClick = {
                                        addingTitle = serial.title
                                        vm.addKnownSerial(serial)
                                    },
                                    enabled = addState !is LibraryViewModel.AddState.Loading
                                ) { Text("Add") }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
