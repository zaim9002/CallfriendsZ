package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.database.dao.CallHistoryDao
import com.example.database.dao.MeetingDao
import com.example.database.dao.ScheduledMeetingDao
import com.example.database.entity.CallHistoryEntity
import com.example.database.entity.MeetingEntity
import com.example.database.entity.ScheduledMeetingEntity

@Database(
    entities = [
        MeetingEntity::class,
        CallHistoryEntity::class,
        ScheduledMeetingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CallfriendsZDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun callHistoryDao(): CallHistoryDao
    abstract fun scheduledMeetingDao(): ScheduledMeetingDao

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
