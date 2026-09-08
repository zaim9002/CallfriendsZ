package com.example.data.repository

import com.example.core.Resource
import com.example.database.dao.CallHistoryDao
import com.example.database.dao.ContactDao
import com.example.database.dao.ScheduledMeetingDao
import com.example.database.entity.CallHistoryEntity
import com.example.database.entity.ContactEntity
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
    private val callHistoryDao: CallHistoryDao,
    private val contactDao: ContactDao
) : CallsRepository {

    override fun getCallHistory(): Flow<List<CallRecord>> {
        return callHistoryDao.getAllCallHistory().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addCallRecord(record: CallRecord) {
        callHistoryDao.insertCall(record.toEntity())
    }

    override suspend fun clearHistory() {
        callHistoryDao.clearAll()
    }

    override fun getAllContacts(): Flow<List<ContactUser>> {
        return contactDao.getAllContacts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addContact(contact: ContactUser) {
        contactDao.insertContact(contact.toEntity())
    }

    override suspend fun deleteContact(id: String) {
        contactDao.deleteContact(id)
    }

    override suspend fun searchContacts(query: String): Resource<List<ContactUser>> {
        val trimmed = query.trim()
        return try {
            val results = if (trimmed.isBlank()) {
                contactDao.searchContacts("")
            } else {
                contactDao.searchContacts(trimmed)
            }
            Resource.Success(results.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search contacts")
        }
    }

    private fun ContactEntity.toDomain() = ContactUser(
        id = id,
        name = name,
        email = email,
        isOnline = isOnline,
        statusText = if (phoneOrNote.isNotBlank()) phoneOrNote else statusText
    )

    private fun ContactUser.toEntity() = ContactEntity(
        id = id,
        name = name,
        email = email,
        phoneOrNote = statusText,
        isOnline = isOnline,
        statusText = statusText
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
            list.map { it.toDomain() }
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
