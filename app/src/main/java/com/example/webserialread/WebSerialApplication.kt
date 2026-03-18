package com.example.webserialread

import android.app.Application
import com.example.webserialread.data.local.AppDatabase
import com.example.webserialread.data.preferences.ReaderPreferences
import com.example.webserialread.data.repository.SerialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class WebSerialApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        SerialRepository(
            serialDao = database.serialDao(),
            chapterDao = database.chapterDao(),
            context = this
        )
    }

    val downloadManager by lazy {
        DownloadManager(repository, applicationScope)
    }

    val readerPreferences by lazy { ReaderPreferences(this) }
}
