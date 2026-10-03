package com.davide.seddio.easymorsecoding.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
// Structural silkscreen stamping: heavy, wide-tracked, uppercase.
private val Stencil = FontFamily.SansSerif

// Telemetry / readouts: fixed pitch, unambiguous glyphs.
private val Readout = FontFamily.Monospace

private val HeadlineXl = TextStyle(
    fontFamily = Stencil,
    fontWeight = FontWeight.Black,
    fontSize = 30.sp,
    lineHeight = 36.sp,
    letterSpacing = 1.5.sp
)

private val HeadlineLg = TextStyle(
    fontFamily = Stencil,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 24.sp,
    lineHeight = 30.sp,
    letterSpacing = 1.0.sp
)

private val HeadlineMd = TextStyle(
    fontFamily = Stencil,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.7.sp
)

private val LabelLg = TextStyle(
    fontFamily = Stencil,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    letterSpacing = 1.3.sp
)

private val LabelMd = TextStyle(
    fontFamily = Readout,
    fontWeight = FontWeight.Bold,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.3.sp
)

private val LabelSm = TextStyle(
    fontFamily = Readout,
    fontWeight = FontWeight.Bold,
    fontSize = 10.sp,
    lineHeight = 12.sp,
    letterSpacing = 1.5.sp
)

val Typography = Typography(
    displayLarge = HeadlineXl,
    displayMedium = HeadlineLg,
    displaySmall = HeadlineMd,
    headlineLarge = HeadlineXl,
    headlineMedium = HeadlineLg,
    headlineSmall = HeadlineMd,
    titleLarge = HeadlineMd,
    titleMedium = LabelLg,
    titleSmall = LabelMd,
    bodyLarge = TextStyle(
        fontFamily = Readout,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Readout,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Readout,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.24.sp
    ),
    labelLarge = LabelLg,
    labelMedium = LabelMd,
    labelSmall = LabelSm
)