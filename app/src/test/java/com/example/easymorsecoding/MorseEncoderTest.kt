package com.example.easymorsecoding

import com.example.easymorsecoding.encoder.MorseEncoder
import com.example.easymorsecoding.model.MorseSignal
import org.junit.Assert.assertEquals
import org.junit.Test

class MorseEncoderTest {

    @Test
    fun testEncodeSOS() {
        val morse = MorseEncoder.encodeToMorseString("SOS")
        assertEquals("... --- ...", morse)
    }

    @Test
    fun testEncodeLowercase() {
        val morse = MorseEncoder.encodeToMorseString("sos")
        assertEquals("... --- ...", morse)
    }

    @Test
    fun testEncodeWithAccents() {
        val morse = MorseEncoder.encodeToMorseString("é")
        assertEquals(".", morse) // é should normalize to E
    }

    @Test
    fun testEncodeWithSpaces() {
        val morse = MorseEncoder.encodeToMorseString("A B")
        assertEquals(".- / -...", morse)
    }

    @Test
    fun testEncodeSignalsSOS() {
        val signals = MorseEncoder.encodeToSignals("S")
        // S is "...", so signals should be DOT, ELEMENT_GAP, DOT, ELEMENT_GAP, DOT
        val expected = listOf(
            MorseSignal.DOT,
            MorseSignal.ELEMENT_GAP,
            MorseSignal.DOT,
            MorseSignal.ELEMENT_GAP,
            MorseSignal.DOT
        )
        assertEquals(expected, signals)
    }

    @Test
    fun testUnsupportedCharacters() {
        val morse = MorseEncoder.encodeToMorseString("S#S")
        assertEquals("... ...", morse) // # should be ignored
    }

    @Test
    fun testDecodeSOS() {
        val text = MorseEncoder.decodeFromMorse("... --- ...")
        assertEquals("SOS", text)
    }

    @Test
    fun testDecodeWithSpaces() {
        val text = MorseEncoder.decodeFromMorse(".- / -...")
        assertEquals("A B", text)
    }

    @Test
    fun testDecodeInvalid() {
        val text = MorseEncoder.decodeFromMorse("........")
        assertEquals(null, text)
    }
}
