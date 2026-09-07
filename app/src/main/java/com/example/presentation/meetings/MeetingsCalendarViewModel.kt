package com.example.presentation.meetings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.ScheduledMeeting
import com.example.domain.repository.ScheduledMeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class MeetingsCalendarUiState(
    val scheduledMeetings: List<ScheduledMeeting> = emptyList(),
    val selectedTab: CalendarTab = CalendarTab.TODAY,
    val showScheduleDialog: Boolean = false
)

enum class CalendarTab {
    TODAY,
    TOMORROW,
    UPCOMING
}

class MeetingsCalendarViewModel(
    private val scheduledMeetingRepository: ScheduledMeetingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingsCalendarUiState())
    val uiState: StateFlow<MeetingsCalendarUiState> = _uiState.asStateFlow()

    init {
        observeMeetings()
    }

    private fun observeMeetings() {
        viewModelScope.launch {
            scheduledMeetingRepository.getScheduledMeetings().collect { list ->
                _uiState.value = _uiState.value.copy(scheduledMeetings = list)
            }
        }
    }

    fun selectTab(tab: CalendarTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleScheduleDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showScheduleDialog = show)
    }

    fun saveScheduledMeeting(
        title: String,
        description: String,
        date: String,
        startTime: String,
        endTime: String
    ) {
        val meeting = ScheduledMeeting(
            id = "sch_" + UUID.randomUUID().toString().take(6),
            title = title.ifBlank { "Project Discussion" },
            description = description,
            dateString = date,
            startTime = startTime,
            endTime = endTime,
            hostName = "You"
        )
        viewModelScope.launch {
            scheduledMeetingRepository.scheduleMeeting(meeting)
            _uiState.value = _uiState.value.copy(showScheduleDialog = false)
        }
    }
}
