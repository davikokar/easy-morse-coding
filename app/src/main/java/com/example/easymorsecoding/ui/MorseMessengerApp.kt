package com.example.easymorsecoding.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymorsecoding.viewmodel.MorseViewModel
import com.example.easymorsecoding.viewmodel.PlaybackState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorseMessengerApp(
    viewModel: MorseViewModel,
    onRequestPermission: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    var menuExpanded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Morse Messenger") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    menuExpanded = false
                                    showSettings = true
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("About") },
                                onClick = {
                                    menuExpanded = false
                                    showAbout = true
                                },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Countdown Display
            if (uiState.playbackState == PlaybackState.COUNTDOWN) {
                Text(
                    text = uiState.currentCountdown?.toString() ?: "",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { contentDescription = "Countdown: ${uiState.currentCountdown}" }
                )
            }

            // Text Input
            OutlinedTextField(
                value = uiState.message,
                onValueChange = { viewModel.onMessageChange(it) },
                label = { Text("Message to Encode") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                enabled = uiState.playbackState == PlaybackState.IDLE,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            // Morse Input/Display
            OutlinedTextField(
                value = uiState.morseDisplay,
                onValueChange = { viewModel.onMorseChange(it) },
                label = { Text("Morse Code (. - /)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
                enabled = uiState.playbackState == PlaybackState.IDLE,
                isError = uiState.isMorseInvalid,
                supportingText = {
                    if (uiState.isMorseInvalid) {
                        Text("Invalid Morse sequence", color = MaterialTheme.colorScheme.error)
                    } else if (uiState.message.isNotEmpty()) {
                        Text("Decodes to: ${uiState.message}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email, // Email keyboard often has . and - prominent
                    imeAction = ImeAction.Done
                )
            )

            // Playback Progress
            if (uiState.playbackState == PlaybackState.PLAYING) {
                PlaybackProgress(uiState = uiState)
            }

            // Controls
            SettingsSection(
                uiState = uiState,
                viewModel = viewModel,
                onRequestPermission = onRequestPermission
            )

            // Play/Pause/Stop Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.playbackState == PlaybackState.IDLE) {
                    Button(
                        onClick = { viewModel.startPlayback() },
                        modifier = Modifier.weight(1f),
                        enabled = uiState.message.isNotBlank()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Play")
                    }
                } else {
                    if (uiState.playbackState == PlaybackState.PLAYING) {
                        Button(
                            onClick = { viewModel.togglePause() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isPaused) MaterialTheme.colorScheme.secondary 
                                                else MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(
                                if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (uiState.isPaused) "Resume" else "Pause")
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.stopPlayback() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop")
                    }
                }
            }
        }
    }

    if (showSettings) {
        SettingsDialog(
            uiState = uiState,
            onDotChange = { viewModel.onDotUnitsChange(it) },
            onDashChange = { viewModel.onDashUnitsChange(it) },
            onCharGapChange = { viewModel.onCharGapUnitsChange(it) },
            onWordGapChange = { viewModel.onWordGapUnitsChange(it) },
            onSecondsPerUnitChange = { viewModel.onSecondsPerUnitChange(it) },
            onDismiss = { showSettings = false }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("About Morse Messenger") },
            text = { Text("A simple app to encode and play Morse code signals using light and sound.\n\nVersion 1.0") },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun PlaybackProgress(uiState: com.example.easymorsecoding.viewmodel.MorseUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = {
                val total = uiState.signals.size
                val current = uiState.currentSignalIndex ?: 0
                if (total > 0) current.toFloat() / total else 0f
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val currentIndex = uiState.currentSignalIndex ?: -1
            uiState.signals.forEachIndexed { index, signal ->
                // Only show a window of signals to avoid overcrowding
                if (index in (currentIndex - 5)..(currentIndex + 5)) {
                    val color = if (index == currentIndex) {
                        MaterialTheme.colorScheme.primary
                    } else if (index < currentIndex) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    val text = when (signal.isActive) {
                        true -> if (signal == com.example.easymorsecoding.model.MorseSignal.DOT) "●" else "▬"
                        false -> " "
                    }
                    
                    Text(
                        text = text,
                        color = color,
                        fontSize = if (index == currentIndex) 24.sp else 18.sp,
                        fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    uiState: com.example.easymorsecoding.viewmodel.MorseUiState,
    viewModel: MorseViewModel,
    onRequestPermission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Outputs", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.useFlashlight,
                onCheckedChange = { 
                    if (it) onRequestPermission() else viewModel.onToggleFlashlight(false)
                },
                enabled = uiState.playbackState == PlaybackState.IDLE && uiState.hasFlashlight
            )
            Text("Phone Flashlight")
            if (!uiState.hasFlashlight) {
                Text(
                    " (Not Available)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.useSound,
                onCheckedChange = { viewModel.onToggleSound(it) },
                enabled = uiState.playbackState == PlaybackState.IDLE
            )
            Text("Sound")
        }

        HorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Countdown: ")
            val countdownOptions = listOf(0, 3, 5, 10, 30)
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                TextButton(
                    onClick = { expanded = true },
                    enabled = uiState.playbackState == PlaybackState.IDLE
                ) {
                    Text("${uiState.countdownSeconds}s")
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    countdownOptions.forEach { seconds ->
                        DropdownMenuItem(
                            text = { Text("${seconds}s") },
                            onClick = {
                                viewModel.onCountdownSecondsChange(seconds)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    uiState: com.example.easymorsecoding.viewmodel.MorseUiState,
    onDotChange: (Int) -> Unit,
    onDashChange: (Int) -> Unit,
    onCharGapChange: (Int) -> Unit,
    onWordGapChange: (Int) -> Unit,
    onSecondsPerUnitChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Timing Settings") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Speed", fontWeight = FontWeight.Bold)
                TimingSliderFloat(
                    label = "Unit Duration",
                    value = uiState.secondsPerUnit,
                    onValueChange = onSecondsPerUnitChange,
                    range = 0.1f..2.0f,
                    unitLabel = "s"
                )
                
                HorizontalDivider()
                
                Text("Multipliers", fontWeight = FontWeight.Bold)
                TimingSlider(label = "Dot Duration", units = uiState.dotUnits, onValueChange = onDotChange, range = 1f..5f)
                TimingSlider(label = "Dash Duration", units = uiState.dashUnits, onValueChange = onDashChange, range = 1f..10f)
                TimingSlider(label = "Character Gap", units = uiState.charGapUnits, onValueChange = onCharGapChange, range = 1f..10f)
                TimingSlider(label = "Word Gap", units = uiState.wordGapUnits, onValueChange = onWordGapChange, range = 1f..20f)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun TimingSliderFloat(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    unitLabel: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("%.2f %s".format(java.util.Locale.US, value, unitLabel), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range
        )
    }
}


@Composable
fun TimingSlider(
    label: String,
    units: Int,
    onValueChange: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("$units units", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = units.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = if (range.endInclusive - range.start > 1) (range.endInclusive - range.start).toInt() - 1 else 0
        )
    }
}
