package com.davide.seddio.easymorsecoding.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.davide.seddio.easymorsecoding.LocaleHelper
import com.davide.seddio.easymorsecoding.R
import com.davide.seddio.easymorsecoding.viewmodel.MorseUiState
import com.davide.seddio.easymorsecoding.viewmodel.MorseViewModel
import com.davide.seddio.easymorsecoding.viewmodel.PlaybackState
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
    
    var showSettings by remember { mutableStateOf(false) }

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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { StationHeader(uiState = uiState, onOpenSettings = { showSettings = true }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.playbackState == PlaybackState.COUNTDOWN) {
                CountdownBay(uiState.currentCountdown ?: 0)
            }

            TransmitBay(
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )

            OptionsBay(
                uiState = uiState,
                viewModel = viewModel,
                onRequestPermission = onRequestPermission
            )

            TransportBay(uiState = uiState, viewModel = viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MorseUiState,
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
    val uriHandler = LocalUriHandler.current
    val privacyUrl = stringResource(R.string.url_privacy)
    val termsUrl = stringResource(R.string.url_terms)
    val versionName = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }

    BackHandler(onBack = onBack)
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSectionHeader(stringResource(R.string.settings_app))
            SettingsRow(Icons.Default.Share, stringResource(R.string.settings_share_app), onShareApp)
            SettingsRow(Icons.Default.Star, stringResource(R.string.settings_rate_app), onRateApp)
            SettingsRow(Icons.Default.SupportAgent, stringResource(R.string.settings_customer_support), onCustomerSupport)
            SettingsRow(Icons.Default.Info, stringResource(R.string.settings_about)) {
                dialog = SettingsDialogType.ABOUT
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSectionHeader(stringResource(R.string.settings_community_support))
            SettingsRow(Icons.Default.Coffee, stringResource(R.string.settings_buy_coffee), onBuyCoffee)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            SettingsSectionHeader(stringResource(R.string.settings_legal))
            SettingsRow(Icons.Default.PrivacyTip, stringResource(R.string.settings_privacy)) {
                runCatching { uriHandler.openUri(privacyUrl) }
            }
            SettingsRow(Icons.Default.Description, stringResource(R.string.settings_terms)) {
                runCatching { uriHandler.openUri(termsUrl) }
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
        null -> Unit
    }
}

private enum class SettingsDialogType { LANGUAGE, TIMINGS, ABOUT }

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primaryContainer
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
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
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun LanguageDialog(onLanguageSelected: (String) -> Unit, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val tags = remember { listOf("") + LocaleHelper.supportedLanguageTags }
    var selectedTag by remember { mutableStateOf(LocaleHelper.getPersistedLanguageTag(context)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
        }
    )
}

@Composable
fun SettingsDialog(
    uiState: MorseUiState,
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
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        title = { Text(stringResource(R.string.timing_settings)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.speed),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
                TimingSliderFloat(
                    label = stringResource(R.string.unit_duration),
                    value = uiState.secondsPerUnit,
                    onValueChange = onSecondsPerUnitChange,
                    range = 0.1f..2.0f
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Text(
                    text = stringResource(R.string.multipliers),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primaryContainer
                )
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
            Text(
                text = stringResource(R.string.duration_seconds, value),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primaryContainer
            )
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
            Text(
                text = stringResource(R.string.unit_count, units),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primaryContainer
            )
        }
        Slider(
            value = units.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range,
            steps = if (range.endInclusive - range.start > 1) (range.endInclusive - range.start).toInt() - 1 else 0
        )
    }
}
