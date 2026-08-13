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
enum class MorseSignal(val durationUnits: Int, val isActive: Boolean) {
    DOT(1, true),
    DASH(3, true),
    ELEMENT_GAP(1, false),
    CHARACTER_GAP(3, false),
    WORD_GAP(7, false)
}
