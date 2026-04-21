package com.mindnote.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

val LocalMindNoteColors: ProvidableCompositionLocal<MindNoteColors> =
    staticCompositionLocalOf { LightColors }

val LocalMindNoteTypography: ProvidableCompositionLocal<MindNoteTypography> =
    staticCompositionLocalOf { AppTypography }

val LocalMindNoteDimens: ProvidableCompositionLocal<MindNoteDimens> =
    staticCompositionLocalOf { AppDimens }

val LocalSnackbarHostState: ProvidableCompositionLocal<SnackbarHostState> =
    staticCompositionLocalOf { error("LocalSnackbarHostState not provided") }

object MindNoteTheme {
    val colors: MindNoteColors
        @Composable get() = LocalMindNoteColors.current
    val typography: MindNoteTypography
        @Composable get() = LocalMindNoteTypography.current
    val dimens: MindNoteDimens
        @Composable get() = LocalMindNoteDimens.current
    val snackbar: SnackbarHostState
        @Composable get() = LocalSnackbarHostState.current
}

private val randomBgPalette = listOf(
    Color(0xFFFFE5C2),
    Color(0xFFCDE4FD),
    Color(0xFFCCEDD4),
    Color(0xFFE4D9F8),
    Color(0xFFFFF2B8),
    Color(0xFFFFD8E4),
    Color(0xFFF0D2FF),
    Color(0xFFD8E4C8),
)

@Composable
fun MindNoteTheme(content: @Composable () -> Unit) {
    var target by remember { mutableStateOf(randomBgPalette.random()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            target = randomBgPalette.filterNot { it == target }.random()
        }
    }
    val animatedBg by animateColorAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 800),
        label = "bg",
    )
    val colors = LightColors.copy(bg = animatedBg)
    val snackbarHostState = remember { SnackbarHostState() }
    val material = lightColorScheme(
        background = colors.bg,
        surface = colors.surface,
        primary = colors.accent,
        onBackground = colors.text,
        onSurface = colors.text,
        onPrimary = colors.bg,
        outline = colors.border,
    )
    CompositionLocalProvider(
        LocalMindNoteColors provides colors,
        LocalMindNoteTypography provides AppTypography,
        LocalMindNoteDimens provides AppDimens,
        LocalSnackbarHostState provides snackbarHostState,
    ) {
        MaterialTheme(colorScheme = material) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = AppDimens.bottomBarHeight + AppDimens.spaceL),
                )
            }
        }
    }
}
