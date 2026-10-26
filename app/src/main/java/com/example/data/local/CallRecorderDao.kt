package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallRecording
import com.example.data.model.PhoneNumberRule
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecorderDao {
    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE isOutsideContacts = 1 ORDER BY timestamp DESC")
    fun getOutsideContactsRecordings(): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE isStarred = 1 ORDER BY timestamp DESC")
    fun getStarredRecordings(): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE phoneNumber LIKE '%' || :query || '%' OR (contactName IS NOT NULL AND contactName LIKE '%' || :query || '%') OR notes LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchRecordings(query: String): Flow<List<CallRecording>>

    @Query("SELECT * FROM call_recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Long): CallRecording?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: CallRecording): Long

    @Update
    suspend fun updateRecording(recording: CallRecording)

    @Delete
    suspend fun deleteRecording(recording: CallRecording)

    @Query("DELETE FROM call_recordings WHERE id IN (:ids)")
    suspend fun deleteRecordingsByIds(ids: List<Long>)

    @Query("DELETE FROM call_recordings")
    suspend fun deleteAllRecordings()

    @Query("SELECT * FROM call_recordings WHERE timestamp < :thresholdTime")
    suspend fun getOldRecordings(thresholdTime: Long): List<CallRecording>

    @Query("SELECT * FROM phone_number_rules ORDER BY addedTimestamp DESC")
    fun getAllRules(): Flow<List<PhoneNumberRule>>

    @Query("SELECT * FROM phone_number_rules WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getRuleForNumber(phoneNumber: String): PhoneNumberRule?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRule(rule: PhoneNumberRule)

    @Delete
    suspend fun deleteRule(rule: PhoneNumberRule)

    @Query("DELETE FROM phone_number_rules WHERE phoneNumber = :phoneNumber")
    suspend fun deleteRuleByNumber(phoneNumber: String)
}
