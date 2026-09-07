package com.example.domain.model

enum class CallDirection {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class CallMedium {
    AUDIO,
    VIDEO
}

data class CallRecord(
    val id: String,
    val peerName: String,
    val peerAvatarUrl: String? = null,
    val direction: CallDirection,
    val medium: CallMedium,
    val timestamp: Long,
    val durationSeconds: Long
)

data class ScheduledMeeting(
    val id: String,
    val title: String,
    val description: String,
    val dateString: String,
    val startTime: String,
    val endTime: String,
    val timezone: String = "UTC+3",
    val hostName: String,
    val repeatRule: String = "Never",
    val reminderMinutes: Int = 10
)

data class ContactUser(
    val id: String,
    val name: String,
    val email: String,
    val isOnline: Boolean = true,
    val statusText: String = "Available"
)
