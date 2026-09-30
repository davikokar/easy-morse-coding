package com.example.easymorsecoding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.easymorsecoding.R
import com.example.easymorsecoding.ui.components.ChassisPanel
import com.example.easymorsecoding.ui.components.MechanicalButton
import com.example.easymorsecoding.ui.components.PanelLabel
import com.example.easymorsecoding.ui.components.StatusLamp
import com.example.easymorsecoding.ui.components.ToggleBracket
import com.example.easymorsecoding.ui.components.chassisBevel
import com.example.easymorsecoding.ui.components.recessedWell
import com.example.easymorsecoding.ui.theme.ChassisGlass
import com.example.easymorsecoding.ui.theme.ChassisRecess
import com.example.easymorsecoding.ui.theme.HazardRed
import com.example.easymorsecoding.viewmodel.MorseUiState
import com.example.easymorsecoding.viewmodel.MorseViewModel
import com.example.easymorsecoding.viewmodel.PlaybackState

internal val CountdownOptions = listOf(0, 3, 5, 10, 30)

/** Front-panel masthead; the lamp cluster doubles as the settings entry point. */
@Composable
internal fun StationHeader(uiState: MorseUiState, onOpenSettings: () -> Unit) {
    val transmitting = uiState.playbackState == PlaybackState.PLAYING && !uiState.isPaused
    val idle = uiState.playbackState == PlaybackState.IDLE
    val settingsLabel = stringResource(R.string.settings)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .statusBarsPadding()
            .chassisBevel()
            .padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SettingsInputAntenna,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.station_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Column(
            modifier = Modifier
                .clickable(onClick = onOpenSettings)
                .semantics {
                    role = Role.Button
                    contentDescription = settingsLabel
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            StatusLamp(MaterialTheme.colorScheme.tertiaryContainer, lit = true, size = 6.dp)
            StatusLamp(HazardRed, lit = transmitting, size = 6.dp)
            StatusLamp(MaterialTheme.colorScheme.primaryContainer, lit = idle, size = 6.dp)
        }
    }
}

/** Large amber segment readout shown while the pre-delay counts down. */
@Composable
internal fun CountdownBay(seconds: Int) {
    val description = stringResource(R.string.countdown_description, seconds)
    val amber = MaterialTheme.colorScheme.primaryContainer

    ChassisPanel(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        PanelLabel(text = stringResource(R.string.pre_delay_label), lampColor = amber)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .recessedWell()
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = seconds.toString().padStart(2, '0'),
                style = MaterialTheme.typography.displayLarge,
                fontSize = 56.sp,
                color = amber,
                modifier = Modifier.semantics { contentDescription = description }
            )
        }
    }
}

/** Message entry well plus the phosphor-green Morse readout. */
@Composable
internal fun TransmitBay(uiState: MorseUiState, viewModel: MorseViewModel) {
    val idle = uiState.playbackState == PlaybackState.IDLE
    val amber = MaterialTheme.colorScheme.primaryContainer
    val phosphor = MaterialTheme.colorScheme.tertiaryContainer

    // Local mirror of the Morse field so the caret survives the round trip through the ViewModel.
    var morseField by remember { mutableStateOf(TextFieldValue(uiState.morseDisplay)) }
    LaunchedEffect(uiState.morseDisplay) {
        if (morseField.text != uiState.morseDisplay) {
            morseField = TextFieldValue(
                text = uiState.morseDisplay,
                selection = TextRange(uiState.morseDisplay.length)
            )
        }
    }

    val messageLabel = stringResource(R.string.message_to_encode)
    val morseLabel = stringResource(R.string.morse_code_label)

    ChassisPanel(verticalSpacing = 8.dp) {
        PanelLabel(text = messageLabel, lampColor = amber)

        ReadoutWell(glowColor = amber) {
            BasicTextField(
                value = uiState.message,
                onValueChange = viewModel::onMessageChange,
                enabled = idle,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = amber),
                cursorBrush = SolidColor(amber),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
                    .semantics { contentDescription = messageLabel }
            )
        }

        PanelLabel(text = morseLabel, lampColor = phosphor)

        ReadoutWell(glowColor = phosphor) {
            if (idle) {
                BasicTextField(
                    value = morseField,
                    onValueChange = {
                        morseField = it
                        viewModel.onMorseChange(it.text)
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (uiState.isMorseInvalid) MaterialTheme.colorScheme.error else phosphor
                    ),
                    cursorBrush = SolidColor(phosphor),
                    keyboardOptions = KeyboardOptions(
                        // Email keyboards surface "." and "-" on the primary page.
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .semantics { contentDescription = morseLabel }
                )
            } else {
                Text(
                    text = highlightedMorse(uiState, phosphor),
                    style = MaterialTheme.typography.bodyLarge,
                    color = phosphor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .verticalScroll(rememberScrollState())
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (uiState.isMorseInvalid) {
                    stringResource(R.string.invalid_morse_sequence)
                } else {
                    ""
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            val statusRes = when {
                uiState.isMorseInvalid -> R.string.status_fault
                idle -> R.string.status_ready
                uiState.isPaused -> R.string.status_hold
                else -> R.string.status_transmitting
            }
            val statusColor =
                if (uiState.isMorseInvalid) MaterialTheme.colorScheme.error else phosphor
            StatusLamp(color = statusColor, lit = true, size = 8.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(statusRes),
                style = MaterialTheme.typography.labelMedium,
                color = statusColor
            )
        }
    }
}

private fun highlightedMorse(uiState: MorseUiState, phosphor: Color) = buildAnnotatedString {
    append(uiState.morseDisplay)
    val index = uiState.currentSignalIndex
    if (index != null && index in uiState.signalRanges.indices) {
        val range = uiState.signalRanges[index]
        if (range != null && range.first < uiState.morseDisplay.length) {
            addStyle(
                style = SpanStyle(color = Color.Black, background = phosphor),
                start = range.first,
                end = (range.last + 1).coerceAtMost(uiState.morseDisplay.length)
            )
        }
    }
}

/** Recessed acrylic-fronted display window. */
@Composable
private fun ReadoutWell(glowColor: Color, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .recessedWell(background = ChassisGlass)
            .padding(12.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides glowColor) { content() }
    }
}

/** Output routing and timing sub-panels. */
@Composable
internal fun OptionsBay(
    uiState: MorseUiState,
    viewModel: MorseViewModel,
    onRequestPermission: () -> Unit
) {
    val idle = uiState.playbackState == PlaybackState.IDLE
    val amber = MaterialTheme.colorScheme.primaryContainer
    val phosphor = MaterialTheme.colorScheme.tertiaryContainer

    ChassisPanel(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Text(
            text = stringResource(R.string.outputs),
            style = MaterialTheme.typography.labelMedium,
            color = amber
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            OutputModule(
                icon = Icons.Default.FlashOn,
                label = stringResource(R.string.phone_flashlight),
                checked = uiState.useFlashlight,
                enabled = idle && uiState.hasFlashlight,
                unavailable = !uiState.hasFlashlight,
                accent = amber,
                onCheckedChange = {
                    if (it) onRequestPermission() else viewModel.onToggleFlashlight(false)
                },
                modifier = Modifier.weight(1f)
            )
            OutputModule(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                label = stringResource(R.string.sound),
                checked = uiState.useSound,
                enabled = idle,
                unavailable = false,
                accent = phosphor,
                onCheckedChange = viewModel::onToggleSound,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            PreDelayModule(
                seconds = uiState.countdownSeconds,
                enabled = idle,
                onChange = viewModel::onCountdownSecondsChange,
                modifier = Modifier.weight(1f)
            )
            RepetitionModule(
                repeating = uiState.repeatEnabled,
                enabled = idle,
                onChange = viewModel::onRepeatChange,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SubModule(
    title: String,
    accent: Color,
    lit: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .chassisBevel()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                modifier = Modifier.weight(1f)
            )
            StatusLamp(color = accent, lit = lit, size = 9.dp)
        }
        content()
    }
}

@Composable
private fun OutputModule(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    enabled: Boolean,
    unavailable: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .chassisBevel()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (enabled) accent else MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                modifier = Modifier.weight(1f)
            )
            StatusLamp(color = accent, lit = checked && !unavailable, size = 11.dp)
        }
        MechanicalButton(
            onClick = { onCheckedChange(!checked) },
            enabled = enabled,
            minHeight = 32.dp,
            horizontalPadding = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (checked) {
                StatusLamp(color = accent, lit = true, size = 7.dp)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = stringResource(
                    if (checked) R.string.state_engaged else R.string.state_disarmed
                ),
                style = MaterialTheme.typography.labelMedium
            )
        }
        if (unavailable) {
            Text(
                text = stringResource(R.string.no_flashlight_available),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun PreDelayModule(
    seconds: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val amber = MaterialTheme.colorScheme.primaryContainer
    val index = CountdownOptions.indexOf(seconds).coerceAtLeast(0)

    SubModule(
        title = stringResource(R.string.pre_delay_label),
        accent = amber,
        lit = enabled && seconds > 0,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChassisRecess)
                .chassisBevel(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepperKey(
                symbol = "\u2212",
                enabled = enabled && index > 0,
                onClick = { onChange(CountdownOptions[index - 1]) }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .recessedWell()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = seconds.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.headlineMedium,
                    color = amber
                )
                Text(
                    text = stringResource(R.string.unit_seconds),
                    style = MaterialTheme.typography.labelMedium,
                    color = amber
                )
            }
            StepperKey(
                symbol = "+",
                enabled = enabled && index < CountdownOptions.lastIndex,
                onClick = { onChange(CountdownOptions[index + 1]) }
            )
        }
    }
}

@Composable
private fun StepperKey(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    MechanicalButton(
        onClick = onClick,
        enabled = enabled,
        minHeight = 46.dp,
        horizontalPadding = 10.dp,
        modifier = Modifier.width(42.dp)
    ) {
        Text(text = symbol, style = MaterialTheme.typography.headlineSmall)
    }
}

@Composable
private fun RepetitionModule(
    repeating: Boolean,
    enabled: Boolean,
    onChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val cyan = MaterialTheme.colorScheme.secondaryContainer

    SubModule(
        title = stringResource(R.string.repetition_label),
        accent = cyan,
        lit = enabled && repeating,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .chassisBevel()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.repeat),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            ToggleBracket(
                checked = repeating,
                onCheckedChange = onChange,
                enabled = enabled,
                activeColor = cyan
            )
        }
    }
}

/** Transport deck carrying the primary keying control. */
@Composable
internal fun TransportBay(uiState: MorseUiState, viewModel: MorseViewModel) {
    ChassisPanel(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentPadding = 14.dp,
        showRivets = false
    ) {
        if (uiState.playbackState == PlaybackState.IDLE) {
            MechanicalButton(
                onClick = viewModel::startPlayback,
                enabled = uiState.message.isNotBlank(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                glow = true,
                minHeight = 62.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.size(26.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.play),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (uiState.playbackState == PlaybackState.PLAYING) {
                    MechanicalButton(
                        onClick = viewModel::togglePause,
                        containerColor = if (uiState.isPaused) {
                            MaterialTheme.colorScheme.tertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.secondaryContainer
                        },
                        contentColor = if (uiState.isPaused) {
                            MaterialTheme.colorScheme.onTertiaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSecondaryContainer
                        },
                        glow = true,
                        minHeight = 54.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (uiState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(
                                if (uiState.isPaused) R.string.resume else R.string.pause
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                MechanicalButton(
                    onClick = viewModel::stopPlayback,
                    containerColor = HazardRed,
                    contentColor = Color.Black,
                    glow = true,
                    minHeight = 54.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Stop, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stop),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

