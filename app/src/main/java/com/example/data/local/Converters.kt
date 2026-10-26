package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.CallType
import com.example.data.model.RuleType

class Converters {
    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = try {
        CallType.valueOf(value)
    } catch (_: Exception) {
        CallType.INCOMING
    }

    @TypeConverter
    fun fromRuleType(value: RuleType): String = value.name

    @TypeConverter
    fun toRuleType(value: String): RuleType = try {
        RuleType.valueOf(value)
    } catch (_: Exception) {
        RuleType.ALWAYS_RECORD
    }
}
