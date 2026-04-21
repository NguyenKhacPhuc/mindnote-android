package com.mindnote.features.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindnote.R
import com.mindnote.design.MindNoteTheme
import com.mindnote.domain.model.OnboardingOption
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(
    onContinue: () -> Unit,
    vm: OnboardingViewModel = koinViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.effects.collect {
            if (it is OnboardingEffect.NavigateNext) onContinue()
        }
    }

    BackHandler(enabled = state.step > 0) {
        vm.send(OnboardingIntent.Back)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindNoteTheme.colors.bg)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            StepDot(active = state.step == 0)
            StepDot(active = state.step == 1)
        }

        Spacer(Modifier.height(48.dp))

        when (state.step) {
            0 -> OptionsStep(state, onSelect = { vm.send(OnboardingIntent.SelectOption(it)) })
            else -> UsernameStep(state, onUpdate = { vm.send(OnboardingIntent.UpdateUsername(it)) })
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { vm.send(OnboardingIntent.Continue) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindNoteTheme.colors.accent,
                contentColor = MindNoteTheme.colors.bg
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                focusedElevation = 0.dp,
                hoveredElevation = 0.dp
            )
        ) {
            Text(
                text = stringResource(
                    if (state.step == 0) R.string.action_continue else R.string.onboarding_get_started
                ),
                style = MindNoteTheme.typography.button,
                color = MindNoteTheme.colors.bg,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TextButton(onClick = { vm.send(OnboardingIntent.Skip) }) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    style = MindNoteTheme.typography.caption,
                    color = MindNoteTheme.colors.textSubtle
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun OptionsStep(state: OnboardingState, onSelect: (String) -> Unit) {
    Text(
        text = stringResource(R.string.onboarding_options_title),
        style = MindNoteTheme.typography.h1,
        color = MindNoteTheme.colors.text
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.onboarding_options_subtitle),
        style = MindNoteTheme.typography.body,
        color = MindNoteTheme.colors.textMuted
    )

    Spacer(Modifier.height(32.dp))

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        state.options.forEach { option ->
            OptionCard(
                option = option,
                selected = option.id == state.selectedId,
                onClick = { onSelect(option.id) }
            )
        }
    }
}

@Composable
private fun UsernameStep(state: OnboardingState, onUpdate: (String) -> Unit) {
    Text(
        text = stringResource(R.string.onboarding_name_title),
        style = MindNoteTheme.typography.h1,
        color = MindNoteTheme.colors.text
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.onboarding_name_subtitle),
        style = MindNoteTheme.typography.body,
        color = MindNoteTheme.colors.textMuted
    )

    Spacer(Modifier.height(32.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MindNoteTheme.colors.surface, RoundedCornerShape(10.dp))
            .border(1.dp, MindNoteTheme.colors.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (state.username.isEmpty()) {
            Text(
                text = stringResource(R.string.onboarding_name_placeholder),
                style = MindNoteTheme.typography.body,
                color = MindNoteTheme.colors.textSubtle,
            )
        }
        BasicTextField(
            value = state.username,
            onValueChange = onUpdate,
            singleLine = true,
            cursorBrush = SolidColor(MindNoteTheme.colors.accent),
            textStyle = MindNoteTheme.typography.body.copy(color = MindNoteTheme.colors.text),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun StepDot(active: Boolean) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .background(
                color = if (active) MindNoteTheme.colors.accent else MindNoteTheme.colors.border,
                shape = RoundedCornerShape(3.dp)
            )
    )
}

@Composable
private fun OptionCard(
    option: OnboardingOption,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) MindNoteTheme.colors.accent else MindNoteTheme.colors.border
    val bgColor = if (selected) MindNoteTheme.colors.accentSoft else MindNoteTheme.colors.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = bgColor, shape = RoundedCornerShape(10.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MindNoteTheme.colors.surfaceHover,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconFor(option.iconKey),
                contentDescription = null,
                tint = MindNoteTheme.colors.text
            )
        }

        Spacer(Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = MindNoteTheme.typography.titleSmall,
                color = MindNoteTheme.colors.text
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = option.description,
                style = MindNoteTheme.typography.caption,
                color = MindNoteTheme.colors.textMuted
            )
        }
    }
}

private fun iconFor(key: String): ImageVector = when (key) {
    "briefcase" -> Icons.Outlined.Work
    "graduation" -> Icons.Outlined.School
    "pen" -> Icons.Outlined.Edit
    "sparkles" -> Icons.Outlined.AutoAwesome
    else -> Icons.Outlined.AutoAwesome
}
