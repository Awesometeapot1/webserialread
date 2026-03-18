package com.example.webserialread

import com.example.webserialread.data.repository.SerialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Application-scoped download manager. Downloads survive screen navigation
 * because they run in the Application's CoroutineScope, not a ViewModel scope.
 */
class DownloadManager(
    private val repo: SerialRepository,
    private val scope: CoroutineScope
) {
    private val jobs = mutableMapOf<Long, Job>()

    // Map of serialId -> (chaptersDownloaded, totalToDownload)
    private val _progress = MutableStateFlow<Map<Long, Pair<Int, Int>>>(emptyMap())
    val progress: StateFlow<Map<Long, Pair<Int, Int>>> = _progress

    fun isDownloading(serialId: Long) = jobs.containsKey(serialId)

    fun downloadAll(serialId: Long) {
        if (jobs.containsKey(serialId)) return
        val job = scope.launch {
            repo.downloadAllChapters(serialId) { done, total ->
                _progress.update { it + (serialId to (done to total)) }
            }
        }
        jobs[serialId] = job
        job.invokeOnCompletion {
            jobs.remove(serialId)
            _progress.update { it - serialId }
        }
    }

    fun cancel(serialId: Long) {
        jobs[serialId]?.cancel()
        jobs.remove(serialId)
        _progress.update { it - serialId }
    }
}
