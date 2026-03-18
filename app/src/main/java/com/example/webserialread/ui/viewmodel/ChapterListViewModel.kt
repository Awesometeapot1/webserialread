package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.local.entity.ChapterEntity
import com.example.webserialread.data.local.entity.SerialEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ChapterListViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as WebSerialApplication).repository
    private val downloadManager = (app as WebSerialApplication).downloadManager

    private val _serialId = MutableStateFlow(-1L)

    val serial: StateFlow<SerialEntity?> = _serialId
        .flatMapLatest { id ->
            flow<SerialEntity?> { emit(if (id == -1L) null else repo.getSerialById(id)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val chapters: StateFlow<List<ChapterEntity>> = _serialId
        .flatMapLatest { id ->
            if (id == -1L) flowOf<List<ChapterEntity>>(emptyList())
            else repo.getChaptersForSerial(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val downloadProgress: StateFlow<Pair<Int, Int>?> = combine(
        _serialId, downloadManager.progress
    ) { id, map -> map[id] }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError

    fun init(serialId: Long) {
        if (_serialId.value == serialId) return
        _serialId.value = serialId
    }

    fun sync() {
        val s = serial.value ?: return
        viewModelScope.launch {
            _syncing.value = true
            _syncError.value = null
            try { repo.syncChapters(s) }
            catch (e: Exception) { _syncError.value = e.message ?: "Sync failed" }
            finally { _syncing.value = false }
        }
    }

    fun downloadAll() {
        val id = _serialId.value
        if (id != -1L) downloadManager.downloadAll(id)
    }

    fun cancelDownload() {
        val id = _serialId.value
        if (id != -1L) downloadManager.cancel(id)
    }

    fun clearSyncError() { _syncError.value = null }
}
