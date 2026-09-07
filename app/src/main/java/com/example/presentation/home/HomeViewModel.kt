package com.example.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.CurrentUser
import com.example.core.UserSession
import com.example.domain.model.Meeting
import com.example.domain.model.ScheduledMeeting
import com.example.domain.repository.MeetingRepository
import com.example.domain.repository.ScheduledMeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentUser: CurrentUser? = null,
    val recentMeetings: List<Meeting> = emptyList(),
    val upcomingMeetings: List<ScheduledMeeting> = emptyList(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val meetingRepository: MeetingRepository,
    private val scheduledMeetingRepository: ScheduledMeetingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            UserSession.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }

        viewModelScope.launch {
            meetingRepository.getRecentMeetings().collect { meetings ->
                _uiState.value = _uiState.value.copy(recentMeetings = meetings)
            }
        }

        viewModelScope.launch {
            scheduledMeetingRepository.getScheduledMeetings().collect { scheduled ->
                _uiState.value = _uiState.value.copy(upcomingMeetings = scheduled)
            }
        }
    }

    fun deleteRecentMeeting(id: String) {
        viewModelScope.launch {
            meetingRepository.deleteRecentMeeting(id)
        }
    }
}
