package com.example.easymorsecoding.model

/**
 * Represents the fundamental elements of Morse code and the gaps between them.
 * 
 * Standard Morse timing:
 * - Dot: 1 unit
 * - Dash: 3 units
 * - Intra-character gap: 1 unit
 * - Inter-character gap: 3 units
 * - Word gap: 7 units
 */
enum class MorseSignal(val isActive: Boolean) {
    DOT(true),
    DASH(true),
    ELEMENT_GAP(false),
    CHARACTER_GAP(false),
    WORD_GAP(false)
}
