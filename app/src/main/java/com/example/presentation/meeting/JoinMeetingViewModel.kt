package com.example.presentation.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AudioRoute
import com.example.core.Resource
import com.example.core.UserSession
import com.example.domain.model.Meeting
import com.example.domain.repository.MeetingRepository
import com.example.security.MeetingSecurityValidator
import com.example.webrtc.MeetingClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class JoinMeetingUiState(
    val meetingCodeInput: String = "",
    val passcodeInput: String = "",
    val displayNameInput: String = "",
    val isMicEnabled: Boolean = true,
    val isCameraEnabled: Boolean = true,
    val isFrontCamera: Boolean = true,
    val audioRoute: AudioRoute = AudioRoute.SPEAKER,
    val isVerifying: Boolean = false,
    val resolvedMeeting: Meeting? = null,
    val errorMessage: String? = null,
    val localAudioLevel: Float = 0f
)

class JoinMeetingViewModel(
    private val meetingRepository: MeetingRepository,
    private val meetingClient: MeetingClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinMeetingUiState())
    val uiState: StateFlow<JoinMeetingUiState> = _uiState.asStateFlow()

    init {
        val user = UserSession.currentUser.value
        _uiState.value = _uiState.value.copy(
            displayNameInput = user?.displayName ?: "Guest"
        )
        observeMicLevel()
    }

    private fun observeMicLevel() {
        viewModelScope.launch {
            meetingClient.localAudioLevel.collect { level ->
                if (_uiState.value.isMicEnabled) {
                    _uiState.value = _uiState.value.copy(localAudioLevel = level)
                } else {
                    _uiState.value = _uiState.value.copy(localAudioLevel = 0f)
                }
            }
        }
    }

    fun onCodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(meetingCodeInput = value, errorMessage = null)
    }

    fun onPasscodeChanged(value: String) {
        _uiState.value = _uiState.value.copy(passcodeInput = value, errorMessage = null)
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.value = _uiState.value.copy(displayNameInput = value)
    }

    fun toggleMic() {
        _uiState.value = _uiState.value.copy(isMicEnabled = !_uiState.value.isMicEnabled)
    }

    fun toggleCamera() {
        _uiState.value = _uiState.value.copy(isCameraEnabled = !_uiState.value.isCameraEnabled)
    }

    fun switchCamera() {
        _uiState.value = _uiState.value.copy(isFrontCamera = !_uiState.value.isFrontCamera)
    }

    fun setAudioRoute(route: AudioRoute) {
        _uiState.value = _uiState.value.copy(audioRoute = route)
    }

    fun verifyAndJoin(onSuccess: (meetingId: String) -> Unit) {
        val state = _uiState.value
        val sanitized = MeetingSecurityValidator.sanitizeMeetingCode(state.meetingCodeInput)
        if (sanitized.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid meeting code or link.")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isVerifying = true, errorMessage = null)
            when (val result = meetingRepository.getMeetingById(sanitized)) {
                is Resource.Success -> {
                    val meeting = result.data
                    if (meeting.passwordProtected && !MeetingSecurityValidator.verifyPasscode(state.passcodeInput, meeting.passwordHash)) {
                        _uiState.value = _uiState.value.copy(
                            isVerifying = false,
                            errorMessage = "Incorrect passcode for this meeting."
                        )
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(isVerifying = false, resolvedMeeting = meeting)
                    onSuccess(meeting.id)
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isVerifying = false, errorMessage = result.message)
                }
                Resource.Loading -> {}
            }
        }
    }
}
