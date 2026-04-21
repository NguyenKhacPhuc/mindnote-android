package com.mindnote.design

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class MindNoteDimens(
    val spaceXS: Dp = 4.dp,
    val spaceS: Dp = 8.dp,
    val spaceM: Dp = 12.dp,
    val spaceL: Dp = 16.dp,
    val spaceXL: Dp = 20.dp,
    val spaceXXL: Dp = 24.dp,
    val screenPadding: Dp = 20.dp,
    val statusBarOffset: Dp = 44.dp,
    val bottomBarHeight: Dp = 64.dp,
    val radiusS: Dp = 8.dp,
    val radiusM: Dp = 10.dp,
    val radiusL: Dp = 12.dp,
    val radiusXL: Dp = 14.dp,
    val radiusPill: Dp = 26.dp,
    val iconS: Dp = 14.dp,
    val iconM: Dp = 18.dp,
    val iconL: Dp = 22.dp,
    val iconXL: Dp = 24.dp,
    val fab: Dp = 48.dp,
    val hairline: Dp = 1.dp,
)

val AppDimens = MindNoteDimens()
