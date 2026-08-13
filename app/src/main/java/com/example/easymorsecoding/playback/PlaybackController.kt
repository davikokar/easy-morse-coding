package com.example.easymorsecoding.playback

import com.example.easymorsecoding.model.MorseSignal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlin.coroutines.coroutineContext

class PlaybackController(
    private val flashlightController: MorsePlayer,
    private val soundController: MorsePlayer,
    private val isPaused: StateFlow<Boolean>,
    private val onProgress: (Int?) -> Unit,
    private val onScreenOutput: (Boolean) -> Unit
) {
    /**
     * Plays a sequence of signals.
     */
    suspend fun play(
        signals: List<MorseSignal>,
        wpm: Int,
        useFlashlight: Boolean,
        useSound: Boolean,
        useScreen: Boolean
    ) {
        val unitDurationMs = (1200 / wpm).toLong()

        try {
            signals.forEachIndexed { index, signal ->
                if (!coroutineContext.isActive) return@forEachIndexed

                // Handle Pause
                if (isPaused.value) {
                    stopOutputs(useFlashlight, useSound, useScreen)
                    isPaused.first { !it } // Wait until unpaused
                }

                onProgress(index)

                val isActive = signal.isActive
                if (isActive) {
                    if (useFlashlight) flashlightController.setOutput(true)
                    if (useSound) soundController.setOutput(true)
                    if (useScreen) onScreenOutput(true)
                }

                delay(signal.durationUnits * unitDurationMs)

                stopOutputs(useFlashlight, useSound, useScreen)
            }
        } finally {
            stopAll()
        }
    }

    private fun stopOutputs(useFlashlight: Boolean, useSound: Boolean, useScreen: Boolean) {
        if (useFlashlight) flashlightController.setOutput(false)
        if (useSound) soundController.setOutput(false)
        if (useScreen) onScreenOutput(false)
    }

    fun stopAll() {
        flashlightController.stop()
        soundController.stop()
        onScreenOutput(false)
        onProgress(null)
    }
}
