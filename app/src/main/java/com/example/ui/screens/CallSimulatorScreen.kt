package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.recorder.ContactsHelper
import com.example.ui.CallRecorderViewModel
import com.example.ui.SimCallStatus
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RecordRed

@Composable
fun CallSimulatorScreen(
    viewModel: CallRecorderViewModel,
    onNavigateToRecordings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val simState by viewModel.simulatorState.collectAsState()
    val recordingMode by viewModel.recordingMode.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("call_simulator_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Laboratoire d'Appels",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Testez en direct la réaction automatique de l'application selon que le numéro est enregistré ou inconnu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Active Simulator Phone Mockup Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("simulator_phone_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (simState.status) {
                        SimCallStatus.IDLE -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Prêt pour le test",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Sélectionnez un numéro ci-dessous et lancez l'appel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SimCallStatus.RINGING -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Appel en cours de sonnerie…",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = ContactsHelper.formatDisplayNumber(simState.phoneNumber),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        SimCallStatus.CONNECTED -> {
                            val mins = simState.elapsedSeconds / 60
                            val secs = simState.elapsedSeconds % 60
                            val timeStr = String.format("%02d:%02d", mins, secs)

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (simState.isRecordingActive) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(RecordRed)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "ENREGISTREMENT ACTIF • $timeStr",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RecordRed
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.outline)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "APPEL EN COURS (NON ENREGISTRÉ) • $timeStr",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = ContactsHelper.formatDisplayNumber(simState.phoneNumber),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Status badge
                            simState.filterDecision?.let { dec ->
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (dec.shouldRecord) RecordRed.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f),
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = if (dec.isOutsideContacts) "⚠️ NUMÉRO HORS CONTACTS DÉTECTÉ" else "👤 CONTACT DU RÉPERTOIRE (${dec.contactName})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dec.shouldRecord) RecordRed else EmeraldSuccess,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Waveform if recording
                            Spacer(modifier = Modifier.height(16.dp))
                            WaveformVisualizer(
                                amplitude = simState.amplitude,
                                isRecording = simState.isRecordingActive,
                                height = 48.dp
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                            // Hang up button
                            Button(
                                onClick = { viewModel.hangupSimulatedCall() },
                                colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("hangup_button")
                            ) {
                                Icon(Icons.Default.CallEnd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Raccrocher l'appel", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        SimCallStatus.ENDED -> {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Appel terminé",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = simState.statusMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.resetSimulator() },
                                    modifier = Modifier.weight(1f).testTag("reset_sim_button")
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Recommencer")
                                }

                                Button(
                                    onClick = onNavigateToRecordings,
                                    modifier = Modifier.weight(1f).testTag("view_recordings_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Écouter")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Test configuration (Only displayed when IDLE)
        if (simState.status == SimCallStatus.IDLE) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Paramètres du test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Direction (Incoming / Outgoing)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = simState.callType == CallType.INCOMING,
                                onClick = { viewModel.updateSimCallType(CallType.INCOMING) },
                                label = { Text("Appel Entrant 📥") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = simState.callType == CallType.OUTGOING,
                                onClick = { viewModel.updateSimCallType(CallType.OUTGOING) },
                                label = { Text("Appel Sortant 📤") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Presets
                        Text(
                            text = "Sélectionnez un scénario d'appel :",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        PresetOption(
                            title = "⚠️ Numéro Inconnu (Hors contacts)",
                            number = "+33 7 94 82 10 55",
                            badge = "Sera enregistré",
                            isBadgedDanger = true,
                            onClick = { viewModel.updateSimNumber("+33 7 94 82 10 55") }
                        )

                        PresetOption(
                            title = "⚠️ Numéro Démarchage / Spam Inconnu",
                            number = "+33 1 89 22 10 00",
                            badge = "Sera enregistré",
                            isBadgedDanger = true,
                            onClick = { viewModel.updateSimNumber("+33 1 89 22 10 00") }
                        )

                        PresetOption(
                            title = "👤 Sophie Martin (Contact enregistré)",
                            number = "+33 6 12 34 56 78",
                            badge = "Ignoré (Contact)",
                            isBadgedDanger = false,
                            onClick = { viewModel.updateSimNumber("+33 6 12 34 56 78") }
                        )

                        // Custom number input
                        OutlinedTextField(
                            value = simState.phoneNumber,
                            onValueChange = { viewModel.updateSimNumber(it) },
                            label = { Text("Ou numéro personnalisé") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sim_phone_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        // Trigger Call Button
                        Button(
                            onClick = {
                                viewModel.startSimulatedCall(simState.phoneNumber, simState.callType)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_sim_call_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Lancer l'appel test", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetOption(
    title: String,
    number: String,
    badge: String,
    isBadgedDanger: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(text = number, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isBadgedDanger) RecordRed.copy(alpha = 0.15f) else EmeraldSuccess.copy(alpha = 0.15f)
            ) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isBadgedDanger) RecordRed else EmeraldSuccess,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }
        }
    }
}
