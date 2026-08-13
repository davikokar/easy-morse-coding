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
        useScreen = savedStateHandle["useScreen"] ?: true,
        useFlashlight = savedStateHandle["useFlashlight"] ?: false,
        useSound = savedStateHandle["useSound"] ?: false,
        wpm = savedStateHandle["wpm"] ?: 15,
        countdownSeconds = savedStateHandle["countdownSeconds"] ?: 0
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
        onProgress = { index -> _uiState.update { it.copy(currentSignalIndex = index) } },
        onScreenOutput = { active -> _uiState.update { it.copy(isScreenActive = active) } }
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
                morseDisplay = MorseEncoder.encodeToMorseString(newMessage)
            )
        }
        save("message", newMessage)
    }

    fun onToggleScreen(enabled: Boolean) {
        _uiState.update { it.copy(useScreen = enabled) }
        save("useScreen", enabled)
    }

    fun onToggleFlashlight(enabled: Boolean) {
        _uiState.update { it.copy(useFlashlight = enabled) }
        save("useFlashlight", enabled)
    }

    fun onToggleSound(enabled: Boolean) {
        _uiState.update { it.copy(useSound = enabled) }
        save("useSound", enabled)
    }

    fun onWpmChange(newWpm: Int) {
        _uiState.update { it.copy(wpm = newWpm) }
        save("wpm", newWpm)
    }

    fun onCountdownSecondsChange(seconds: Int) {
        _uiState.update { it.copy(countdownSeconds = seconds) }
        save("countdownSeconds", seconds)
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
            val signals = MorseEncoder.encodeToSignals(_uiState.value.message)
            _uiState.update { 
                it.copy(
                    signals = signals, 
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
                wpm = _uiState.value.wpm,
                useFlashlight = _uiState.value.useFlashlight,
                useSound = _uiState.value.useSound,
                useScreen = _uiState.value.useScreen
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
                currentSignalIndex = null,
                isScreenActive = false
            )
        }
    }

    override fun onCleared() {
        stopPlayback()
    }
}
