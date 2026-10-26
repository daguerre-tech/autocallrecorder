package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.repository.CallRecorderRepository
import com.example.data.repository.PreferencesRepository
import com.example.recorder.AudioPlayerManager
import com.example.recorder.CallRecorderManager
import com.example.recorder.RecordingFilterEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallRecorderApplication : Application() {

    companion object {
        const val CHANNEL_ID_RECORDING = "call_recording_channel"
        const val CHANNEL_ID_ALERTS = "call_recording_alerts_channel"

        lateinit var instance: CallRecorderApplication
            private set
    }

    lateinit var database: AppDatabase
        private set
    lateinit var repository: CallRecorderRepository
        private set
    lateinit var preferencesRepository: PreferencesRepository
        private set
    lateinit var recorderManager: CallRecorderManager
        private set
    lateinit var audioPlayerManager: AudioPlayerManager
        private set
    lateinit var filterEngine: RecordingFilterEngine
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getDatabase(this)
        repository = CallRecorderRepository(database.callRecorderDao(), this)
        preferencesRepository = PreferencesRepository(this)
        recorderManager = CallRecorderManager(this, repository)
        audioPlayerManager = AudioPlayerManager(this)
        filterEngine = RecordingFilterEngine(this, database.callRecorderDao())

        createNotificationChannels()

        // Seed initial rules and sample if needed
        CoroutineScope(Dispatchers.IO).launch {
            repository.initializePrepopulatedDataIfNeeded()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val recChannel = NotificationChannel(
                CHANNEL_ID_RECORDING,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                "Alertes d'appels enregistrés",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications pour les appels inconnus nouvellement enregistrés"
            }

            notificationManager.createNotificationChannel(recChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }
}
