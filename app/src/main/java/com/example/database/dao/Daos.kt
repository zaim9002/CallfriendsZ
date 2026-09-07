package com.example.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.database.entity.CallHistoryEntity
import com.example.database.entity.MeetingEntity
import com.example.database.entity.ScheduledMeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM recent_meetings ORDER BY createdAt DESC")
    fun getAllRecentMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM recent_meetings WHERE id = :id LIMIT 1")
    suspend fun getMeetingById(id: String): MeetingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Query("DELETE FROM recent_meetings WHERE id = :id")
    suspend fun deleteMeeting(id: String)
}

@Dao
interface CallHistoryDao {
    @Query("SELECT * FROM call_history ORDER BY timestamp DESC")
    fun getAllCallHistory(): Flow<List<CallHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallHistoryEntity)

    @Query("DELETE FROM call_history")
    suspend fun clearAll()
}

@Dao
interface ScheduledMeetingDao {
    @Query("SELECT * FROM scheduled_meetings ORDER BY dateString ASC, startTime ASC")
    fun getScheduledMeetings(): Flow<List<ScheduledMeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledMeeting(meeting: ScheduledMeetingEntity)

    @Query("DELETE FROM scheduled_meetings WHERE id = :id")
    suspend fun deleteScheduledMeeting(id: String)
}
