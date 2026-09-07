package com.example.domain.repository

import com.example.core.Resource
import com.example.domain.model.HostControlsConfig
import com.example.domain.model.JoinRequest
import com.example.domain.model.Meeting
import com.example.domain.model.MeetingMessage
import com.example.domain.model.Participant
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    suspend fun createMeeting(meeting: Meeting): Resource<Meeting>
    suspend fun getMeetingById(id: String): Resource<Meeting>
    suspend fun getRecentMeetings(): Flow<List<Meeting>>
    suspend fun saveRecentMeeting(meeting: Meeting)
    suspend fun deleteRecentMeeting(id: String)
    suspend fun updateHostControls(meetingId: String, config: HostControlsConfig): Resource<Unit>
    suspend fun fetchMessages(meetingId: String): Flow<List<MeetingMessage>>
    suspend fun sendMessage(meetingId: String, message: MeetingMessage): Resource<Unit>
    suspend fun fetchParticipants(meetingId: String): Flow<List<Participant>>
    suspend fun fetchJoinRequests(meetingId: String): Flow<List<JoinRequest>>
    suspend fun respondJoinRequest(meetingId: String, requestId: String, admit: Boolean): Resource<Unit>
}
