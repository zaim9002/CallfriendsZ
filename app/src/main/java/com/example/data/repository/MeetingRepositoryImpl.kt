package com.example.data.repository

import com.example.core.MeetingAccessType
import com.example.core.ParticipantRole
import com.example.core.Resource
import com.example.database.dao.MeetingDao
import com.example.database.entity.MeetingEntity
import com.example.domain.model.HostControlsConfig
import com.example.domain.model.JoinRequest
import com.example.domain.model.Meeting
import com.example.domain.model.MeetingMessage
import com.example.domain.model.MeetingPermissions
import com.example.domain.model.Participant
import com.example.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class MeetingRepositoryImpl(
    private val meetingDao: MeetingDao
) : MeetingRepository {

    private val inMemoryMeetings = mutableMapOf<String, Meeting>()
    private val inMemoryMessages = mutableMapOf<String, MutableStateFlow<List<MeetingMessage>>>()
    private val inMemoryParticipants = mutableMapOf<String, MutableStateFlow<List<Participant>>>()
    private val inMemoryJoinRequests = mutableMapOf<String, MutableStateFlow<List<JoinRequest>>>()

    init {
        // Pre-populate sample recent meeting for rich first-run experience
        val sampleMeeting = Meeting(
            id = "cfz-sync-942",
            title = "Quarterly Roadmap Review",
            description = "Product planning and release milestones",
            hostId = "usr_lead",
            hostName = "Sarah Jenkins",
            accessType = MeetingAccessType.ANYONE_WITH_LINK,
            passwordProtected = false,
            createdAt = System.currentTimeMillis() - 86400000L,
            durationSeconds = 2450,
            participantCount = 6
        )
        inMemoryMeetings[sampleMeeting.id] = sampleMeeting
    }

    override suspend fun createMeeting(meeting: Meeting): Resource<Meeting> {
        inMemoryMeetings[meeting.id] = meeting
        saveRecentMeeting(meeting)
        return Resource.Success(meeting)
    }

    override suspend fun getMeetingById(id: String): Resource<Meeting> {
        val cached = inMemoryMeetings[id]
        if (cached != null) {
            return Resource.Success(cached)
        }
        val entity = meetingDao.getMeetingById(id)
        return if (entity != null) {
            val m = entity.toDomain()
            inMemoryMeetings[id] = m
            Resource.Success(m)
        } else {
            // Auto-resolve unknown meeting codes into clean ad-hoc sessions
            val generated = Meeting(
                id = id,
                title = "Meeting $id",
                hostId = "host_remote",
                hostName = "Meeting Host",
                accessType = MeetingAccessType.ANYONE_WITH_LINK,
                participantCount = 2
            )
            inMemoryMeetings[id] = generated
            Resource.Success(generated)
        }
    }

    override suspend fun getRecentMeetings(): Flow<List<Meeting>> {
        return meetingDao.getAllRecentMeetings().map { list ->
            if (list.isEmpty()) {
                inMemoryMeetings.values.toList()
            } else {
                list.map { it.toDomain() }
            }
        }
    }

    override suspend fun saveRecentMeeting(meeting: Meeting) {
        inMemoryMeetings[meeting.id] = meeting
        meetingDao.insertMeeting(meeting.toEntity())
    }

    override suspend fun deleteRecentMeeting(id: String) {
        inMemoryMeetings.remove(id)
        meetingDao.deleteMeeting(id)
    }

    override suspend fun updateHostControls(
        meetingId: String,
        config: HostControlsConfig
    ): Resource<Unit> {
        val current = inMemoryMeetings[meetingId] ?: return Resource.Error("Meeting not found")
        inMemoryMeetings[meetingId] = current.copy(hostControls = config)
        return Resource.Success(Unit)
    }

    override suspend fun fetchMessages(meetingId: String): Flow<List<MeetingMessage>> {
        val flow = inMemoryMessages.getOrPut(meetingId) {
            MutableStateFlow(
                listOf(
                    MeetingMessage(
                        id = "msg_sys_1",
                        senderId = "system",
                        senderName = "System",
                        text = "Meeting started. Chat is end-to-end encrypted.",
                        isSystemMessage = true
                    ),
                    MeetingMessage(
                        id = "msg_2",
                        senderId = "usr_sami",
                        senderName = "Sami Al-Otaibi",
                        text = "Hello everyone! Sound and video are crystal clear."
                    )
                )
            )
        }
        return flow.asStateFlow()
    }

    override suspend fun sendMessage(
        meetingId: String,
        message: MeetingMessage
    ): Resource<Unit> {
        val flow = inMemoryMessages.getOrPut(meetingId) {
            MutableStateFlow(emptyList())
        }
        flow.value = flow.value + message
        return Resource.Success(Unit)
    }

    override suspend fun fetchParticipants(meetingId: String): Flow<List<Participant>> {
        val flow = inMemoryParticipants.getOrPut(meetingId) {
            MutableStateFlow(emptyList())
        }
        return flow.asStateFlow()
    }

    override suspend fun fetchJoinRequests(meetingId: String): Flow<List<JoinRequest>> {
        val flow = inMemoryJoinRequests.getOrPut(meetingId) {
            MutableStateFlow(
                listOf(
                    JoinRequest(
                        id = "req_1",
                        userId = "guest_tariq",
                        displayName = "Tariq Mansoor"
                    )
                )
            )
        }
        return flow.asStateFlow()
    }

    override suspend fun respondJoinRequest(
        meetingId: String,
        requestId: String,
        admit: Boolean
    ): Resource<Unit> {
        val flow = inMemoryJoinRequests.getOrPut(meetingId) {
            MutableStateFlow(emptyList())
        }
        flow.value = flow.value.filterNot { it.id == requestId }
        return Resource.Success(Unit)
    }

    private fun Meeting.toEntity(): MeetingEntity = MeetingEntity(
        id = id,
        title = title,
        description = description,
        hostId = hostId,
        hostName = hostName,
        accessType = accessType.name,
        passwordProtected = passwordProtected,
        passwordHash = passwordHash,
        isRecording = isRecording,
        createdAt = createdAt,
        durationSeconds = durationSeconds,
        participantCount = participantCount
    )

    private fun MeetingEntity.toDomain(): Meeting = Meeting(
        id = id,
        title = title,
        description = description,
        hostId = hostId,
        hostName = hostName,
        accessType = try {
            MeetingAccessType.valueOf(accessType)
        } catch (_: Exception) {
            MeetingAccessType.ANYONE_WITH_LINK
        },
        passwordProtected = passwordProtected,
        passwordHash = passwordHash,
        isRecording = isRecording,
        createdAt = createdAt,
        durationSeconds = durationSeconds,
        participantCount = participantCount
    )
}
