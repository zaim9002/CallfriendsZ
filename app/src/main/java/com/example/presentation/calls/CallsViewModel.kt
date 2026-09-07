package com.example.presentation.calls

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Resource
import com.example.domain.model.CallDirection
import com.example.domain.model.CallMedium
import com.example.domain.model.CallRecord
import com.example.domain.model.ContactUser
import com.example.domain.repository.CallsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class CallsUiState(
    val callHistory: List<CallRecord> = emptyList(),
    val contacts: List<ContactUser> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val showNewCallSheet: Boolean = false
)

class CallsViewModel(
    private val callsRepository: CallsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallsUiState())
    val uiState: StateFlow<CallsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeCallHistory()
        loadContacts("")
    }

    private fun observeCallHistory() {
        viewModelScope.launch {
            callsRepository.getCallHistory().collect { history ->
                _uiState.value = _uiState.value.copy(callHistory = history)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // Debounce search
            loadContacts(query)
        }
    }

    private fun loadContacts(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            when (val res = callsRepository.searchContacts(query)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(contacts = res.data, isSearching = false)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isSearching = false)
                }
                Resource.Loading -> {}
            }
        }
    }

    fun toggleNewCallSheet(open: Boolean) {
        _uiState.value = _uiState.value.copy(showNewCallSheet = open)
    }

    fun recordNewCall(peerName: String, medium: CallMedium) {
        val record = CallRecord(
            id = "call_" + UUID.randomUUID().toString().take(6),
            peerName = peerName,
            direction = CallDirection.OUTGOING,
            medium = medium,
            timestamp = System.currentTimeMillis(),
            durationSeconds = 0
        )
        viewModelScope.launch {
            callsRepository.addCallRecord(record)
        }
    }
}
