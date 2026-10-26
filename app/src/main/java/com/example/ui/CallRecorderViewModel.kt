package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CallRecorderApplication
import com.example.data.model.CallRecording
import com.example.data.model.CallType
import com.example.data.model.PhoneNumberRule
import com.example.data.model.RecordingMode
import com.example.data.model.RuleType
import com.example.recorder.FilterDecision
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecordingFilterTab {
    ALL,
    OUTSIDE_CONTACTS,
    STARRED
}

enum class SimCallStatus {
    IDLE,
    RINGING,
    CONNECTED,
    ENDED
}

data class CallSimulatorState(
    val status: SimCallStatus = SimCallStatus.IDLE,
    val phoneNumber: String = "+33 7 94 82 10 55",
    val callType: CallType = CallType.INCOMING,
    val filterDecision: FilterDecision? = null,
    val isRecordingActive: Boolean = false,
    val elapsedSeconds: Int = 0,
    val amplitude: Float = 0f,
    val statusMessage: String = "Prêt à tester la détection d'appels"
)

class CallRecorderViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CallRecorderApplication
    private val repository = app.repository
    private val prefs = app.preferencesRepository
    val recorderManager = app.recorderManager
    val audioPlayerManager = app.audioPlayerManager
    val filterEngine = app.filterEngine

    // Search and filter
    val searchQuery = MutableStateFlow("")
    val selectedFilterTab = MutableStateFlow(RecordingFilterTab.ALL)

    val recordingMode: StateFlow<RecordingMode> = prefs.recordingMode
    val audioFormat: StateFlow<String> = prefs.audioFormat
    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled
    val highQualityAudio: StateFlow<Boolean> = prefs.highQualityAudio

    val allRules: StateFlow<List<PhoneNumberRule>> = repository.allRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered list of recordings
    val recordings: StateFlow<List<CallRecording>> = combine(
        repository.allRecordings,
        searchQuery,
        selectedFilterTab
    ) { list, query, tab ->
        var filtered = when (tab) {
            RecordingFilterTab.ALL -> list
            RecordingFilterTab.OUTSIDE_CONTACTS -> list.filter { it.isOutsideContacts }
            RecordingFilterTab.STARRED -> list.filter { it.isStarred }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            filtered = filtered.filter { rec ->
                rec.phoneNumber.lowercase().contains(q) ||
                        (rec.contactName?.lowercase()?.contains(q) == true) ||
                        rec.notes.lowercase().contains(q)
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Storage info
    private val _totalStorageBytes = MutableStateFlow(0L)
    val totalStorageBytes: StateFlow<Long> = _totalStorageBytes.asStateFlow()

    // Call Simulator
    private val _simulatorState = MutableStateFlow(CallSimulatorState())
    val simulatorState: StateFlow<CallSimulatorState> = _simulatorState.asStateFlow()
    private var simTimerJob: Job? = null

    // Number rule test result
    private val _ruleTestResult = MutableStateFlow<FilterDecision?>(null)
    val ruleTestResult: StateFlow<FilterDecision?> = _ruleTestResult.asStateFlow()

    init {
        refreshStorageInfo()
    }

    fun refreshStorageInfo() {
        viewModelScope.launch {
            _totalStorageBytes.value = repository.getTotalStorageSizeBytes()
        }
    }

    fun setRecordingMode(mode: RecordingMode) {
        prefs.setRecordingMode(mode)
    }

    fun setAudioFormat(format: String) {
        prefs.setAudioFormat(format)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.setNotificationsEnabled(enabled)
    }

    fun setHighQualityAudio(enabled: Boolean) {
        prefs.setHighQualityAudio(enabled)
    }

    fun toggleStar(recording: CallRecording) {
        viewModelScope.launch {
            repository.toggleStar(recording)
        }
    }

    fun updateNotes(recordingId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateNotes(recordingId, notes)
        }
    }

    fun deleteRecording(recording: CallRecording) {
        viewModelScope.launch {
            if (audioPlayerManager.playbackState.value.currentRecordingId == recording.id) {
                audioPlayerManager.stop()
            }
            repository.deleteRecording(recording)
            refreshStorageInfo()
        }
    }

    fun deleteAllRecordings() {
        viewModelScope.launch {
            audioPlayerManager.stop()
            repository.deleteAllRecordings()
            refreshStorageInfo()
        }
    }

    fun savePhoneNumberRule(phoneNumber: String, label: String, ruleType: RuleType) {
        viewModelScope.launch {
            repository.saveRule(
                PhoneNumberRule(
                    phoneNumber = phoneNumber.trim(),
                    label = label.trim(),
                    ruleType = ruleType
                )
            )
        }
    }

    fun deleteRule(rule: PhoneNumberRule) {
        viewModelScope.launch {
            repository.deleteRule(rule)
        }
    }

    fun testNumberEvaluation(phoneNumber: String) {
        viewModelScope.launch {
            if (phoneNumber.isBlank()) {
                _ruleTestResult.value = null
                return@launch
            }
            val decision = filterEngine.evaluateCall(phoneNumber, recordingMode.value)
            _ruleTestResult.value = decision
        }
    }

    // Call Simulator Studio Actions
    fun updateSimNumber(number: String) {
        _simulatorState.value = _simulatorState.value.copy(phoneNumber = number)
    }

    fun updateSimCallType(type: CallType) {
        _simulatorState.value = _simulatorState.value.copy(callType = type)
    }

    fun startSimulatedCall(number: String, type: CallType) {
        viewModelScope.launch {
            val decision = filterEngine.evaluateCall(number, recordingMode.value)
            _simulatorState.value = CallSimulatorState(
                status = SimCallStatus.RINGING,
                phoneNumber = number,
                callType = type,
                filterDecision = decision,
                isRecordingActive = false,
                elapsedSeconds = 0,
                statusMessage = "Sonnerie en cours… Détection du numéro"
            )

            // Simulate 1.5 seconds of ringing before auto-connect
            delay(1500)
            answerSimulatedCall()
        }
    }

    private fun answerSimulatedCall() {
        val state = _simulatorState.value
        val decision = state.filterDecision ?: return

        var willRecord = decision.shouldRecord
        if (willRecord) {
            recorderManager.startRecording(
                phoneNumber = state.phoneNumber,
                contactName = decision.contactName,
                isOutsideContacts = decision.isOutsideContacts,
                callType = state.callType,
                ruleApplied = decision.ruleApplied,
                format = audioFormat.value
            )
        }

        val msg = if (willRecord) {
            "Appel décroché 🔴 ENREGISTREMENT AUTOMATIQUE ACTIF (${decision.reason})"
        } else {
            "Appel décroché ⚪ Enregistrement ignoré (${decision.reason})"
        }

        _simulatorState.value = state.copy(
            status = SimCallStatus.CONNECTED,
            isRecordingActive = willRecord,
            statusMessage = msg
        )

        simTimerJob?.cancel()
        simTimerJob = viewModelScope.launch {
            var secs = 0
            while (_simulatorState.value.status == SimCallStatus.CONNECTED) {
                delay(1000)
                secs++
                val amp = if (willRecord) recorderManager.currentAmplitude.value else 0.1f
                _simulatorState.value = _simulatorState.value.copy(
                    elapsedSeconds = secs,
                    amplitude = amp
                )
            }
        }
    }

    fun hangupSimulatedCall() {
        simTimerJob?.cancel()
        simTimerJob = null

        viewModelScope.launch {
            val wasRecording = _simulatorState.value.isRecordingActive
            if (wasRecording) {
                val savedRec = recorderManager.stopRecording()
                refreshStorageInfo()
                _simulatorState.value = _simulatorState.value.copy(
                    status = SimCallStatus.ENDED,
                    isRecordingActive = false,
                    statusMessage = "Appel terminé. Enregistrement sauvegardé avec succès dans le stockage du téléphone !"
                )
            } else {
                _simulatorState.value = _simulatorState.value.copy(
                    status = SimCallStatus.ENDED,
                    isRecordingActive = false,
                    statusMessage = "Appel terminé. Aucun enregistrement créé (Numéro exclu/filtré)."
                )
            }
        }
    }

    fun resetSimulator() {
        simTimerJob?.cancel()
        simTimerJob = null
        _simulatorState.value = CallSimulatorState()
    }
}
