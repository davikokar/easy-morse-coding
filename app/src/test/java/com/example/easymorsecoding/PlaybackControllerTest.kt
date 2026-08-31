package com.example.easymorsecoding

import com.example.easymorsecoding.model.MorseSignal
import com.example.easymorsecoding.playback.MorsePlayer
import com.example.easymorsecoding.playback.PlaybackController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlaybackControllerTest {

    class MockPlayer : MorsePlayer {
        val outputs = mutableListOf<Boolean>()
        var stopped = false

        override fun setOutput(active: Boolean) {
            outputs.add(active)
        }

        override fun stop() {
            stopped = true
            setOutput(false)
        }
    }

    @Test
    fun testPlaybackSequence() = runTest {
        val flashlight = MockPlayer()
        val sound = MockPlayer()
        val isPaused = MutableStateFlow(false)
        
        val controller = PlaybackController(
            flashlightController = flashlight,
            soundController = sound,
            isPaused = isPaused,
            onProgress = {}
        )

        val signals = listOf(MorseSignal.DOT)
        
        controller.play(
            signals = signals,
            secondsPerUnit = 0.001f, // 1ms per unit for fast test
            useFlashlight = true,
            useSound = true,
            dotUnits = 1,
            dashUnits = 3,
            charGapUnits = 3,
            wordGapUnits = 7
        )

        // DOT is active, then it turns off in loop, then finally stopAll() calls setOutput(false) again for safety
        assertEquals(listOf(true, false, false), flashlight.outputs)
        assertEquals(listOf(true, false, false), sound.outputs)
    }

    @Test
    fun repeatPlaybackWaitsForConfiguredGap() = runTest {
        val flashlight = MockPlayer()
        val sound = MockPlayer()
        val controller = PlaybackController(
            flashlightController = flashlight,
            soundController = sound,
            isPaused = MutableStateFlow(false),
            onProgress = {}
        )

        val playbackJob = launch {
            controller.play(
                signals = listOf(MorseSignal.DOT),
                secondsPerUnit = 0.001f,
                useFlashlight = true,
                useSound = false,
                dotUnits = 1,
                dashUnits = 3,
                charGapUnits = 3,
                wordGapUnits = 7,
                repeat = true,
                repeatGapUnits = 14
            )
        }

        runCurrent()
        assertEquals(listOf(true), flashlight.outputs)

        advanceTimeBy(14)
        runCurrent()
        assertEquals(listOf(true, false), flashlight.outputs)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(listOf(true, false, true), flashlight.outputs)

        playbackJob.cancelAndJoin()
    }
}
