package com.example.recorder

import android.content.Context
import com.example.data.local.CallRecorderDao
import com.example.data.model.PhoneNumberRule
import com.example.data.model.RecordingMode
import com.example.data.model.RuleType

data class FilterDecision(
    val shouldRecord: Boolean,
    val reason: String,
    val isOutsideContacts: Boolean,
    val contactName: String?,
    val ruleApplied: String
)

class RecordingFilterEngine(
    private val context: Context,
    private val dao: CallRecorderDao
) {

    suspend fun evaluateCall(
        phoneNumber: String,
        currentMode: RecordingMode
    ): FilterDecision {
        val contactName = ContactsHelper.getContactName(context, phoneNumber)
        val isOutsideContacts = contactName == null

        // 1. Check if the user set a specific custom rule for this exact phone number
        val customRule: PhoneNumberRule? = dao.getRuleForNumber(phoneNumber)
            ?: dao.getRuleForNumber(ContactsHelper.normalizePhoneNumber(phoneNumber))

        if (customRule != null) {
            return when (customRule.ruleType) {
                RuleType.ALWAYS_RECORD -> FilterDecision(
                    shouldRecord = true,
                    reason = "Règle spécifique : 'Toujours enregistrer ce numéro'",
                    isOutsideContacts = isOutsideContacts,
                    contactName = contactName ?: customRule.label.ifBlank { null },
                    ruleApplied = "CUSTOM_ALWAYS"
                )
                RuleType.NEVER_RECORD -> FilterDecision(
                    shouldRecord = false,
                    reason = "Règle spécifique : 'Exclu de l'enregistrement'",
                    isOutsideContacts = isOutsideContacts,
                    contactName = contactName ?: customRule.label.ifBlank { null },
                    ruleApplied = "CUSTOM_EXCLUDED"
                )
            }
        }

        // 2. Evaluate according to Global Mode
        return when (currentMode) {
            RecordingMode.DISABLED -> FilterDecision(
                shouldRecord = false,
                reason = "Enregistrement automatique désactivé dans les paramètres",
                isOutsideContacts = isOutsideContacts,
                contactName = contactName,
                ruleApplied = "DISABLED"
            )

            RecordingMode.ONLY_UNKNOWN_NUMBERS -> {
                if (isOutsideContacts) {
                    FilterDecision(
                        shouldRecord = true,
                        reason = "Numéro inconnu (Hors répertoire) - Déclenchement automatique",
                        isOutsideContacts = true,
                        contactName = null,
                        ruleApplied = "OUTSIDE_CONTACTS_AUTO"
                    )
                } else {
                    FilterDecision(
                        shouldRecord = false,
                        reason = "Contact existant '$contactName' ignoré (Mode 'Hors contacts uniquement' actif)",
                        isOutsideContacts = false,
                        contactName = contactName,
                        ruleApplied = "CONTACT_IGNORED"
                    )
                }
            }

            RecordingMode.ALL_CALLS -> FilterDecision(
                shouldRecord = true,
                reason = "Mode 'Tous les appels' actif",
                isOutsideContacts = isOutsideContacts,
                contactName = contactName,
                ruleApplied = "ALL_CALLS"
            )

            RecordingMode.CUSTOM_LIST_ONLY -> FilterDecision(
                shouldRecord = false,
                reason = "Mode 'Liste personnalisée' actif, mais ce numéro n'a pas de règle associée",
                isOutsideContacts = isOutsideContacts,
                contactName = contactName,
                ruleApplied = "NOT_IN_CUSTOM_LIST"
            )
        }
    }
}
