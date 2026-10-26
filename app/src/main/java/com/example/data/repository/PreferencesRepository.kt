package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.RecordingMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("autocall_prefs", Context.MODE_PRIVATE)

    private val _recordingMode = MutableStateFlow(loadRecordingMode())
    val recordingMode: StateFlow<RecordingMode> = _recordingMode.asStateFlow()

    private val _audioFormat = MutableStateFlow(prefs.getString("audio_format", "m4a") ?: "m4a")
    val audioFormat: StateFlow<String> = _audioFormat.asStateFlow()

    private val _autoDeleteDays = MutableStateFlow(prefs.getInt("auto_delete_days", 0))
    val autoDeleteDays: StateFlow<Int> = _autoDeleteDays.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(prefs.getBoolean("notifications_enabled", true))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _highQualityAudio = MutableStateFlow(prefs.getBoolean("high_quality_audio", true))
    val highQualityAudio: StateFlow<Boolean> = _highQualityAudio.asStateFlow()

    private fun loadRecordingMode(): RecordingMode {
        val saved = prefs.getString("recording_mode", RecordingMode.ONLY_UNKNOWN_NUMBERS.name)
        return try {
            RecordingMode.valueOf(saved ?: RecordingMode.ONLY_UNKNOWN_NUMBERS.name)
        } catch (_: Exception) {
            RecordingMode.ONLY_UNKNOWN_NUMBERS
        }
    }

    fun setRecordingMode(mode: RecordingMode) {
        prefs.edit().putString("recording_mode", mode.name).apply()
        _recordingMode.value = mode
    }

    fun setAudioFormat(format: String) {
        prefs.edit().putString("audio_format", format).apply()
        _audioFormat.value = format
    }

    fun setAutoDeleteDays(days: Int) {
        prefs.edit().putInt("auto_delete_days", days).apply()
        _autoDeleteDays.value = days
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("notifications_enabled", enabled).apply()
        _notificationsEnabled.value = enabled
    }

    fun setHighQualityAudio(enabled: Boolean) {
        prefs.edit().putBoolean("high_quality_audio", enabled).apply()
        _highQualityAudio.value = enabled
    }
}
