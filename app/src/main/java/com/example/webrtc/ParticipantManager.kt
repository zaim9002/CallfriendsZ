package com.example.webrtc

import com.example.domain.model.Participant
import kotlinx.coroutines.flow.StateFlow

interface ParticipantManager {
    fun observeParticipants(): StateFlow<List<Participant>>
    fun addParticipant(participant: Participant)
    fun removeParticipant(userId: String)
    fun updateParticipantTrack(userId: String, isAudioOn: Boolean, isVideoOn: Boolean)
    fun setHandRaised(userId: String, isRaised: Boolean)
    fun setActiveSpeaker(userId: String?)
    fun clear()
}
