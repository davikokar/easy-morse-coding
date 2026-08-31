package com.example.easymorsecoding.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.easymorsecoding.LocaleHelper
import com.example.easymorsecoding.R
import com.example.easymorsecoding.viewmodel.MorseViewModel
import com.example.easymorsecoding.viewmodel.PlaybackState
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MorseMessengerApp(
    viewModel: MorseViewModel,
    onRequestPermission: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onShareApp: () -> Unit,
    onRateApp: () -> Unit,
    onCustomerSupport: () -> Unit,
    onBuyCoffee: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var menuExpanded by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Local state for Morse input to handle cursor position correctly
    var morseTextFieldValue by remember { mutableStateOf(TextFieldValue(uiState.morseDisplay)) }

    // Sync from ViewModel to local state (e.g. when Message area changes)
    LaunchedEffect(uiState.morseDisplay) {
        if (morseTextFieldValue.text != uiState.morseDisplay) {
            morseTextFieldValue = TextFieldValue(
                text = uiState.morseDisplay,
                selection = TextRange(uiState.morseDisplay.length)
            )
        }
    }

    val morseAnnotatedString = buildAnnotatedString {
        append(uiState.morseDisplay)
        val currentIndex = uiState.currentSignalIndex
        if (currentIndex != null && currentIndex in uiState.signalRanges.indices) {
            val range = uiState.signalRanges[currentIndex]
            if (range != null && range.first < uiState.morseDisplay.length) {
                addStyle(
                    style = SpanStyle(
                        color = Color.White,
                        background = Color.Black
                    ),
                    start = range.first,
                    end = (range.last + 1).coerceAtMost(uiState.morseDisplay.length)
                )
            }
        }
    }

    if (showSettings) {
        SettingsScreen(
            uiState = uiState,
            viewModel = viewModel,
            onBack = { showSettings = false },
            onLanguageSelected = onLanguageSelected,
            onShareApp = onShareApp,
            onRateApp = onRateApp,
            onCustomerSupport = onCustomerSupport,
            onBuyCoffee = onBuyCoffee
        )
        return
    }

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
                            Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.menu))
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.settings)) },
                                onClick = {
                                    menuExpanded = false
                                    showSettings = true
                                },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) }
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Countdown Display
            if (uiState.playbackState == PlaybackState.COUNTDOWN) {
                val countdownDescription = stringResource(R.string.countdown_description, uiState.currentCountdown ?: 0)
                Text(
                    text = uiState.currentCountdown?.toString() ?: "",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics { contentDescription = countdownDescription }
                )
            }

            // Text Input
            OutlinedTextField(
                value = uiState.message,
                onValueChange = { viewModel.onMessageChange(it) },
                label = { Text(stringResource(R.string.message_to_encode)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 3,
                enabled = uiState.playbackState == PlaybackState.IDLE,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            // Morse Input/Display
            OutlinedTextField(
                value = if (uiState.playbackState == PlaybackState.IDLE) {
                    morseTextFieldValue
                } else {
                    TextFieldValue(morseAnnotatedString)
                },
                onValueChange = { 
                    if (uiState.playbackState == PlaybackState.IDLE) {
                        morseTextFieldValue = it
                        viewModel.onMorseChange(it.text) 
                    }
                },
                label = { Text(stringResource(R.string.morse_code_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                minLines = 2,
                enabled = uiState.playbackState == PlaybackState.IDLE,
                isError = uiState.isMorseInvalid,
                supportingText = {
                    if (uiState.isMorseInvalid) {
                        Text(stringResource(R.string.invalid_morse_sequence), color = MaterialTheme.colorScheme.error)
                    } else if (uiState.message.isNotEmpty()) {
                        Text(stringResource(R.string.decodes_to, uiState.message), style = MaterialTheme.typography.bodySmall)
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
                        Text(stringResource(R.string.play))
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
                            Text(stringResource(if (uiState.isPaused) R.string.resume else R.string.pause))
                        }
                    }
                    
                    Button(
                        onClick = { viewModel.stopPlayback() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.stop))
                    }
                }
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: com.example.easymorsecoding.viewmodel.MorseUiState,
    viewModel: MorseViewModel,
    onBack: () -> Unit,
    onLanguageSelected: (String) -> Unit,
    onShareApp: () -> Unit,
    onRateApp: () -> Unit,
    onCustomerSupport: () -> Unit,
    onBuyCoffee: () -> Unit
) {
    var dialog by remember { mutableStateOf<SettingsDialogType?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current
    val versionName = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }

    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(stringResource(R.string.settings_general))
            SettingsRow(Icons.Default.Language, stringResource(R.string.settings_change_language)) {
                dialog = SettingsDialogType.LANGUAGE
            }
            SettingsRow(Icons.Default.Schedule, stringResource(R.string.settings_timings)) {
                dialog = SettingsDialogType.TIMINGS
            }
            HorizontalDivider()

            SettingsSectionHeader(stringResource(R.string.settings_app))
            SettingsRow(Icons.Default.Share, stringResource(R.string.settings_share_app), onShareApp)
            SettingsRow(Icons.Default.Star, stringResource(R.string.settings_rate_app), onRateApp)
            SettingsRow(Icons.Default.SupportAgent, stringResource(R.string.settings_customer_support), onCustomerSupport)
            SettingsRow(Icons.Default.Info, stringResource(R.string.settings_about)) {
                dialog = SettingsDialogType.ABOUT
            }
            HorizontalDivider()

            SettingsSectionHeader(stringResource(R.string.settings_community_support))
            SettingsRow(Icons.Default.Coffee, stringResource(R.string.settings_buy_coffee), onBuyCoffee)
            HorizontalDivider()

            SettingsSectionHeader(stringResource(R.string.settings_legal))
            SettingsRow(Icons.Default.PrivacyTip, stringResource(R.string.settings_privacy)) {
                dialog = SettingsDialogType.PRIVACY
            }
            SettingsRow(Icons.Default.Description, stringResource(R.string.settings_terms)) {
                dialog = SettingsDialogType.TERMS
            }
        }
    }

    when (dialog) {
        SettingsDialogType.LANGUAGE -> LanguageDialog(
            onLanguageSelected = onLanguageSelected,
            onDismiss = { dialog = null }
        )
        SettingsDialogType.TIMINGS -> SettingsDialog(
            uiState = uiState,
            onDotChange = viewModel::onDotUnitsChange,
            onDashChange = viewModel::onDashUnitsChange,
            onCharGapChange = viewModel::onCharGapUnitsChange,
            onWordGapChange = viewModel::onWordGapUnitsChange,
            onRepeatGapChange = viewModel::onRepeatGapUnitsChange,
            onSecondsPerUnitChange = viewModel::onSecondsPerUnitChange,
            onDismiss = { dialog = null }
        )
        SettingsDialogType.ABOUT -> InformationDialog(
            title = stringResource(R.string.settings_about),
            message = stringResource(R.string.about_message) + "\n\n" +
                stringResource(R.string.about_version, versionName),
            onDismiss = { dialog = null }
        )
        SettingsDialogType.PRIVACY -> InformationDialog(
            title = stringResource(R.string.settings_privacy),
            message = stringResource(R.string.privacy_policy_text),
            onDismiss = { dialog = null }
        )
        SettingsDialogType.TERMS -> InformationDialog(
            title = stringResource(R.string.settings_terms),
            message = stringResource(R.string.terms_text),
            onDismiss = { dialog = null }
        )
        null -> Unit
    }
}

private enum class SettingsDialogType { LANGUAGE, TIMINGS, ABOUT, PRIVACY, TERMS }

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(20.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LanguageDialog(onLanguageSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tags = remember { listOf("") + LocaleHelper.supportedLanguageTags }
    var selectedTag by remember { mutableStateOf(LocaleHelper.getPersistedLanguageTag(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_change_language)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                tags.forEach { tag ->
                    val label = if (tag.isEmpty()) {
                        stringResource(R.string.language_system_default)
                    } else {
                        val locale = Locale.forLanguageTag(tag)
                        locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTag = tag }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedTag == tag, onClick = { selectedTag = tag })
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onLanguageSelected(selectedTag) }) {
                Text(stringResource(R.string.action_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}

@Composable
private fun InformationDialog(title: String, message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
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
    }
}

@Composable
fun SettingsSection(
    uiState: com.example.easymorsecoding.viewmodel.MorseUiState,
    viewModel: MorseViewModel,
    onRequestPermission: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.outputs), fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = uiState.useFlashlight,
                onCheckedChange = { 
                    if (it) onRequestPermission() else viewModel.onToggleFlashlight(false)
                },
                enabled = uiState.playbackState == PlaybackState.IDLE && uiState.hasFlashlight
            )
            Text(stringResource(R.string.phone_flashlight))
            if (!uiState.hasFlashlight) {
                Text(
                    stringResource(R.string.not_available),
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
            Text(stringResource(R.string.sound))
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.countdown))
            val countdownOptions = listOf(0, 3, 5, 10, 30)
            var expanded by remember { mutableStateOf(false) }
            
            Box {
                TextButton(
                    onClick = { expanded = true },
                    enabled = uiState.playbackState == PlaybackState.IDLE
                ) {
                    Text(stringResource(R.string.seconds_short, uiState.countdownSeconds))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    countdownOptions.forEach { seconds ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.seconds_short, seconds)) },
                            onClick = {
                                viewModel.onCountdownSecondsChange(seconds)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))
            Checkbox(
                checked = uiState.repeatEnabled,
                onCheckedChange = { viewModel.onRepeatChange(it) },
                enabled = uiState.playbackState == PlaybackState.IDLE
            )
            Text(stringResource(R.string.repeat))
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
    onRepeatGapChange: (Int) -> Unit,
    onSecondsPerUnitChange: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.timing_settings)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.speed), fontWeight = FontWeight.Bold)
                TimingSliderFloat(
                    label = stringResource(R.string.unit_duration),
                    value = uiState.secondsPerUnit,
                    onValueChange = onSecondsPerUnitChange,
                    range = 0.1f..2.0f
                )
                
                HorizontalDivider()
                
                Text(stringResource(R.string.multipliers), fontWeight = FontWeight.Bold)
                TimingSlider(label = stringResource(R.string.dot_duration), units = uiState.dotUnits, onValueChange = onDotChange, range = 1f..5f)
                TimingSlider(label = stringResource(R.string.dash_duration), units = uiState.dashUnits, onValueChange = onDashChange, range = 1f..10f)
                TimingSlider(label = stringResource(R.string.character_gap), units = uiState.charGapUnits, onValueChange = onCharGapChange, range = 1f..10f)
                TimingSlider(label = stringResource(R.string.word_gap), units = uiState.wordGapUnits, onValueChange = onWordGapChange, range = 1f..20f)
                TimingSlider(label = stringResource(R.string.repeat_gap), units = uiState.repeatGapUnits, onValueChange = onRepeatGapChange, range = 1f..30f)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.done))
            }
        }
    )
}

@Composable
fun TimingSliderFloat(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.duration_seconds, value), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
            Text(stringResource(R.string.unit_count, units), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = units.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = if (range.endInclusive - range.start > 1) (range.endInclusive - range.start).toInt() - 1 else 0
        )
    }
}
