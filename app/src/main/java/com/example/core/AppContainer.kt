package com.example.core

import android.content.Context
import com.example.data.repository.CallsRepositoryImpl
import com.example.data.repository.MeetingRepositoryImpl
import com.example.data.repository.ScheduledMeetingRepositoryImpl
import com.example.database.CallfriendsZDatabase
import com.example.domain.repository.CallsRepository
import com.example.domain.repository.MeetingRepository
import com.example.domain.repository.ScheduledMeetingRepository
import com.example.webrtc.DefaultMeetingClient
import com.example.webrtc.MeetingClient

class AppContainer(context: Context) {
    private val database = CallfriendsZDatabase.getDatabase(context)

    val meetingRepository: MeetingRepository = MeetingRepositoryImpl(database.meetingDao())
    val callsRepository: CallsRepository = CallsRepositoryImpl(database.callHistoryDao(), database.contactDao())
    val scheduledMeetingRepository: ScheduledMeetingRepository = ScheduledMeetingRepositoryImpl(database.scheduledMeetingDao())
    val meetingClient: MeetingClient = DefaultMeetingClient(context)
    val meetingNoteDao = database.meetingNoteDao()

    companion object {
        @Volatile
        private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
