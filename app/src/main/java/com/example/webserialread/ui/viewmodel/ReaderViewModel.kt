package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as WebSerialApplication).repository

    private val _chapter = MutableStateFlow<ChapterEntity?>(null)
    val chapter: StateFlow<ChapterEntity?> = _chapter

    private val _htmlContent = MutableStateFlow<String?>(null)
    val htmlContent: StateFlow<String?> = _htmlContent

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _prevChapterId = MutableStateFlow<Long?>(null)
    val prevChapterId: StateFlow<Long?> = _prevChapterId

    private val _nextChapterId = MutableStateFlow<Long?>(null)
    val nextChapterId: StateFlow<Long?> = _nextChapterId

    fun loadChapter(chapterId: Long) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _htmlContent.value = null
            try {
                val ch = repo.getChapterById(chapterId) ?: error("Chapter not found")
                _chapter.value = ch

                val html = if (ch.content != null) ch.content
                else repo.fetchChapterContent(ch)
                _htmlContent.value = html

                // Resolve prev/next from a snapshot
                val all = repo.getChaptersSnapshot(ch.serialId)
                val idx = all.indexOfFirst { it.id == chapterId }
                _prevChapterId.value = if (idx > 0) all[idx - 1].id else null
                _nextChapterId.value = if (idx in 0 until all.lastIndex) all[idx + 1].id else null

                repo.markChapterRead(chapterId)
                repo.updateLastReadChapter(ch.serialId, chapterId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load chapter"
            } finally {
                _loading.value = false
            }
        }
    }
}
