package com.example.domain.repository

import com.example.core.Resource
import com.example.domain.model.CallRecord
import com.example.domain.model.ContactUser
import com.example.domain.model.ScheduledMeeting
import kotlinx.coroutines.flow.Flow

interface CallsRepository {
    fun getCallHistory(): Flow<List<CallRecord>>
    suspend fun addCallRecord(record: CallRecord)
    suspend fun clearHistory()
    suspend fun searchContacts(query: String): Resource<List<ContactUser>>
}

interface ScheduledMeetingRepository {
    fun getScheduledMeetings(): Flow<List<ScheduledMeeting>>
    suspend fun scheduleMeeting(meeting: ScheduledMeeting): Resource<ScheduledMeeting>
    suspend fun deleteScheduledMeeting(id: String): Resource<Unit>
}
