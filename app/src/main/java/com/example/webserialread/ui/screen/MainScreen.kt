package com.example.webserialread.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

private enum class Tab(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Library("Library", Icons.Filled.LibraryBooks, Icons.Outlined.LibraryBooks),
    Updates("Updates", Icons.Filled.NewReleases, Icons.Outlined.NewReleases),
    History("History", Icons.Filled.History, Icons.Outlined.History),
    Settings("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
}

@Composable
fun MainScreen(
    onSerialClick: (Long) -> Unit,
    onChapterClick: (Long) -> Unit
) {
    var selectedTab by remember { mutableStateOf(Tab.Library) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                if (selectedTab == tab) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                Tab.Library   -> LibraryScreen(onSerialClick = onSerialClick, onChapterClick = onChapterClick)
                Tab.Updates   -> UpdatesScreen(onSerialClick = onSerialClick)
                Tab.History   -> HistoryScreen(onChapterClick = onChapterClick)
                Tab.Settings  -> SettingsScreen()
            }
        }
    }
}
