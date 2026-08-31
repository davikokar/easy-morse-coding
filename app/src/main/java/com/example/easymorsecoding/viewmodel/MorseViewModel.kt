package com.example.easymorsecoding.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.easymorsecoding.encoder.MorseEncoder
import com.example.easymorsecoding.playback.FlashlightController
import com.example.easymorsecoding.playback.PlaybackController
import com.example.easymorsecoding.playback.SoundController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MorseViewModel(
    application: Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MorseUiState(
        message = savedStateHandle["message"] ?: "",
        useFlashlight = savedStateHandle["useFlashlight"] ?: false,
        useSound = savedStateHandle["useSound"] ?: false,
        secondsPerUnit = savedStateHandle["secondsPerUnit"] ?: 0.5f,
        dotUnits = savedStateHandle["dotUnits"] ?: 1,
        dashUnits = savedStateHandle["dashUnits"] ?: 3,
        charGapUnits = savedStateHandle["charGapUnits"] ?: 3,
        wordGapUnits = savedStateHandle["wordGapUnits"] ?: 7,
        countdownSeconds = savedStateHandle["countdownSeconds"] ?: 0,
        repeatEnabled = savedStateHandle["repeatEnabled"] ?: false,
        repeatGapUnits = savedStateHandle["repeatGapUnits"] ?: 14
    ))
    val uiState: StateFlow<MorseUiState> = _uiState.asStateFlow()

    private val isPausedFlow = uiState.map { it.isPaused }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val flashlightController = FlashlightController(application)
    private val soundController = SoundController()
    private val playbackController = PlaybackController(
        flashlightController = flashlightController,
        soundController = soundController,
        isPaused = isPausedFlow,
        onProgress = { index -> _uiState.update { it.copy(currentSignalIndex = index) } }
    )

    private var playbackJob: Job? = null

    init {
        val hasFlash = flashlightController.hasFlashlight()
        _uiState.update {
            it.copy(
                hasFlashlight = hasFlash,
                flashlightExplanation = if (!hasFlash) "No flashlight available on this device" else null,
                morseDisplay = MorseEncoder.encodeToMorseString(it.message)
            )
        }
    }

    private fun save(key: String, value: Any?) {
        savedStateHandle[key] = value
    }

    fun onMessageChange(newMessage: String) {
        _uiState.update { 
            it.copy(
                message = newMessage,
                morseDisplay = MorseEncoder.encodeToMorseString(newMessage),
                isMorseInvalid = false
            )
        }
        save("message", newMessage)
    }

    fun onMorseChange(newMorse: String) {
        // Filter input to allow only valid Morse characters
        val filteredMorse = newMorse.filter { it == '.' || it == '-' || it == ' ' || it == '/' }
        
        val decodedText = MorseEncoder.decodeFromMorse(filteredMorse)
        
        _uiState.update { 
            it.copy(
                morseDisplay = filteredMorse,
                message = decodedText ?: it.message, // Keep old message if invalid, or update to new
                isMorseInvalid = decodedText == null
            )
        }
        
        if (decodedText != null) {
            save("message", decodedText)
        }
    }

    fun onToggleFlashlight(enabled: Boolean) {
        _uiState.update { it.copy(useFlashlight = enabled) }
        save("useFlashlight", enabled)
    }

    fun onToggleSound(enabled: Boolean) {
        _uiState.update { it.copy(useSound = enabled) }
        save("useSound", enabled)
    }

    fun onSecondsPerUnitChange(seconds: Float) {
        _uiState.update { it.copy(secondsPerUnit = seconds) }
        save("secondsPerUnit", seconds)
    }

    fun onDotUnitsChange(units: Int) {
        _uiState.update { it.copy(dotUnits = units) }
        save("dotUnits", units)
    }

    fun onDashUnitsChange(units: Int) {
        _uiState.update { it.copy(dashUnits = units) }
        save("dashUnits", units)
    }

    fun onCharGapUnitsChange(units: Int) {
        _uiState.update { it.copy(charGapUnits = units) }
        save("charGapUnits", units)
    }

    fun onWordGapUnitsChange(units: Int) {
        _uiState.update { it.copy(wordGapUnits = units) }
        save("wordGapUnits", units)
    }

    fun onCountdownSecondsChange(seconds: Int) {
        _uiState.update { it.copy(countdownSeconds = seconds) }
        save("countdownSeconds", seconds)
    }

    fun onRepeatChange(enabled: Boolean) {
        _uiState.update { it.copy(repeatEnabled = enabled) }
        save("repeatEnabled", enabled)
    }

    fun onRepeatGapUnitsChange(units: Int) {
        _uiState.update { it.copy(repeatGapUnits = units) }
        save("repeatGapUnits", units)
    }

    fun togglePause() {
        if (uiState.value.playbackState == PlaybackState.PLAYING) {
            _uiState.update { it.copy(isPaused = !it.isPaused) }
        }
    }

    fun startPlayback() {
        if (_uiState.value.message.isBlank()) return
        if (playbackJob?.isActive == true) return

        playbackJob = viewModelScope.launch {
            val (morse, signalsWithRanges) = MorseEncoder.encodeToSignalsWithRanges(_uiState.value.message)
            val signals = signalsWithRanges.map { it.signal }
            val ranges = signalsWithRanges.map { it.range }
            
            _uiState.update { 
                it.copy(
                    signals = signals,
                    signalRanges = ranges,
                    morseDisplay = morse, // Ensure display matches the signals exactly
                    playbackState = PlaybackState.COUNTDOWN,
                    isPaused = false 
                ) 
            }

            val countdown = _uiState.value.countdownSeconds
            if (countdown > 0) {
                for (i in countdown downTo 1) {
                    _uiState.update { it.copy(currentCountdown = i) }
                    delay(1000)
                }
            }
            _uiState.update { it.copy(currentCountdown = null, playbackState = PlaybackState.PLAYING) }

            playbackController.play(
                signals = signals,
                secondsPerUnit = _uiState.value.secondsPerUnit,
                useFlashlight = _uiState.value.useFlashlight,
                useSound = _uiState.value.useSound,
                dotUnits = _uiState.value.dotUnits,
                dashUnits = _uiState.value.dashUnits,
                charGapUnits = _uiState.value.charGapUnits,
                wordGapUnits = _uiState.value.wordGapUnits,
                repeat = _uiState.value.repeatEnabled,
                repeatGapUnits = _uiState.value.repeatGapUnits
            )

            _uiState.update { it.copy(playbackState = PlaybackState.IDLE, currentSignalIndex = null, isPaused = false) }
        }
    }

    fun stopPlayback() {
        playbackJob?.cancel()
        playbackController.stopAll()
        _uiState.update {
            it.copy(
                playbackState = PlaybackState.IDLE,
                isPaused = false,
                currentCountdown = null,
                currentSignalIndex = null
            )
        }
    }

    override fun onCleared() {
        stopPlayback()
    }
}
