package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.CallRecorderApplication
import com.example.data.model.CallType
import com.example.service.CallRecordService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CallStateReceiver : BroadcastReceiver() {

    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var savedNumber: String? = null
        private var isCallIncoming = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return

        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            if (!incomingNumber.isNullOrBlank()) {
                savedNumber = incomingNumber
            }

            if (stateStr == null || stateStr == lastState) return

            when (stateStr) {
                TelephonyManager.EXTRA_STATE_RINGING -> {
                    isCallIncoming = true
                }

                TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                    // Call answered (either incoming or outgoing)
                    val number = savedNumber ?: "Inconnu"
                    val callType = if (isCallIncoming) CallType.INCOMING else CallType.OUTGOING

                    handleCallStarted(context, number, callType)
                }

                TelephonyManager.EXTRA_STATE_IDLE -> {
                    // Call ended / hung up
                    if (lastState == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        CallRecordService.stop(context)
                    }
                    isCallIncoming = false
                    savedNumber = null
                }
            }

            lastState = stateStr
        }
    }

    private fun handleCallStarted(context: Context, phoneNumber: String, callType: CallType) {
        val app = CallRecorderApplication.instance
        val currentMode = app.preferencesRepository.recordingMode.value

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val decision = app.filterEngine.evaluateCall(phoneNumber, currentMode)
                Log.d("CallStateReceiver", "Filter decision for $phoneNumber: shouldRecord=${decision.shouldRecord}, reason=${decision.reason}")

                if (decision.shouldRecord) {
                    CallRecordService.start(
                        context = context,
                        phoneNumber = phoneNumber,
                        callType = callType,
                        isOutsideContacts = decision.isOutsideContacts,
                        contactName = decision.contactName,
                        ruleApplied = decision.ruleApplied
                    )
                }
            } catch (e: Exception) {
                Log.e("CallStateReceiver", "Error evaluating call state", e)
            }
        }
    }
}
