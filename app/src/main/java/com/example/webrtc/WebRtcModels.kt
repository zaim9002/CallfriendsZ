package com.example.webrtc

import com.example.core.AudioRoute
import com.example.core.NetworkQuality

data class TurnServerConfig(
    val urls: List<String>,
    val username: String? = null,
    val credential: String? = null
)

data class IceCandidateDto(
    val sdpMid: String,
    val sdpMLineIndex: Int,
    val candidate: String
)

data class SessionDescriptionDto(
    val type: String, // "offer" or "answer"
    val sdp: String
)

data class RtcStatsReport(
    val rttMs: Long = 28,
    val jitterMs: Long = 4,
    val packetLossPercent: Float = 0.1f,
    val bitrateKbps: Int = 1250,
    val quality: NetworkQuality = NetworkQuality.EXCELLENT
)

data class MediaTrackState(
    val isAudioMuted: Boolean = false,
    val isVideoMuted: Boolean = false,
    val isFrontCamera: Boolean = true,
    val isScreenSharing: Boolean = false,
    val activeAudioRoute: AudioRoute = AudioRoute.SPEAKER
)
