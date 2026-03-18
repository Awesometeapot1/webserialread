package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.local.entity.ChapterEntity
import com.example.webserialread.data.local.entity.SerialEntity
import kotlinx.coroutines.flow.*

data class HistoryItem(val chapter: ChapterEntity, val serial: SerialEntity?)

class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as WebSerialApplication).repository

    val history: StateFlow<List<HistoryItem>> = combine(
        repo.getRecentlyReadChapters(),
        repo.getAllSerials()
    ) { chapters, serials ->
        val serialMap = serials.associateBy { it.id }
        chapters.map { HistoryItem(it, serialMap[it.serialId]) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
