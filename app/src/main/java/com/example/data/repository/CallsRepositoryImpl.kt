package com.example.data.repository

import com.example.core.Resource
import com.example.database.dao.CallHistoryDao
import com.example.database.dao.ScheduledMeetingDao
import com.example.database.entity.CallHistoryEntity
import com.example.database.entity.ScheduledMeetingEntity
import com.example.domain.model.CallDirection
import com.example.domain.model.CallMedium
import com.example.domain.model.CallRecord
import com.example.domain.model.ContactUser
import com.example.domain.model.ScheduledMeeting
import com.example.domain.repository.CallsRepository
import com.example.domain.repository.ScheduledMeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CallsRepositoryImpl(
    private val callHistoryDao: CallHistoryDao
) : CallsRepository {

    private val sampleContacts = listOf(
        ContactUser("u1", "Zaid Al-Harbi", "zaid.harbi@callfriendsz.app", isOnline = true, statusText = "Available for meetings"),
        ContactUser("u2", "Mariam Al-Khalidi", "mariam.k@callfriendsz.app", isOnline = true, statusText = "In a call"),
        ContactUser("u3", "Omar Farooq", "omar.f@callfriendsz.app", isOnline = false, statusText = "Away"),
        ContactUser("u4", "Lina Qasim", "lina.q@callfriendsz.app", isOnline = true, statusText = "Ready to connect"),
        ContactUser("u5", "Khaled Mansour", "khaled.m@callfriendsz.app", isOnline = false, statusText = "Offline")
    )

    override fun getCallHistory(): Flow<List<CallRecord>> {
        return callHistoryDao.getAllCallHistory().map { list ->
            if (list.isEmpty()) {
                getDefaultCallHistory()
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun addCallRecord(record: CallRecord) {
        callHistoryDao.insertCall(record.toEntity())
    }

    override suspend fun clearHistory() {
        callHistoryDao.clearAll()
    }

    override suspend fun searchContacts(query: String): Resource<List<ContactUser>> {
        val trimmed = query.trim().lowercase()
        return if (trimmed.isBlank()) {
            Resource.Success(sampleContacts)
        } else {
            Resource.Success(
                sampleContacts.filter {
                    it.name.lowercase().contains(trimmed) || it.email.lowercase().contains(trimmed)
                }
            )
        }
    }

    private fun getDefaultCallHistory(): List<CallRecord> = listOf(
        CallRecord(
            id = "c1",
            peerName = "Zaid Al-Harbi",
            direction = CallDirection.INCOMING,
            medium = CallMedium.VIDEO,
            timestamp = System.currentTimeMillis() - 7200000L,
            durationSeconds = 840
        ),
        CallRecord(
            id = "c2",
            peerName = "Mariam Al-Khalidi",
            direction = CallDirection.OUTGOING,
            medium = CallMedium.AUDIO,
            timestamp = System.currentTimeMillis() - 86400000L,
            durationSeconds = 420
        ),
        CallRecord(
            id = "c3",
            peerName = "Omar Farooq",
            direction = CallDirection.MISSED,
            medium = CallMedium.VIDEO,
            timestamp = System.currentTimeMillis() - 172800000L,
            durationSeconds = 0
        )
    )

    private fun CallRecord.toEntity() = CallHistoryEntity(
        id = id,
        peerName = peerName,
        peerAvatarUrl = peerAvatarUrl,
        direction = direction.name,
        medium = medium.name,
        timestamp = timestamp,
        durationSeconds = durationSeconds
    )

    private fun CallHistoryEntity.toDomain() = CallRecord(
        id = id,
        peerName = peerName,
        peerAvatarUrl = peerAvatarUrl,
        direction = try { CallDirection.valueOf(direction) } catch (_: Exception) { CallDirection.INCOMING },
        medium = try { CallMedium.valueOf(medium) } catch (_: Exception) { CallMedium.VIDEO },
        timestamp = timestamp,
        durationSeconds = durationSeconds
    )
}

class ScheduledMeetingRepositoryImpl(
    private val scheduledMeetingDao: ScheduledMeetingDao
) : ScheduledMeetingRepository {

    override fun getScheduledMeetings(): Flow<List<ScheduledMeeting>> {
        return scheduledMeetingDao.getScheduledMeetings().map { list ->
            if (list.isEmpty()) {
                getDefaultScheduled()
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun scheduleMeeting(meeting: ScheduledMeeting): Resource<ScheduledMeeting> {
        scheduledMeetingDao.insertScheduledMeeting(meeting.toEntity())
        return Resource.Success(meeting)
    }

    override suspend fun deleteScheduledMeeting(id: String): Resource<Unit> {
        scheduledMeetingDao.deleteScheduledMeeting(id)
        return Resource.Success(Unit)
    }

    private fun getDefaultScheduled(): List<ScheduledMeeting> = listOf(
        ScheduledMeeting(
            id = "sch_1",
            title = "Design System & UI Architecture Review",
            description = "Reviewing Compose M3 dark mode tokens and WebRTC widgets",
            dateString = "Today",
            startTime = "04:30 PM",
            endTime = "05:30 PM",
            hostName = "Sarah Jenkins",
            repeatRule = "Weekly",
            reminderMinutes = 10
        ),
        ScheduledMeeting(
            id = "sch_2",
            title = "Core SFU & Backend Sync",
            description = "Infrastructure latency and TURN allocation benchmark",
            dateString = "Tomorrow",
            startTime = "11:00 AM",
            endTime = "12:00 PM",
            hostName = "Zaid Al-Harbi",
            repeatRule = "Daily",
            reminderMinutes = 15
        )
    )

    private fun ScheduledMeeting.toEntity() = ScheduledMeetingEntity(
        id = id,
        title = title,
        description = description,
        dateString = dateString,
        startTime = startTime,
        endTime = endTime,
        timezone = timezone,
        hostName = hostName,
        repeatRule = repeatRule,
        reminderMinutes = reminderMinutes
    )

    private fun ScheduledMeetingEntity.toDomain() = ScheduledMeeting(
        id = id,
        title = title,
        description = description,
        dateString = dateString,
        startTime = startTime,
        endTime = endTime,
        timezone = timezone,
        hostName = hostName,
        repeatRule = repeatRule,
        reminderMinutes = reminderMinutes
    )
}
