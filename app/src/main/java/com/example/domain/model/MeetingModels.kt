package com.example.domain.model

import com.example.core.MeetingAccessType
import com.example.core.ParticipantRole

data class MeetingPermissions(
    val allowQuickJoin: Boolean = true,
    val allowGuests: Boolean = true,
    val allowChat: Boolean = true,
    val allowScreenShare: Boolean = true,
    val allowRecording: Boolean = false,
    val allowCamera: Boolean = true,
    val allowMic: Boolean = true
)

data class HostControlsConfig(
    val isLocked: Boolean = false,
    val allowUnmute: Boolean = true,
    val allowCamera: Boolean = true,
    val allowScreenShare: Boolean = true,
    val allowChat: Boolean = true,
    val allowReactions: Boolean = true
)

data class Meeting(
    val id: String, // e.g. "cfz-894-kxm"
    val title: String,
    val description: String = "",
    val hostId: String,
    val hostName: String,
    val accessType: MeetingAccessType = MeetingAccessType.ANYONE_WITH_LINK,
    val passwordProtected: Boolean = false,
    val passwordHash: String? = null,
    val permissions: MeetingPermissions = MeetingPermissions(),
    val hostControls: HostControlsConfig = HostControlsConfig(),
    val isRecording: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val durationSeconds: Long = 0,
    val participantCount: Int = 1
)

data class Participant(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: ParticipantRole = ParticipantRole.PARTICIPANT,
    val isAudioEnabled: Boolean = true,
    val isVideoEnabled: Boolean = true,
    val isScreenSharing: Boolean = false,
    val isSpeaking: Boolean = false,
    val isHandRaised: Boolean = false,
    val isLocal: Boolean = false,
    val connectionQuality: String = "Excellent"
)

data class MeetingMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSystemMessage: Boolean = false,
    val attachmentName: String? = null,
    val attachmentUrl: String? = null,
    val attachmentType: String? = null,
    val replyToMessageId: String? = null
)

data class JoinRequest(
    val id: String,
    val userId: String,
    val displayName: String,
    val timestamp: Long = System.currentTimeMillis()
)
