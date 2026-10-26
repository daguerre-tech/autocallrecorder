package com.example.data.repository

import android.content.Context
import com.example.data.local.CallRecorderDao
import com.example.data.model.CallRecording
import com.example.data.model.CallType
import com.example.data.model.PhoneNumberRule
import com.example.data.model.RuleType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class CallRecorderRepository(
    private val dao: CallRecorderDao,
    private val context: Context
) {
    val allRecordings: Flow<List<CallRecording>> = dao.getAllRecordings()
    val outsideContactsRecordings: Flow<List<CallRecording>> = dao.getOutsideContactsRecordings()
    val starredRecordings: Flow<List<CallRecording>> = dao.getStarredRecordings()
    val allRules: Flow<List<PhoneNumberRule>> = dao.getAllRules()

    fun searchRecordings(query: String): Flow<List<CallRecording>> = dao.searchRecordings(query)

    suspend fun getRecordingById(id: Long): CallRecording? = dao.getRecordingById(id)

    suspend fun insertRecording(recording: CallRecording): Long = withContext(Dispatchers.IO) {
        dao.insertRecording(recording)
    }

    suspend fun updateRecording(recording: CallRecording) = withContext(Dispatchers.IO) {
        dao.updateRecording(recording)
    }

    suspend fun deleteRecording(recording: CallRecording) = withContext(Dispatchers.IO) {
        // Also remove physical file if it exists
        try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {}
        dao.deleteRecording(recording)
    }

    suspend fun deleteAllRecordings() = withContext(Dispatchers.IO) {
        try {
            val dir = getRecordingsDirectory()
            dir.listFiles()?.forEach { it.delete() }
        } catch (_: Exception) {}
        dao.deleteAllRecordings()
    }

    suspend fun toggleStar(recording: CallRecording) = withContext(Dispatchers.IO) {
        dao.updateRecording(recording.copy(isStarred = !recording.isStarred))
    }

    suspend fun updateNotes(recordingId: Long, notes: String) = withContext(Dispatchers.IO) {
        val existing = dao.getRecordingById(recordingId)
        if (existing != null) {
            dao.updateRecording(existing.copy(notes = notes))
        }
    }

    suspend fun getRuleForNumber(phoneNumber: String): PhoneNumberRule? = withContext(Dispatchers.IO) {
        dao.getRuleForNumber(phoneNumber)
    }

    suspend fun saveRule(rule: PhoneNumberRule) = withContext(Dispatchers.IO) {
        dao.insertRule(rule)
    }

    suspend fun deleteRule(rule: PhoneNumberRule) = withContext(Dispatchers.IO) {
        dao.deleteRule(rule)
    }

    fun getRecordingsDirectory(): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun getTotalStorageSizeBytes(): Long = withContext(Dispatchers.IO) {
        val dir = getRecordingsDirectory()
        var total = 0L
        dir.listFiles()?.forEach { file ->
            if (file.isFile) {
                total += file.length()
            }
        }
        total
    }

    suspend fun initializePrepopulatedDataIfNeeded() = withContext(Dispatchers.IO) {
        // Add default sample rules and sample recordings demonstrating unknown number detection
        val rules = dao.getRuleForNumber("+33 7 00 11 22 33")
        if (rules == null) {
            dao.insertRule(
                PhoneNumberRule(
                    phoneNumber = "+33 7 00 11 22 33",
                    label = "Service Livraison Express",
                    ruleType = RuleType.ALWAYS_RECORD,
                    addedTimestamp = System.currentTimeMillis() - 86400000L
                )
            )
            dao.insertRule(
                PhoneNumberRule(
                    phoneNumber = "+33 8 99 00 11 22",
                    label = "Numéro surtaxé / Démarchage",
                    ruleType = RuleType.NEVER_RECORD,
                    addedTimestamp = System.currentTimeMillis() - 172800000L
                )
            )
        }
    }
}
