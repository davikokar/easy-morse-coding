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

    /**
     * Normalizes text (accents, uppercase) and converts it to a human-readable Morse string.
     */
    fun encodeToMorseString(text: String): String {
        return normalize(text).split(" ").filter { it.isNotEmpty() }.joinToString(" / ") { word ->
            word.mapNotNull { MORSE_MAP[it] }.joinToString(" ")
        }
    }

    /**
     * Converts text to a sequence of MorseSignal objects for playback.
     */
    fun encodeToSignals(text: String): List<MorseSignal> {
        val signals = mutableListOf<MorseSignal>()
        val words = normalize(text).split(" ").filter { it.isNotEmpty() }

        words.forEachIndexed { wordIndex, word ->
            word.forEachIndexed { charIndex, char ->
                val code = MORSE_MAP[char]
                if (code != null) {
                    code.forEachIndexed { elementIndex, element ->
                        signals.add(if (element == '.') MorseSignal.DOT else MorseSignal.DASH)
                        if (elementIndex < code.length - 1) {
                            signals.add(MorseSignal.ELEMENT_GAP)
                        }
                    }
                    if (charIndex < word.length - 1) {
                        signals.add(MorseSignal.CHARACTER_GAP)
                    }
                }
            }
            if (wordIndex < words.size - 1) {
                signals.add(MorseSignal.WORD_GAP)
            }
        }
        return signals
    }

    private fun normalize(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        val withoutAccents = normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        return withoutAccents.uppercase(Locale.getDefault())
            .filter { it.isLetterOrDigit() || it.isWhitespace() || MORSE_MAP.containsKey(it) }
    }
}
