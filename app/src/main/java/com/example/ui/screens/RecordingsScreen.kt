package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RecordingMode
import com.example.recorder.ContactsHelper
import com.example.ui.CallRecorderViewModel
import com.example.ui.RecordingFilterTab
import com.example.ui.components.RecordingCard
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.RecordRed
import com.example.ui.theme.Slate700

@Composable
fun RecordingsScreen(
    viewModel: CallRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedTab by viewModel.selectedFilterTab.collectAsState()
    val recordingMode by viewModel.recordingMode.collectAsState()
    val isRecordingActive by viewModel.recorderManager.isRecording.collectAsState()
    val activeSession by viewModel.recorderManager.currentActiveSession.collectAsState()
    val activeSeconds by viewModel.recorderManager.recordingDurationSeconds.collectAsState()
    val amplitude by viewModel.recorderManager.currentAmplitude.collectAsState()
    val playbackState by viewModel.audioPlayerManager.playbackState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recordings_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Hero Visual Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.call_recorder_banner_1793007620223),
                    contentDescription = "Bannière enregistrement d'appels",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = when (recordingMode) {
                                RecordingMode.ONLY_UNKNOWN_NUMBERS -> "🛡️ Filtre actif : Numéros hors répertoire"
                                RecordingMode.ALL_CALLS -> "📞 Tous les appels enregistrés"
                                RecordingMode.CUSTOM_LIST_ONLY -> "📋 Liste personnalisée uniquement"
                                RecordingMode.DISABLED -> "⏸️ Enregistrement suspendu"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "Enregistrements Locaux",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Active Recording Live Banner (if in call)
        item {
            AnimatedVisibility(
                visible = isRecordingActive,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("active_recording_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = RecordRed.copy(alpha = 0.15f)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(RecordRed)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(RecordRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ENREGISTREMENT EN COURS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = RecordRed
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            val mins = activeSeconds / 60
                            val secs = activeSeconds % 60
                            Text(
                                text = String.format("%02d:%02d", mins, secs),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = RecordRed
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = ContactsHelper.formatDisplayNumber(activeSession?.phoneNumber ?: "Numéro en cours"),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (activeSession?.isOutsideContacts == true) {
                            Text(
                                text = "⚠️ Numéro hors de vos contacts enregistrés",
                                style = MaterialTheme.typography.bodySmall,
                                color = RecordRed
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        WaveformVisualizer(
                            amplitude = amplitude,
                            isRecording = true,
                            height = 36.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                kotlinx.coroutines.runBlocking {
                                    viewModel.recorderManager.stopRecording()
                                    viewModel.refreshStorageInfo()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RecordRed),
                            modifier = Modifier.fillMaxWidth().testTag("stop_recording_button")
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Arrêter et sauvegarder")
                        }
                    }
                }
            }
        }

        // Search Bar & Filter Chips
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Rechercher un numéro, nom ou note…") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Recherche",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Effacer")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == RecordingFilterTab.ALL,
                        onClick = { viewModel.selectedFilterTab.value = RecordingFilterTab.ALL },
                        label = { Text("Tous") },
                        modifier = Modifier.testTag("filter_all")
                    )

                    FilterChip(
                        selected = selectedTab == RecordingFilterTab.OUTSIDE_CONTACTS,
                        onClick = { viewModel.selectedFilterTab.value = RecordingFilterTab.OUTSIDE_CONTACTS },
                        label = { Text("Hors contacts 🛡️") },
                        modifier = Modifier.testTag("filter_outside_contacts")
                    )

                    FilterChip(
                        selected = selectedTab == RecordingFilterTab.STARRED,
                        onClick = { viewModel.selectedFilterTab.value = RecordingFilterTab.STARRED },
                        label = { Text("Favoris ⭐") },
                        modifier = Modifier.testTag("filter_starred")
                    )
                }
            }
        }

        // Recordings List
        if (recordings.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = if (searchQuery.isNotBlank()) "Aucun résultat pour '$searchQuery'" else "Aucun appel enregistré pour l'instant",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Les appels des numéros inconnus hors de votre répertoire s'enregistreront automatiquement ici.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(
                items = recordings,
                key = { it.id }
            ) { recording ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    RecordingCard(
                        recording = recording,
                        playbackState = playbackState,
                        onTogglePlayPause = {
                            viewModel.audioPlayerManager.togglePlayPause(recording.id, recording.filePath)
                        },
                        onSeekTo = { pos -> viewModel.audioPlayerManager.seekTo(pos) },
                        onCycleSpeed = { viewModel.audioPlayerManager.cyclePlaybackSpeed() },
                        onToggleStar = { viewModel.toggleStar(recording) },
                        onDelete = { viewModel.deleteRecording(recording) },
                        onUpdateNotes = { notes -> viewModel.updateNotes(recording.id, notes) }
                    )
                }
            }
        }
    }
}
