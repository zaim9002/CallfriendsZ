package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.CallHistoryDao
import com.example.database.dao.ContactDao
import com.example.database.dao.MeetingDao
import com.example.database.dao.MeetingNoteDao
import com.example.database.dao.ScheduledMeetingDao
import com.example.database.entity.CallHistoryEntity
import com.example.database.entity.ContactEntity
import com.example.database.entity.MeetingEntity
import com.example.database.entity.MeetingNoteEntity
import com.example.database.entity.ScheduledMeetingEntity

@Database(
    entities = [
        MeetingEntity::class,
        CallHistoryEntity::class,
        ScheduledMeetingEntity::class,
        ContactEntity::class,
        MeetingNoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class CallfriendsZDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun scheduledMeetingDao(): ScheduledMeetingDao
    abstract fun contactDao(): ContactDao
    abstract fun meetingNoteDao(): MeetingNoteDao

    companion object {
        @Volatile
        private var INSTANCE: CallfriendsZDatabase? = null

        fun getDatabase(context: Context): CallfriendsZDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CallfriendsZDatabase::class.java,
                    "callfriendsz_offline.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
