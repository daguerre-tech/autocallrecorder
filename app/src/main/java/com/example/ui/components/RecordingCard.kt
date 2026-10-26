package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.model.CallRecording
import com.example.data.model.CallType
import com.example.recorder.ContactsHelper
import com.example.recorder.PlaybackState
import com.example.ui.theme.AmberAlert
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.RecordRed
import com.example.ui.theme.RecordRedContainer
import com.example.ui.theme.Slate700
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordingCard(
    recording: CallRecording,
    playbackState: PlaybackState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onCycleSpeed: () -> Unit,
    onToggleStar: () -> Unit,
    onDelete: () -> Unit,
    onUpdateNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isCurrentTrack = playbackState.currentRecordingId == recording.id
    val isPlaying = isCurrentTrack && playbackState.isPlaying

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNoteDialog by remember { mutableStateOf(false) }
    var noteInput by remember { mutableStateOf(recording.notes) }

    val formattedDate = remember(recording.timestamp) {
        SimpleDateFormat("dd/MM/yyyy • HH:mm", Locale.getDefault()).format(Date(recording.timestamp))
    }

    val formattedDuration = remember(recording.durationSeconds) {
        val mins = recording.durationSeconds / 60
        val secs = recording.durationSeconds % 60
        String.format("%02d:%02d", mins, secs)
    }

    val formattedSize = remember(recording.fileSizeBytes) {
        when {
            recording.fileSizeBytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f Mo", recording.fileSizeBytes / (1024f * 1024f))
            recording.fileSizeBytes >= 1024 -> String.format(Locale.US, "%d Ko", recording.fileSizeBytes / 1024)
            else -> "${recording.fileSizeBytes} octets"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("recording_card_${recording.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentTrack) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentTrack) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Call Icon, Phone Number, Outside Contact Badge & Star
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direction icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (recording.isOutsideContacts) RecordRedContainer else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (recording.callType == CallType.INCOMING) Icons.Default.CallReceived else Icons.Default.CallMade,
                        contentDescription = "Direction de l'appel",
                        tint = if (recording.isOutsideContacts) RecordRed else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ContactsHelper.formatDisplayNumber(recording.phoneNumber),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (recording.isOutsideContacts) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = RecordRed.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = RecordRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "HORS RÉPERTOIRE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RecordRed,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = recording.contactName ?: "CONTACT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "• $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite Star
                IconButton(
                    onClick = onToggleStar,
                    modifier = Modifier.testTag("favorite_button_${recording.id}")
                ) {
                    Icon(
                        imageVector = if (recording.isStarred) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favori",
                        tint = if (recording.isStarred) AmberAlert else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Note if present
            if (recording.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📝 ${recording.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Player Bar: Play button, Progress/Duration, Speed badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("play_button_${recording.id}"),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isPlaying) RecordRed else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Lire",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    if (isCurrentTrack) {
                        val duration = playbackState.durationMs.toFloat().coerceAtLeast(1000f)
                        val currentPos = playbackState.currentPositionMs.toFloat().coerceIn(0f, duration)

                        Slider(
                            value = currentPos,
                            onValueChange = { onSeekTo(it.toInt()) },
                            valueRange = 0f..duration,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val posMins = (playbackState.currentPositionMs / 1000) / 60
                            val posSecs = (playbackState.currentPositionMs / 1000) % 60
                            Text(
                                text = String.format("%02d:%02d", posMins, posSecs),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$formattedDuration • $formattedSize",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Static timeline view
                        WaveformVisualizer(
                            amplitude = 0.25f,
                            isRecording = false,
                            modifier = Modifier.height(24.dp),
                            activeColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Durée : $formattedDuration",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedSize,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (isCurrentTrack) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clickable { onCycleSpeed() }
                            .padding(2.dp)
                    ) {
                        Text(
                            text = "${playbackState.playbackSpeed}x",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer Actions: Note, Share audio, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add / Edit note
                IconButton(
                    onClick = { showNoteDialog = true },
                    modifier = Modifier.testTag("note_button_${recording.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.EditNote,
                        contentDescription = "Ajouter une note",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Share audio file
                IconButton(
                    onClick = { shareRecordingFile(context, recording.filePath, recording.phoneNumber) },
                    modifier = Modifier.testTag("share_button_${recording.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Partager",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Delete recording
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.testTag("delete_button_${recording.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer l'enregistrement ?") },
            text = { Text("Le fichier audio sera définitivement supprimé de la mémoire de votre téléphone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Supprimer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    // Note / Memo Dialog
    if (showNoteDialog) {
        AlertDialog(
            onDismissRequest = { showNoteDialog = false },
            title = { Text("Note pour cet appel") },
            text = {
                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Ajouter des détails (ex: Devis, rendez-vous)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showNoteDialog = false
                        onUpdateNotes(noteInput)
                    }
                ) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

private fun shareRecordingFile(context: Context, filePath: String, phoneNumber: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Enregistrement d'appel - $phoneNumber")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Partager l'enregistrement audio"))
    } catch (_: Exception) {}
}
