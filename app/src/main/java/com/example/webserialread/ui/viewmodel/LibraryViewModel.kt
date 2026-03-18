package com.example.webserialread.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.webserialread.WebSerialApplication
import com.example.webserialread.data.local.entity.SerialEntity
import com.example.webserialread.data.remote.KnownSerial
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SerialItem(val serial: SerialEntity, val unreadCount: Int)

class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = (app as WebSerialApplication).repository

    val serialItems: StateFlow<List<SerialItem>> = combine(
        repo.getAllSerials(),
        repo.getUnreadCountMap()
    ) { serials, unreadMap ->
        serials.map { SerialItem(it, unreadMap[it.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _addState = MutableStateFlow<AddState>(AddState.Idle)
    val addState: StateFlow<AddState> = _addState

    fun addSerial(url: String, tocUrl: String? = null, wpCategorySlug: String? = null) {
        _addState.value = AddState.Loading
        viewModelScope.launch {
            _addState.value = repo.addSerial(url, tocUrl?.ifBlank { null }, wpCategorySlug).fold(
                onSuccess = { AddState.Success },
                onFailure = { AddState.Error(it.message ?: "Unknown error") }
            )
        }
    }

    fun addKnownSerial(known: KnownSerial) = addSerial(known.siteUrl, known.tocUrl, known.wpCategorySlug)

    fun resetAddState() { _addState.value = AddState.Idle }

    fun deleteSerial(serial: SerialEntity) {
        viewModelScope.launch { repo.deleteSerial(serial) }
    }

    sealed interface AddState {
        data object Idle : AddState
        data object Loading : AddState
        data object Success : AddState
        data class Error(val message: String) : AddState
    }
}
