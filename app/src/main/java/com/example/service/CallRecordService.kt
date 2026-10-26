package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.CallRecorderApplication
import com.example.MainActivity
import com.example.R
import com.example.data.model.CallType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CallRecordService : Service() {

    companion object {
        const val ACTION_START_RECORDING = "com.example.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.action.STOP_RECORDING"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_CALL_TYPE = "extra_call_type"
        const val EXTRA_IS_OUTSIDE_CONTACTS = "extra_is_outside_contacts"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_RULE_APPLIED = "extra_rule_applied"

        const val NOTIFICATION_ID = 1001

        fun start(
            context: Context,
            phoneNumber: String,
            callType: CallType,
            isOutsideContacts: Boolean,
            contactName: String?,
            ruleApplied: String
        ) {
            val intent = Intent(context, CallRecordService::class.java).apply {
                action = ACTION_START_RECORDING
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(EXTRA_CALL_TYPE, callType.name)
                putExtra(EXTRA_IS_OUTSIDE_CONTACTS, isOutsideContacts)
                putExtra(EXTRA_CONTACT_NAME, contactName)
                putExtra(EXTRA_RULE_APPLIED, ruleApplied)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, CallRecordService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER) ?: "Inconnu"
                val callTypeName = intent.getStringExtra(EXTRA_CALL_TYPE) ?: CallType.INCOMING.name
                val isOutside = intent.getBooleanExtra(EXTRA_IS_OUTSIDE_CONTACTS, true)
                val contactName = intent.getStringExtra(EXTRA_CONTACT_NAME)
                val ruleApplied = intent.getStringExtra(EXTRA_RULE_APPLIED) ?: "OUTSIDE_CONTACTS_AUTO"
                val callType = try { CallType.valueOf(callTypeName) } catch (_: Exception) { CallType.INCOMING }

                val notification = buildForegroundNotification(phoneNumber, isOutside, 0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                    )
                } else {
                    startForeground(NOTIFICATION_ID, notification)
                }

                val app = CallRecorderApplication.instance
                app.recorderManager.startRecording(
                    phoneNumber = phoneNumber,
                    contactName = contactName,
                    isOutsideContacts = isOutside,
                    callType = callType,
                    ruleApplied = ruleApplied,
                    format = app.preferencesRepository.audioFormat.value
                )

                observeDuration(phoneNumber, isOutside)
            }

            ACTION_STOP_RECORDING -> {
                stopActiveRecording()
            }
        }
        return START_NOT_STICKY
    }

    private fun observeDuration(phoneNumber: String, isOutside: Boolean) {
        serviceScope.launch {
            CallRecorderApplication.instance.recorderManager.recordingDurationSeconds.collectLatest { seconds ->
                val notification = buildForegroundNotification(phoneNumber, isOutside, seconds)
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }
    }

    private fun stopActiveRecording() {
        serviceScope.launch {
            CallRecorderApplication.instance.recorderManager.stopRecording()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun buildForegroundNotification(
        phoneNumber: String,
        isOutside: Boolean,
        seconds: Int
    ): Notification {
        val appIntent = Intent(this, MainActivity::class.java)
        val pendingAppIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, CallRecordService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val pendingStopIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mins = seconds / 60
        val secs = seconds % 60
        val timeStr = String.format("%02d:%02d", mins, secs)

        val badgeText = if (isOutside) "• Numéro HORS RÉPERTOIRE" else "• Contact enregistré"

        return NotificationCompat.Builder(this, CallRecorderApplication.CHANNEL_ID_RECORDING)
            .setContentTitle("🔴 Enregistrement d'appel en cours ($timeStr)")
            .setContentText("$phoneNumber $badgeText")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingAppIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Arrêter", pendingStopIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
