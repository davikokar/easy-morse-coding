package com.davide.seddio.easymorsecoding.playback

/**
 * Common interface for hardware outputs (Flashlight, Sound, Screen).
 */
interface MorsePlayer {
    fun setOutput(active: Boolean)
    fun stop() {}
}
