package com.example.webrtc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RtcEngine {
    fun initialize(turnServers: List<TurnServerConfig>)
    suspend fun createOffer(simulcastEnabled: Boolean = true): SessionDescriptionDto
    suspend fun createAnswer(): SessionDescriptionDto
    suspend fun setRemoteDescription(sdp: SessionDescriptionDto)
    suspend fun addIceCandidate(candidate: IceCandidateDto)
    fun getStats(): StateFlow<RtcStatsReport>
    fun release()
}

sealed class SignalingEvent {
    object Connected : SignalingEvent()
    data class RoomJoined(val roomId: String, val selfId: String, val participants: List<String>) : SignalingEvent()
    data class RemoteOffer(val fromId: String, val sdp: SessionDescriptionDto) : SignalingEvent()
    data class RemoteAnswer(val fromId: String, val sdp: SessionDescriptionDto) : SignalingEvent()
    data class RemoteIceCandidate(val fromId: String, val candidate: IceCandidateDto) : SignalingEvent()
    data class ParticipantJoined(val userId: String, val displayName: String, val role: String) : SignalingEvent()
    data class ParticipantLeft(val userId: String) : SignalingEvent()
    data class ReactionReceived(val userId: String, val emoji: String) : SignalingEvent()
    data class HandRaised(val userId: String, val isRaised: Boolean) : SignalingEvent()
    data class HostActionReceived(val actionType: String, val targetUserId: String) : SignalingEvent()
    data class Disconnected(val reason: String) : SignalingEvent()
    data class Reconnecting(val attempt: Int) : SignalingEvent()
}

interface SignalingClient {
    fun connect(serverUrl: String, meetingToken: String)
    fun disconnect()
    fun joinRoom(roomId: String, userId: String, displayName: String)
    fun leaveRoom(roomId: String)
    fun sendOffer(targetUserId: String, sdp: SessionDescriptionDto)
    fun sendAnswer(targetUserId: String, sdp: SessionDescriptionDto)
    fun sendIceCandidate(targetUserId: String, candidate: IceCandidateDto)
    fun sendReaction(roomId: String, emoji: String)
    fun sendRaiseHand(roomId: String, isRaised: Boolean)
    fun sendHostAction(roomId: String, actionType: String, targetUserId: String)
    fun observeEvents(): Flow<SignalingEvent>
}
