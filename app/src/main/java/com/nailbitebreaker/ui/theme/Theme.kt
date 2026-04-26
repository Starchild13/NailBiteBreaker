package com.nailbitebreaker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Colour palette ────────────────────────────────────────────────────────────

/** Soft lavender — primary colour, conveys calm and mindfulness. */
val Lavender = Color(0xFF9C89CC)
val LavenderDark = Color(0xFF6A5E99)
val LavenderContainer = Color(0xFFEDE7FF)

/** Teal accent — positive reinforcement moments (streaks, success). */
val TealAccent = Color(0xFF4DB6AC)
val TealDark = Color(0xFF00897B)

/** Warm coral — urge button; attention-grabbing but not alarming. */
val CoralButton = Color(0xFFFF7043)

val BackgroundLight = Color(0xFFF8F5FF)
val BackgroundDark = Color(0xFF1A1625)
val SurfaceDark = Color(0xFF241E36)

// ── Material 3 colour schemes ─────────────────────────────────────────────────

private val LightColors = lightColorScheme(
    primary = Lavender,
    onPrimary = Color.White,
    primaryContainer = LavenderContainer,
    onPrimaryContainer = LavenderDark,
    secondary = TealAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFB2DFDB),
    onSecondaryContainer = TealDark,
    background = BackgroundLight,
    onBackground = Color(0xFF1C1B2E),
    surface = Color.White,
    onSurface = Color(0xFF1C1B2E),
    error = CoralButton,
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Lavender,
    onPrimary = Color(0xFF1A0E4A),
    primaryContainer = LavenderDark,
    onPrimaryContainer = LavenderContainer,
    secondary = TealAccent,
    onSecondary = Color(0xFF00352F),
    secondaryContainer = TealDark,
    onSecondaryContainer = Color(0xFFB2DFDB),
    background = BackgroundDark,
    onBackground = Color(0xFFE8E0F5),
    surface = SurfaceDark,
    onSurface = Color(0xFFE8E0F5),
    error = CoralButton,
    onError = Color.White
)

// ── Typography ────────────────────────────────────────────────────────────────

val AppTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

// ── Theme composable ──────────────────────────────────────────────────────────

/**
 * Root Material 3 theme for NailBiteBreaker.
 *
 * Uses a calming lavender / teal palette that supports both light and dark modes.
 * The coral accent is reserved for the urge button only, so it retains its
 * "call-to-action" visual weight.
 *
 * @param darkTheme When true, applies [DarkColors]; defaults to system preference.
 * @param content   The composable tree to theme.
 */
@Composable
fun NailBiteBreakerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = AppTypography,
        content = content
    )
}
