package com.mindnote.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TrackedCaps(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MindNoteTheme.typography.tracked,
        color = MindNoteTheme.colors.textSubtle,
        modifier = modifier,
    )
}

@Composable
fun HairlineDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = MindNoteTheme.colors.border,
    )
}

@Composable
fun TagChip(label: String, active: Boolean = false) {
    val bg = if (active) MindNoteTheme.colors.accentSoft else MindNoteTheme.colors.tagBg
    val fg = if (active) MindNoteTheme.colors.accent else MindNoteTheme.colors.textMuted
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(text = label, style = MindNoteTheme.typography.caption, color = fg)
    }
}

@Composable
fun AiAvatar(size: Int = 24, icon: ImageVector = Icons.Outlined.AutoAwesome) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MindNoteTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = MindNoteTheme.colors.bg, modifier = Modifier.size((size * 0.6f).dp))
    }
}

@Composable
fun VSpace(dp: Int) { Spacer(Modifier.height(dp.dp)) }

@Composable
fun HSpace(dp: Int) { Spacer(Modifier.width(dp.dp)) }

@Composable
fun RowWithSpaceBetween(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = MindNoteTheme.colors.accent,
            strokeWidth = 2.dp,
            modifier = Modifier.size(size),
        )
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MindNoteTheme.colors.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MindNoteTheme.colors.textSubtle,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Text(
            text = title,
            style = MindNoteTheme.typography.titleSmall,
            color = MindNoteTheme.colors.text,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message,
            style = MindNoteTheme.typography.bodySmall,
            color = MindNoteTheme.colors.textMuted,
            textAlign = TextAlign.Center,
        )
        if (action != null) {
            Spacer(Modifier.height(4.dp))
            action()
        }
    }
}
