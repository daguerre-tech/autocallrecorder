package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.CallRecorderViewModel
import com.example.ui.screens.CallSimulatorScreen
import com.example.ui.screens.NumberRulesScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    RECORDINGS("Enregistrements", Icons.Filled.PhoneCallback, Icons.Outlined.PhoneCallback, "tab_recordings"),
    RULES("Filtres", Icons.Filled.Shield, Icons.Outlined.Shield, "tab_rules"),
    SIMULATOR("Simulateur", Icons.Filled.Call, Icons.Outlined.Call, "tab_simulator"),
    SETTINGS("Paramètres", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: CallRecorderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Request permissions seamlessly on startup
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { /* permissions handled */ }

                LaunchedEffect(Unit) {
                    val perms = mutableListOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CONTACTS
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionsLauncher.launch(perms.toTypedArray())
                }

                CallRecorderApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun CallRecorderApp(viewModel: CallRecorderViewModel) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(AppTab.RECORDINGS.ordinal) }
    val currentTab = AppTab.entries[selectedTabIndex]

    // BackHandler: Return to Recordings tab if on another screen
    BackHandler(enabled = selectedTabIndex != AppTab.RECORDINGS.ordinal) {
        selectedTabIndex = AppTab.RECORDINGS.ordinal
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Adaptive Tablet / Foldable layout with NavigationRail
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.testTag("app_navigation_rail"),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    AppTab.entries.forEach { tab ->
                        val isSelected = tab.ordinal == selectedTabIndex
                        NavigationRailItem(
                            selected = isSelected,
                            onClick = { selectedTabIndex = tab.ordinal },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
                            },
                            label = { Text(tab.title) },
                            modifier = Modifier.testTag(tab.testTag)
                        )
                    }
                }

                Scaffold(modifier = Modifier.weight(1f)) { innerPadding ->
                    ScreenContent(
                        tab = currentTab,
                        viewModel = viewModel,
                        onNavigateToRecordings = { selectedTabIndex = AppTab.RECORDINGS.ordinal },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        } else {
            // Standard Handheld layout with NavigationBar
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("app_navigation_bar"),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppTab.entries.forEach { tab ->
                            val isSelected = tab.ordinal == selectedTabIndex
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTabIndex = tab.ordinal },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                },
                                label = { Text(tab.title) },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            ) { innerPadding ->
                ScreenContent(
                    tab = currentTab,
                    viewModel = viewModel,
                    onNavigateToRecordings = { selectedTabIndex = AppTab.RECORDINGS.ordinal },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@Composable
fun ScreenContent(
    tab: AppTab,
    viewModel: CallRecorderViewModel,
    onNavigateToRecordings: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (tab) {
        AppTab.RECORDINGS -> RecordingsScreen(viewModel = viewModel, modifier = modifier)
        AppTab.RULES -> NumberRulesScreen(viewModel = viewModel, modifier = modifier)
        AppTab.SIMULATOR -> CallSimulatorScreen(
            viewModel = viewModel,
            onNavigateToRecordings = onNavigateToRecordings,
            modifier = modifier
        )
        AppTab.SETTINGS -> SettingsScreen(viewModel = viewModel, modifier = modifier)
    }
}
