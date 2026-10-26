package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PermContactCalendar
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PhoneNumberRule
import com.example.data.model.RecordingMode
import com.example.data.model.RuleType
import com.example.recorder.ContactsHelper
import com.example.ui.CallRecorderViewModel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RecordRed

@Composable
fun NumberRulesScreen(
    viewModel: CallRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val activeMode by viewModel.recordingMode.collectAsState()
    val allRules by viewModel.allRules.collectAsState()
    val testResult by viewModel.ruleTestResult.collectAsState()

    var testNumberInput by remember { mutableStateOf("+33 7 99 88 77 66") }
    var showAddRuleDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("number_rules_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Title: Mode d'enregistrement automatique
        item {
            Column {
                Text(
                    text = "Système d'Enregistrement",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Configurez la règle de déclenchement selon les numéros de téléphone appelants.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Global Modes List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ModeSelectionCard(
                    title = "🛡️ Numéros hors répertoire uniquement",
                    subtitle = "Recommandé : Enregistre automatiquement tout numéro inconnu qui n'est pas sauvegardé dans vos contacts.",
                    isSelected = activeMode == RecordingMode.ONLY_UNKNOWN_NUMBERS,
                    badgeText = "ACTIF PAR DÉFAUT",
                    onClick = { viewModel.setRecordingMode(RecordingMode.ONLY_UNKNOWN_NUMBERS) },
                    testTag = "mode_outside_contacts"
                )

                ModeSelectionCard(
                    title = "📞 Tous les appels",
                    subtitle = "Enregistre tous les appels entrants et sortants (contacts connus et inconnus).",
                    isSelected = activeMode == RecordingMode.ALL_CALLS,
                    badgeText = null,
                    onClick = { viewModel.setRecordingMode(RecordingMode.ALL_CALLS) },
                    testTag = "mode_all_calls"
                )

                ModeSelectionCard(
                    title = "📋 Liste personnalisée",
                    subtitle = "Enregistre uniquement les numéros enregistrés dans votre liste de surveillance ci-dessous.",
                    isSelected = activeMode == RecordingMode.CUSTOM_LIST_ONLY,
                    badgeText = null,
                    onClick = { viewModel.setRecordingMode(RecordingMode.CUSTOM_LIST_ONLY) },
                    testTag = "mode_custom_list"
                )

                ModeSelectionCard(
                    title = "⏸️ Désactivé",
                    subtitle = "Suspendre temporairement tout enregistrement d'appel.",
                    isSelected = activeMode == RecordingMode.DISABLED,
                    badgeText = null,
                    onClick = { viewModel.setRecordingMode(RecordingMode.DISABLED) },
                    testTag = "mode_disabled"
                )
            }
        }

        // Section: Live Number Rule Tester
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rule_tester_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Testeur de détection de numéro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Saisissez un numéro pour vérifier instantanément s'il est considéré comme 'Hors répertoire' et s'il sera enregistré.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = testNumberInput,
                            onValueChange = { testNumberInput = it },
                            label = { Text("Numéro à tester") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_number_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { viewModel.testNumberEvaluation(testNumberInput) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("test_number_button")
                        ) {
                            Text("Tester")
                        }
                    }

                    // Test Result Display
                    testResult?.let { result ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (result.shouldRecord) RecordRed.copy(alpha = 0.1f) else EmeraldSuccess.copy(alpha = 0.1f),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = androidx.compose.ui.graphics.SolidColor(
                                    if (result.shouldRecord) RecordRed else EmeraldSuccess
                                )
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (result.shouldRecord) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (result.shouldRecord) RecordRed else EmeraldSuccess,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (result.shouldRecord) "SERA ENREGISTRÉ AUTOMATIQUEMENT" else "NE SERA PAS ENREGISTRÉ",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (result.shouldRecord) RecordRed else EmeraldSuccess
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Statut : ${if (result.isOutsideContacts) "⚠️ Hors contacts (Numéro inconnu)" else "👤 Contact connu (${result.contactName})"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Raison : ${result.reason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Custom Phone Number Rules
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Règles Spécifiques par Numéro",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Forcer l'enregistrement ou l'exclusion de numéros précis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddRuleDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("add_rule_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ajouter")
                }
            }
        }

        // Custom Rules List
        if (allRules.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Aucune règle spécifique. Le mode global ci-dessus s'applique à tous vos appels.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(items = allRules, key = { it.phoneNumber }) { rule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_item_${rule.phoneNumber}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rule.ruleType == RuleType.ALWAYS_RECORD) RecordRed.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (rule.ruleType == RuleType.ALWAYS_RECORD) Icons.Default.Shield else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (rule.ruleType == RuleType.ALWAYS_RECORD) RecordRed else EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = ContactsHelper.formatDisplayNumber(rule.phoneNumber),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (rule.label.isNotBlank()) {
                                Text(
                                    text = rule.label,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (rule.ruleType == RuleType.ALWAYS_RECORD) "🔴 Toujours enregistrer" else "🟢 Ne jamais enregistrer",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (rule.ruleType == RuleType.ALWAYS_RECORD) RecordRed else EmeraldSuccess
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteRule(rule) },
                            modifier = Modifier.testTag("delete_rule_${rule.phoneNumber}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Supprimer la règle",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (showAddRuleDialog) {
        var newNumber by remember { mutableStateOf("") }
        var newLabel by remember { mutableStateOf("") }
        var selectedType by remember { mutableStateOf(RuleType.ALWAYS_RECORD) }

        AlertDialog(
            onDismissRequest = { showAddRuleDialog = false },
            title = { Text("Nouvelle Règle de Numéro") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newNumber,
                        onValueChange = { newNumber = it },
                        label = { Text("Numéro de téléphone (ex: +33 6...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("Étiquette (ex: Client important, Démarchage)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(
                        text = "Action appliquée :",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = RuleType.ALWAYS_RECORD },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == RuleType.ALWAYS_RECORD,
                            onClick = { selectedType = RuleType.ALWAYS_RECORD }
                        )
                        Text(
                            text = "🔴 Toujours enregistrer cet appel",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedType = RuleType.NEVER_RECORD },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == RuleType.NEVER_RECORD,
                            onClick = { selectedType = RuleType.NEVER_RECORD }
                        )
                        Text(
                            text = "🟢 Ne jamais enregistrer (Exclure)",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newNumber.isNotBlank()) {
                            viewModel.savePhoneNumberRule(newNumber, newLabel, selectedType)
                            showAddRuleDialog = false
                        }
                    }
                ) {
                    Text("Ajouter la règle")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRuleDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun ModeSelectionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    badgeText: String?,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    badgeText?.let {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
