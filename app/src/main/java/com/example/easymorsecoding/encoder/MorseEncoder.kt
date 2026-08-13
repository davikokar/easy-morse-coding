package com.example.easymorsecoding.encoder

import com.example.easymorsecoding.model.MorseSignal
import java.text.Normalizer
import java.util.Locale

object MorseEncoder {

    private val MORSE_MAP = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..", 'E' to ".",
        'F' to "..-.", 'G' to "--.", 'H' to "....", 'I' to "..", 'J' to ".---",
        'K' to "-.-", 'L' to ".-..", 'M' to "--", 'N' to "-.", 'O' to "---",
        'P' to ".--.", 'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-", 'Y' to "-.--",
        'Z' to "--..",
        '0' to "-----", '1' to ".----", '2' to "..---", '3' to "...--", '4' to "....-",
        '5' to ".....", '6' to "-....", '7' to "--...", '8' to "---..", '9' to "----.",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '\'' to ".----.",
        '/' to "-..-.", '(' to "-.--.", ')' to "-.--.-", ':' to "---...",
        ';' to "-.-.-.", '=' to "-...-", '+' to ".-.-.", '-' to "-....-",
        '_' to "..--.-", '"' to ".-..-.", '$' to "...-..-", '@' to ".--.-.",
        '!' to "-.-.--"
    )

    private val REVERSE_MORSE_MAP = MORSE_MAP.entries.associate { (k, v) -> v to k }

    /**
     * Normalizes text (accents, uppercase) and converts it to a human-readable Morse string.
     */
    fun encodeToMorseString(text: String): String {
        return normalize(text).split(" ").filter { it.isNotEmpty() }.joinToString(" / ") { word ->
            word.mapNotNull { MORSE_MAP[it] }.joinToString(" ")
        }
    }

    /**
     * Decodes a Morse string into plain text. 
     * Uses ' ' as character separator and ' / ' as word separator.
     * Returns null if any part of the sequence is invalid.
     */
    fun decodeFromMorse(morse: String): String? {
        if (morse.isBlank()) return ""
        
        val words = morse.trim().split(" / ")
        val decodedWords = mutableListOf<String>()

        for (word in words) {
            val characters = word.trim().split(" ")
            val decodedChars = StringBuilder()
            for (char in characters) {
                if (char.isEmpty()) continue
                val decoded = REVERSE_MORSE_MAP[char] ?: return null
                decodedChars.append(decoded)
            }
            if (decodedChars.isNotEmpty()) {
                decodedWords.add(decodedChars.toString())
            }
        }
        
        return decodedWords.joinToString(" ")
    }

    data class SignalWithRange(
        val signal: MorseSignal,
        val range: IntRange?
    )

    /**
     * Converts text to a sequence of MorseSignal objects with their corresponding
     * character ranges in the Morse string representation.
     */
    fun encodeToSignalsWithRanges(text: String): Pair<String, List<SignalWithRange>> {
        val signalsWithRanges = mutableListOf<SignalWithRange>()
        val morseString = StringBuilder()
        
        val words = normalize(text).split(" ").filter { it.isNotEmpty() }

        words.forEachIndexed { wordIndex, word ->
            word.forEachIndexed { charIndex, char ->
                val code = MORSE_MAP[char]
                if (code != null) {
                    code.forEachIndexed { elementIndex, element ->
                        val start = morseString.length
                        morseString.append(element)
                        val end = morseString.length
                        
                        signalsWithRanges.add(SignalWithRange(
                            if (element == '.') MorseSignal.DOT else MorseSignal.DASH,
                            IntRange(start, end - 1)
                        ))
                        
                        if (elementIndex < code.length - 1) {
                            signalsWithRanges.add(SignalWithRange(MorseSignal.ELEMENT_GAP, null))
                        }
                    }
                    
                    if (charIndex < word.length - 1) {
                        val start = morseString.length
                        morseString.append(" ")
                        val end = morseString.length
                        signalsWithRanges.add(SignalWithRange(MorseSignal.CHARACTER_GAP, IntRange(start, end - 1)))
                    }
                }
            }
            if (wordIndex < words.size - 1) {
                val start = morseString.length
                morseString.append(" / ")
                val end = morseString.length
                signalsWithRanges.add(SignalWithRange(MorseSignal.WORD_GAP, IntRange(start, end - 1)))
            }
        }
        
        return morseString.toString() to signalsWithRanges
    }

    /**
     * Converts text to a sequence of MorseSignal objects for playback.
     */
    fun encodeToSignals(text: String): List<MorseSignal> {
        return encodeToSignalsWithRanges(text).second.map { it.signal }
    }

    private fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        val withoutAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutAccents.uppercase(Locale.getDefault())
            .filter { it.isLetterOrDigit() || it.isWhitespace() || MORSE_MAP.containsKey(it) }
    }
}
