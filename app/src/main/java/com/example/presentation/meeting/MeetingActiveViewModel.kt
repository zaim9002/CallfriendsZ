package com.example.presentation.meeting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AudioRoute
import com.example.core.MeetingAccessType
import com.example.core.NetworkQuality
import com.example.core.ParticipantRole
import com.example.core.Resource
import com.example.core.UserSession
import com.example.domain.model.HostControlsConfig
import com.example.domain.model.JoinRequest
import com.example.domain.model.Meeting
import com.example.domain.model.MeetingMessage
import com.example.domain.model.Participant
import com.example.domain.repository.MeetingRepository
import com.example.webrtc.MediaTrackState
import com.example.webrtc.MeetingClient
import com.example.webrtc.RtcStatsReport
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class MeetingLayout {
    GRID,
    SPEAKER,
    SPOTLIGHT,
    SCREEN_SHARE,
    PIP
}

data class MeetingActiveUiState(
    val meeting: Meeting? = null,
    val connectionQuality: NetworkQuality = NetworkQuality.EXCELLENT,
    val mediaState: MediaTrackState = MediaTrackState(),
    val participants: List<Participant> = emptyList(),
    val messages: List<MeetingMessage> = emptyList(),
    val joinRequests: List<JoinRequest> = emptyList(),
    val stats: RtcStatsReport = RtcStatsReport(),
    val layout: MeetingLayout = MeetingLayout.GRID,
    val durationSeconds: Long = 0,
    val isControlsVisible: Boolean = true,
    val activeBottomSheet: ActiveSheet? = null,
    val isHandRaised: Boolean = false,
    val activeReaction: Pair<String, String>? = null, // Pair(sender, emoji)
    val isWaitingRoom: Boolean = false,
    val isRecording: Boolean = false,
    val unreadMessageCount: Int = 0
)

enum class ActiveSheet {
    CHAT,
    PARTICIPANTS,
    HOST_CONTROLS,
    MORE_OPTIONS,
    DIAGNOSTICS
}

class MeetingActiveViewModel(
    private val meetingRepository: MeetingRepository,
    val meetingClient: MeetingClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(MeetingActiveUiState())
    val uiState: StateFlow<MeetingActiveUiState> = _uiState.asStateFlow()

    private var durationTimerJob: Job? = null
    private var reactionDismissJob: Job? = null

    fun initializeMeeting(meetingId: String, initialAudio: Boolean = true, initialVideo: Boolean = true) {
        viewModelScope.launch {
            when (val result = meetingRepository.getMeetingById(meetingId)) {
                is Resource.Success -> {
                    val meeting = result.data
                    val user = UserSession.currentUser.value
                    val isHost = meeting.hostId == (user?.id ?: "")
                    val isWaiting = meeting.accessType == MeetingAccessType.ASK_TO_JOIN && !isHost

                    _uiState.value = _uiState.value.copy(
                        meeting = meeting,
                        isWaitingRoom = isWaiting,
                        isRecording = meeting.isRecording
                    )

                    if (!isWaiting) {
                        startSession(meeting, initialAudio, initialVideo)
                    }
                }
                is Resource.Error -> {
                    // Fallback create ad-hoc
                    val fallbackMeeting = Meeting(
                        id = meetingId,
                        title = "Meeting $meetingId",
                        hostId = "host",
                        hostName = "Host"
                    )
                    _uiState.value = _uiState.value.copy(meeting = fallbackMeeting)
                    startSession(fallbackMeeting, initialAudio, initialVideo)
                }
                Resource.Loading -> {}
            }
        }
    }

    private fun startSession(meeting: Meeting, initialAudio: Boolean, initialVideo: Boolean) {
        val user = UserSession.currentUser.value
        val role = if (meeting.hostId == (user?.id ?: "")) ParticipantRole.HOST else ParticipantRole.PARTICIPANT

        meetingClient.startMeetingSession(
            meetingId = meeting.id,
            userId = user?.id ?: "usr_local",
            userName = user?.displayName ?: "Alex",
            role = role,
            initialAudioEnabled = initialAudio,
            initialVideoEnabled = initialVideo,
            initialAudioRoute = AudioRoute.SPEAKER
        )

        observeClientState(meeting.id)
        startDurationTimer()
    }

    private fun observeClientState(meetingId: String) {
        viewModelScope.launch {
            meetingClient.connectionState.collect { quality ->
                _uiState.value = _uiState.value.copy(connectionQuality = quality)
            }
        }

        viewModelScope.launch {
            meetingClient.mediaState.collect { media ->
                _uiState.value = _uiState.value.copy(mediaState = media)
            }
        }

        viewModelScope.launch {
            meetingClient.participants.collect { list ->
                _uiState.value = _uiState.value.copy(participants = list)
            }
        }

        viewModelScope.launch {
            meetingClient.stats.collect { st ->
                _uiState.value = _uiState.value.copy(stats = st)
            }
        }

        viewModelScope.launch {
            meetingClient.incomingReactions.collect { reaction ->
                _uiState.value = _uiState.value.copy(activeReaction = reaction)
                reactionDismissJob?.cancel()
                reactionDismissJob = launch {
                    delay(2500)
                    _uiState.value = _uiState.value.copy(activeReaction = null)
                }
            }
        }

        viewModelScope.launch {
            meetingRepository.fetchMessages(meetingId).collect { msgList ->
                _uiState.value = _uiState.value.copy(messages = msgList)
            }
        }

        viewModelScope.launch {
            meetingRepository.fetchJoinRequests(meetingId).collect { reqList ->
                _uiState.value = _uiState.value.copy(joinRequests = reqList)
            }
        }
    }

    private fun startDurationTimer() {
        durationTimerJob?.cancel()
        durationTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                _uiState.value = _uiState.value.copy(durationSeconds = _uiState.value.durationSeconds + 1)
            }
        }
    }

    fun toggleControlsVisibility() {
        _uiState.value = _uiState.value.copy(isControlsVisible = !_uiState.value.isControlsVisible)
    }

    fun toggleMic() = meetingClient.toggleMic()
    fun toggleCamera() = meetingClient.toggleCamera()
    fun switchCamera() = meetingClient.switchCamera()
    fun setAudioRoute(route: AudioRoute) = meetingClient.setAudioRoute(route)
    fun setScreenSharing(enabled: Boolean) {
        meetingClient.setScreenSharing(enabled)
        _uiState.value = _uiState.value.copy(
            layout = if (enabled) MeetingLayout.SCREEN_SHARE else MeetingLayout.GRID
        )
    }

    fun sendReaction(emoji: String) {
        meetingClient.sendReaction(emoji)
    }

    fun toggleHandRaise() {
        meetingClient.toggleHandRaise()
        _uiState.value = _uiState.value.copy(isHandRaised = !_uiState.value.isHandRaised)
    }

    fun setLayout(layout: MeetingLayout) {
        _uiState.value = _uiState.value.copy(layout = layout)
    }

    fun openSheet(sheet: ActiveSheet) {
        if (sheet == ActiveSheet.CHAT) {
            _uiState.value = _uiState.value.copy(activeBottomSheet = sheet, unreadMessageCount = 0)
        } else {
            _uiState.value = _uiState.value.copy(activeBottomSheet = sheet)
        }
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(activeBottomSheet = null)
    }

    fun sendMessage(text: String, attachmentName: String? = null) {
        val meetingId = _uiState.value.meeting?.id ?: return
        val user = UserSession.currentUser.value
        val msg = MeetingMessage(
            id = "msg_" + UUID.randomUUID().toString().take(6),
            senderId = user?.id ?: "usr_local",
            senderName = user?.displayName ?: "Me",
            text = text,
            attachmentName = attachmentName
        )
        viewModelScope.launch {
            meetingRepository.sendMessage(meetingId, msg)
        }
    }

    fun hostMuteAll() {
        _uiState.value.participants.forEach {
            meetingClient.hostMuteParticipant(it.id)
        }
    }

    fun hostRemoveParticipant(userId: String) {
        meetingClient.hostRemoveParticipant(userId)
    }

    fun hostLockMeeting(locked: Boolean) {
        val meeting = _uiState.value.meeting ?: return
        val updated = meeting.hostControls.copy(isLocked = locked)
        viewModelScope.launch {
            meetingRepository.updateHostControls(meeting.id, updated)
            _uiState.value = _uiState.value.copy(meeting = meeting.copy(hostControls = updated))
        }
    }

    fun admitJoinRequest(requestId: String) {
        val meetingId = _uiState.value.meeting?.id ?: return
        viewModelScope.launch {
            meetingRepository.respondJoinRequest(meetingId, requestId, admit = true)
        }
    }

    fun denyJoinRequest(requestId: String) {
        val meetingId = _uiState.value.meeting?.id ?: return
        viewModelScope.launch {
            meetingRepository.respondJoinRequest(meetingId, requestId, admit = false)
        }
    }

    fun leaveMeeting() {
        durationTimerJob?.cancel()
        reactionDismissJob?.cancel()
        meetingClient.leaveMeeting()
    }
}
