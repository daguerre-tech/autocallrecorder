package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "phone_number_rules")
data class PhoneNumberRule(
    @PrimaryKey
    val phoneNumber: String,
    val label: String = "",
    val ruleType: RuleType = RuleType.ALWAYS_RECORD,
    val addedTimestamp: Long = System.currentTimeMillis()
)
