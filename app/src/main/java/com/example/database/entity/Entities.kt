package com.example.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val hostId: String,
    val hostName: String,
    val accessType: String,
    val passwordProtected: Boolean,
    val passwordHash: String?,
    val isRecording: Boolean,
    val createdAt: Long,
    val durationSeconds: Long,
    val participantCount: Int
)

@Entity(tableName = "call_history")
data class CallHistoryEntity(
    @PrimaryKey val id: String,
    val peerName: String,
    val peerAvatarUrl: String?,
    val direction: String, // INCOMING, OUTGOING, MISSED
    val medium: String, // AUDIO, VIDEO
    val timestamp: Long,
    val durationSeconds: Long
)

@Entity(tableName = "scheduled_meetings")
data class ScheduledMeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val dateString: String,
    val startTime: String,
    val endTime: String,
    val timezone: String,
    val hostName: String,
    val repeatRule: String,
    val reminderMinutes: Int
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phoneOrNote: String = "",
    val isOnline: Boolean = true,
    val statusText: String = "Available"
)

@Entity(tableName = "meeting_notes")
data class MeetingNoteEntity(
    @PrimaryKey val id: String,
    val meetingId: String,
    val meetingTitle: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

