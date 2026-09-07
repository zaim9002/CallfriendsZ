package com.example.presentation.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.Constants
import com.example.core.MeetingAccessType
import com.example.core.Resource
import com.example.core.UserSession
import com.example.domain.model.Meeting
import com.example.domain.model.MeetingPermissions
import com.example.domain.repository.MeetingRepository
import com.example.security.MeetingSecurityValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateMeetingUiState(
    val title: String = "",
    val description: String = "",
    val accessType: MeetingAccessType = MeetingAccessType.ANYONE_WITH_LINK,
    val passcode: String = "",
    val generatedMeetingId: String = "",
    val generatedMeetingLink: String = "",
    val permissions: MeetingPermissions = MeetingPermissions(),
    val isCreating: Boolean = false,
    val isCreated: Boolean = false,
    val errorMessage: String? = null
)

class CreateMeetingViewModel(
    private val meetingRepository: MeetingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMeetingUiState())
    val uiState: StateFlow<CreateMeetingUiState> = _uiState.asStateFlow()

    init {
        generateNewMeetingId()
    }

    fun generateNewMeetingId() {
        val newId = MeetingSecurityValidator.generateSecureMeetingId()
        val link = Constants.getPublicMeetingUrl(newId)
        _uiState.value = _uiState.value.copy(
            generatedMeetingId = newId,
            generatedMeetingLink = link
        )
    }

    fun onTitleChanged(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun onDescriptionChanged(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onAccessTypeSelected(type: MeetingAccessType) {
        _uiState.value = _uiState.value.copy(accessType = type)
    }

    fun onPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(passcode = value)
    }

    fun togglePermission(key: String, value: Boolean) {
        val current = _uiState.value.permissions
        val updated = when (key) {
            "quickJoin" -> current.copy(allowQuickJoin = value)
            "guests" -> current.copy(allowGuests = value)
            "chat" -> current.copy(allowChat = value)
            "screenShare" -> current.copy(allowScreenShare = value)
            "recording" -> current.copy(allowRecording = value)
            "camera" -> current.copy(allowCamera = value)
            "mic" -> current.copy(allowMic = value)
            else -> current
        }
        _uiState.value = _uiState.value.copy(permissions = updated)
    }

    fun createMeeting(onComplete: (String) -> Unit) {
        val state = _uiState.value
        val user = UserSession.currentUser.value
        val meeting = Meeting(
            id = state.generatedMeetingId,
            title = state.title.ifBlank { "Quick Meeting" },
            description = state.description,
            hostId = user?.id ?: "host_default",
            hostName = user?.displayName ?: "Host",
            accessType = state.accessType,
            passwordProtected = state.passcode.isNotBlank(),
            passwordHash = if (state.passcode.isNotBlank()) MeetingSecurityValidator.hashPasscode(state.passcode) else null,
            permissions = state.permissions,
            participantCount = 1
        )

        viewModelScope.launch {
            _uiState.value = state.copy(isCreating = true)
            meetingRepository.createMeeting(meeting)
            _uiState.value = _uiState.value.copy(isCreating = false, isCreated = true)
            onComplete(meeting.id)
        }
    }
}
