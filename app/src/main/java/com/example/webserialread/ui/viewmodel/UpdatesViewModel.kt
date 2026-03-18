package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import kotlinx.coroutines.flow.*

class UpdatesViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as WebSerialApplication).repository

    // Serials that have at least one unread chapter
    val updates: StateFlow<List<SerialItem>> = combine(
        repo.getAllSerials(),
        repo.getUnreadCountMap()
    ) { serials, unreadMap ->
        serials
            .map { SerialItem(it, unreadMap[it.id] ?: 0) }
            .filter { it.unreadCount > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
