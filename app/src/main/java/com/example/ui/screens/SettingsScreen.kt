package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.CallRecorderViewModel
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RecordRed
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: CallRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsState()
    val audioFormat by viewModel.audioFormat.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val highQualityAudio by viewModel.highQualityAudio.collectAsState()

    var showClearDialog by remember { mutableStateOf(false) }

    // Check permissions
    var hasMicPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    var hasPhonePermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)
    }
    var hasContactsPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasMicPermission = results[Manifest.permission.RECORD_AUDIO] ?: hasMicPermission
        hasPhonePermission = results[Manifest.permission.READ_PHONE_STATE] ?: hasPhonePermission
        hasContactsPermission = results[Manifest.permission.READ_CONTACTS] ?: hasContactsPermission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasNotifPermission = results[Manifest.permission.POST_NOTIFICATIONS] ?: hasNotifPermission
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refreshStorageInfo()
    }

    val formattedStorage = remember(totalStorageBytes) {
        when {
            totalStorageBytes >= 1024 * 1024 -> String.format(Locale.US, "%.2f Mo", totalStorageBytes / (1024f * 1024f))
            totalStorageBytes >= 1024 -> String.format(Locale.US, "%d Ko", totalStorageBytes / 1024)
            else -> "$totalStorageBytes octets"
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "Paramètres & Stockage",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gestion de l'espace mémoire du téléphone, formats et autorisations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Storage Management Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("storage_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stockage interne du téléphone",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Emplacement : ${context.filesDir.absolutePath}/recordings/",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Espace utilisé",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formattedStorage,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Button(
                                onClick = { showClearDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("clear_storage_button")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Vider tout")
                            }
                        }
                    }
                }
            }
        }

        // Audio format & Quality
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("audio_config_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Format & Qualité Audio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Format d'encodage du fichier audio :",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = audioFormat == "m4a",
                            onClick = { viewModel.setAudioFormat("m4a") },
                            label = { Text("M4A (Recommandé)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = audioFormat == "aac",
                            onClick = { viewModel.setAudioFormat("aac") },
                            label = { Text("AAC") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Qualité audio supérieure (128 kbps)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Voix plus claire et nette pour les deux interlocuteurs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = highQualityAudio,
                            onCheckedChange = { viewModel.setHighQualityAudio(it) }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Alerte après un appel inconnu",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Affiche une notification dès qu'un numéro hors répertoire a été enregistré.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                        )
                    }
                }
            }
        }

        // Permissions Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("permissions_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Autorisations requises",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val perms = mutableListOf(
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.READ_CONTACTS
                                )
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    perms.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                requestPermissionLauncher.launch(perms.toTypedArray())
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("request_permissions_button")
                        ) {
                            Text("Activer")
                        }
                    }

                    PermissionRow(
                        label = "Microphone (Capture de l'appel)",
                        isGranted = hasMicPermission,
                        icon = Icons.Default.Audiotrack
                    )

                    PermissionRow(
                        label = "État du téléphone (Détection d'appel)",
                        isGranted = hasPhonePermission,
                        icon = Icons.Default.Phone
                    )

                    PermissionRow(
                        label = "Contacts (Filtrage des numéros inconnus)",
                        isGranted = hasContactsPermission,
                        icon = Icons.Default.ContactPhone
                    )

                    PermissionRow(
                        label = "Notifications (Contrôle d'enregistrement)",
                        isGranted = hasNotifPermission,
                        icon = Icons.Default.Notifications
                    )
                }
            }
        }

        // Privacy Guarantee Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Confidentialité 100% Locale",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tous les appels et enregistrements sont stockés exclusivement dans la mémoire privée de votre appareil. Aucune donnée n'est envoyée vers des serveurs distants.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Clear dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Supprimer tous les enregistrements ?") },
            text = { Text("Cette action effacera tous les fichiers audio sauvegardés sur votre téléphone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.deleteAllRecordings()
                    }
                ) {
                    Text("Tout effacer", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
private fun PermissionRow(
    label: String,
    isGranted: Boolean,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (isGranted) EmeraldSuccess.copy(alpha = 0.15f) else RecordRed.copy(alpha = 0.15f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isGranted) Icons.Default.Check else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isGranted) EmeraldSuccess else RecordRed,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isGranted) "Accordée" else "Requise",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isGranted) EmeraldSuccess else RecordRed,
                    fontSize = 11.sp
                )
            }
        }
    }
}
