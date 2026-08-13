package com.example.easymorsecoding.viewmodel

import com.example.easymorsecoding.model.MorseSignal

data class MorseUiState(
    val message: String = "",
    val morseDisplay: String = "",
    val useScreen: Boolean = true,
    val useFlashlight: Boolean = false,
    val useSound: Boolean = false,
    val wpm: Int = 15,
    val countdownSeconds: Int = 0,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isPaused: Boolean = false,
    val currentCountdown: Int? = null,
    val currentSignalIndex: Int? = null,
    val signals: List<MorseSignal> = emptyList(),
    val hasFlashlight: Boolean = false,
    val flashlightExplanation: String? = null,
    val isScreenActive: Boolean = false
)

enum class PlaybackState {
    IDLE,
    COUNTDOWN,
    PLAYING,
    PAUSED
}
