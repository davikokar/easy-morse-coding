package com.example.easymorsecoding.viewmodel

import com.example.easymorsecoding.model.MorseSignal

data class MorseUiState(
    val message: String = "",
    val morseDisplay: String = "",
    val isMorseInvalid: Boolean = false,
    val useFlashlight: Boolean = false,
    val useSound: Boolean = false,
    val secondsPerUnit: Float = 0.5f,
    val dotUnits: Int = 1,
    val dashUnits: Int = 3,
    val charGapUnits: Int = 3,
    val wordGapUnits: Int = 7,
    val countdownSeconds: Int = 0,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isPaused: Boolean = false,
    val currentCountdown: Int? = null,
    val currentSignalIndex: Int? = null,
    val signals: List<MorseSignal> = emptyList(),
    val hasFlashlight: Boolean = false,
    val flashlightExplanation: String? = null
)

enum class PlaybackState {
    IDLE,
    COUNTDOWN,
    PLAYING,
    PAUSED
}
