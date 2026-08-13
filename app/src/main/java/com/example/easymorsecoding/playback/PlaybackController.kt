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
    private val onProgress: (Int?) -> Unit
) {
    /**
     * Plays a sequence of signals.
     */
    suspend fun play(
        signals: List<MorseSignal>,
        wpm: Int,
        useFlashlight: Boolean,
        useSound: Boolean,
        dotUnits: Int,
        dashUnits: Int,
        charGapUnits: Int,
        wordGapUnits: Int
    ) {
        val unitDurationMs = (1200 / wpm).toLong()

        try {
            signals.forEachIndexed { index, signal ->
                if (!coroutineContext.isActive) return@forEachIndexed

                // Handle Pause
                if (isPaused.value) {
                    stopOutputs(useFlashlight, useSound)
                    isPaused.first { !it } // Wait until unpaused
                }

                onProgress(index)

                val isActive = signal.isActive
                if (isActive) {
                    if (useFlashlight) flashlightController.setOutput(true)
                    if (useSound) soundController.setOutput(true)
                }

                val durationUnits = when (signal) {
                    MorseSignal.DOT -> dotUnits
                    MorseSignal.DASH -> dashUnits
                    MorseSignal.ELEMENT_GAP -> 1 // Internal element gap is always 1 unit by convention
                    MorseSignal.CHARACTER_GAP -> charGapUnits
                    MorseSignal.WORD_GAP -> wordGapUnits
                }

                delay(durationUnits * unitDurationMs)

                stopOutputs(useFlashlight, useSound)
            }
        } finally {
            stopAll()
        }
    }

    private fun stopOutputs(useFlashlight: Boolean, useSound: Boolean) {
        if (useFlashlight) flashlightController.setOutput(false)
        if (useSound) soundController.setOutput(false)
    }

    fun stopAll() {
        flashlightController.stop()
        soundController.stop()
        onProgress(null)
    }
}
