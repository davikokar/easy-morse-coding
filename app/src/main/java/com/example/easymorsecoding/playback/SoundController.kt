package com.example.easymorsecoding.playback

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

class SoundController : MorsePlayer {
    private val sampleRate = 44100
    private val frequency = 700.0
    private var audioTrack: AudioTrack? = null
    private val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    private val samples = ShortArray(bufferSize)
    private var phase = 0.0
    private val isActive = AtomicBoolean(false)
    private val isRunning = AtomicBoolean(false)
    private var playbackThread: Thread? = null

    init {
        // Prepare AudioTrack eagerly to reduce latency when first signal starts
        ensureAudioTrack()
    }

    private fun ensureAudioTrack() {
        if (audioTrack == null) {
            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        }
    }

    override fun setOutput(active: Boolean) {
        isActive.set(active)
        if (active) {
            startPlaybackThread()
        }
    }

    private fun startPlaybackThread() {
        if (isRunning.getAndSet(true)) return

        ensureAudioTrack()
        audioTrack?.play()
        
        playbackThread = Thread {
            while (isRunning.get()) {
                if (isActive.get()) {
                    for (i in samples.indices) {
                        samples[i] = (sin(phase) * Short.MAX_VALUE).toInt().toShort()
                        phase += 2.0 * PI * frequency / sampleRate
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                    audioTrack?.write(samples, 0, samples.size)
                } else {
                    // Silence - write zeros to keep buffer filled and avoid underruns/latency
                    samples.fill(0)
                    audioTrack?.write(samples, 0, samples.size)
                    // If inactive for a while, we could stop the thread, but for Morse signals,
                    // it's better to keep it hot during the sequence.
                }
            }
        }.apply { 
            priority = Thread.MAX_PRIORITY
            start() 
        }
    }

    override fun stop() {
        isRunning.set(false)
        isActive.set(false)
        playbackThread?.interrupt()
        playbackThread = null
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        phase = 0.0
    }
}
