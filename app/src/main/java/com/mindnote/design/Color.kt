package com.mindnote.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class MindNoteColors(
    val bg: Color,
    val surface: Color,
    val surfaceHover: Color,
    val border: Color,
    val borderStrong: Color,
    val text: Color,
    val textMuted: Color,
    val textSubtle: Color,
    val accent: Color,
    val accentSoft: Color,
    val tagBg: Color,
    val aiBubble: Color,
)

val LightColors = MindNoteColors(
    bg = Color(0xFFFFFFFF),
    surface = Color(0xFFF7F6F3),
    surfaceHover = Color(0xFFEFEEE9),
    border = Color(0xFFE8E6E0),
    borderStrong = Color(0xFFD3D1CB),
    text = Color(0xFF1F1F1E),
    textMuted = Color(0xFF787773),
    textSubtle = Color(0xFFA8A6A1),
    accent = Color(0xFF2383E2),
    accentSoft = Color(0xFFEAF3FC),
    tagBg = Color(0xFFF1EFE9),
    aiBubble = Color(0xFFF7F6F3),
)
