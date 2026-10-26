package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val isOutsideContacts: Boolean = true,
    val callType: CallType = CallType.INCOMING,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val filePath: String,
    val fileSizeBytes: Long = 0L,
    val isStarred: Boolean = false,
    val notes: String = "",
    val recordingRuleApplied: String = "NON_CONTACT"
)
