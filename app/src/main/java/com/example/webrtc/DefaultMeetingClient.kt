package com.example.webrtc

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.example.core.AudioRoute
import com.example.core.Constants
import com.example.core.NetworkQuality
import com.example.core.ParticipantRole
import com.example.domain.model.Participant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sin

interface MeetingClient {
    val connectionState: StateFlow<NetworkQuality>
    val mediaState: StateFlow<MediaTrackState>
    val participants: StateFlow<List<Participant>>
    val stats: StateFlow<RtcStatsReport>
    val localAudioLevel: StateFlow<Float>
    val incomingReactions: SharedFlow<Pair<String, String>> // Pair(userName, emoji)

    fun startMeetingSession(
        meetingId: String,
        userId: String,
        userName: String,
        role: ParticipantRole,
        initialAudioEnabled: Boolean,
        initialVideoEnabled: Boolean,
        initialAudioRoute: AudioRoute
    )

    fun toggleMic()
    fun toggleCamera()
    fun switchCamera()
    fun setAudioRoute(route: AudioRoute)
    fun setScreenSharing(enabled: Boolean)
    fun sendReaction(emoji: String)
    fun toggleHandRaise()
    fun hostMuteParticipant(userId: String)
    fun hostRemoveParticipant(userId: String)
    fun leaveMeeting()
}

class DefaultMeetingClient(
    private val context: Context
) : MeetingClient {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var statsJob: Job? = null
    private var micLevelJob: Job? = null
    private var simulationTimerJob: Job? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _connectionState = MutableStateFlow(NetworkQuality.EXCELLENT)
    override val connectionState: StateFlow<NetworkQuality> = _connectionState.asStateFlow()

    private val _mediaState = MutableStateFlow(MediaTrackState())
    override val mediaState: StateFlow<MediaTrackState> = _mediaState.asStateFlow()

    private val _participants = MutableStateFlow<List<Participant>>(emptyList())
    override val participants: StateFlow<List<Participant>> = _participants.asStateFlow()

    private val _stats = MutableStateFlow(RtcStatsReport())
    override val stats: StateFlow<RtcStatsReport> = _stats.asStateFlow()

    private val _localAudioLevel = MutableStateFlow(0f)
    override val localAudioLevel: StateFlow<Float> = _localAudioLevel.asStateFlow()

    private val _incomingReactions = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 16)
    override val incomingReactions: SharedFlow<Pair<String, String>> = _incomingReactions.asSharedFlow()

    private var currentMeetingId: String? = null
    private var currentUserId: String = ""
    private var currentUserName: String = ""

    override fun startMeetingSession(
        meetingId: String,
        userId: String,
        userName: String,
        role: ParticipantRole,
        initialAudioEnabled: Boolean,
        initialVideoEnabled: Boolean,
        initialAudioRoute: AudioRoute
    ) {
        currentMeetingId = meetingId
        currentUserId = userId
        currentUserName = userName

        _mediaState.value = MediaTrackState(
            isAudioMuted = !initialAudioEnabled,
            isVideoMuted = !initialVideoEnabled,
            isFrontCamera = true,
            isScreenSharing = false,
            activeAudioRoute = initialAudioRoute
        )

        applyAudioRoute(initialAudioRoute)

        // Initialize real local participant
        val localUser = Participant(
            id = userId,
            displayName = "$userName (You)",
            role = role,
            isAudioEnabled = initialAudioEnabled,
            isVideoEnabled = initialVideoEnabled,
            isScreenSharing = false,
            isSpeaking = false,
            isHandRaised = false,
            isLocal = true,
            connectionQuality = "Excellent"
        )

        _participants.value = listOf(localUser)
        _connectionState.value = NetworkQuality.EXCELLENT

        startAudioLevelPolling()
        startStatsMonitor()
    }

    override fun toggleMic() {
        val current = _mediaState.value
        val newState = !current.isAudioMuted
        _mediaState.value = current.copy(isAudioMuted = newState)

        updateLocalParticipant {
            it.copy(isAudioEnabled = !newState)
        }
    }

    override fun toggleCamera() {
        val current = _mediaState.value
        val newState = !current.isVideoMuted
        _mediaState.value = current.copy(isVideoMuted = newState)

        updateLocalParticipant {
            it.copy(isVideoEnabled = !newState)
        }
    }

    override fun switchCamera() {
        val current = _mediaState.value
        _mediaState.value = current.copy(isFrontCamera = !current.isFrontCamera)
    }

    override fun setAudioRoute(route: AudioRoute) {
        _mediaState.value = _mediaState.value.copy(activeAudioRoute = route)
        applyAudioRoute(route)
    }

    private fun applyAudioRoute(route: AudioRoute) {
        try {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            when (route) {
                AudioRoute.SPEAKER -> {
                    audioManager?.isSpeakerphoneOn = true
                }
                AudioRoute.EARPIECE -> {
                    audioManager?.isSpeakerphoneOn = false
                }
                AudioRoute.BLUETOOTH -> {
                    audioManager?.isSpeakerphoneOn = false
                    @Suppress("DEPRECATION")
                    audioManager?.startBluetoothSco()
                }
                AudioRoute.WIRED -> {
                    audioManager?.isSpeakerphoneOn = false
                }
            }
        } catch (_: Exception) {
            // Audio routing fallback
        }
    }

    override fun setScreenSharing(enabled: Boolean) {
        _mediaState.value = _mediaState.value.copy(isScreenSharing = enabled)
        updateLocalParticipant {
            it.copy(isScreenSharing = enabled)
        }
    }

    override fun sendReaction(emoji: String) {
        scope.launch {
            _incomingReactions.emit(Pair(currentUserName, emoji))
        }
    }

    override fun toggleHandRaise() {
        updateLocalParticipant {
            val nextState = !it.isHandRaised
            it.copy(isHandRaised = nextState)
        }
    }

    override fun hostMuteParticipant(userId: String) {
        _participants.value = _participants.value.map {
            if (it.id == userId) it.copy(isAudioEnabled = false) else it
        }
    }

    override fun hostRemoveParticipant(userId: String) {
        _participants.value = _participants.value.filterNot { it.id == userId }
    }

    override fun leaveMeeting() {
        statsJob?.cancel()
        micLevelJob?.cancel()
        simulationTimerJob?.cancel()
        try {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isSpeakerphoneOn = false
        } catch (_: Exception) { }
        _participants.value = emptyList()
        _localAudioLevel.value = 0f
    }

    private fun updateLocalParticipant(transform: (Participant) -> Participant) {
        _participants.value = _participants.value.map {
            if (it.id == currentUserId) transform(it) else it
        }
    }

    private fun startAudioLevelPolling() {
        micLevelJob?.cancel()
        micLevelJob = scope.launch {
            var step = 0.0
            while (isActive) {
                if (!_mediaState.value.isAudioMuted) {
                    step += 0.3
                    val level = ((sin(step) + 1) / 2.0).toFloat() * 0.85f
                    _localAudioLevel.value = level
                } else {
                    _localAudioLevel.value = 0f
                }
                delay(120)
            }
        }
    }

    private fun startStatsMonitor() {
        statsJob?.cancel()
        statsJob = scope.launch {
            while (isActive) {
                delay(3000)
                _stats.value = RtcStatsReport(
                    rttMs = (24..38).random().toLong(),
                    jitterMs = (2..6).random().toLong(),
                    packetLossPercent = (0..5).random() / 10f,
                    bitrateKbps = if (_mediaState.value.isScreenSharing) 2400 else 1280,
                    quality = NetworkQuality.EXCELLENT
                )
            }
        }
    }
}
