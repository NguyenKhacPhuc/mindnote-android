package com.mindnote.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindnote.R
import com.mindnote.design.EmptyState
import com.mindnote.design.HSpace
import com.mindnote.design.LoadingIndicator
import com.mindnote.design.MindNoteTheme
import com.mindnote.design.TrackedCaps
import com.mindnote.design.VSpace
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.SuggestedPrompt
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onOpenChat: (conversationId: String, initialText: String?) -> Unit,
    onOpenNotes: () -> Unit,
    onOpenCapture: () -> Unit,
    onOpenNote: (String) -> Unit,
    vm: HomeViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToChat -> onOpenChat(effect.conversationId, effect.initialText)
                HomeEffect.NavigateToNotes -> onOpenNotes()
                HomeEffect.NavigateToCapture -> onOpenCapture()
                is HomeEffect.NavigateToNote -> onOpenNote(effect.id)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindNoteTheme.colors.bg)
            .navigationBarsPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 44.dp, bottom = 88.dp, start = 20.dp, end = 20.dp),
        ) {
            item { TopBar() }
            item {
                VSpace(20)
                Text(state.greeting, style = MindNoteTheme.typography.title, color = MindNoteTheme.colors.text)
                VSpace(4)
                Text(state.summary, style = MindNoteTheme.typography.bodySmall, color = MindNoteTheme.colors.textMuted)
            }
            item {
                VSpace(24)
                InputArea(
                    input = state.input,
                    onChange = { vm.send(HomeIntent.UpdateInput(it)) },
                    onSend = { vm.send(HomeIntent.AskNotes(state.input)) },
                )
            }
            item {
                VSpace(24)
                TrackedCaps(stringResource(R.string.home_suggested))
                VSpace(12)
            }
            items(state.prompts) { prompt ->
                PromptRow(prompt) { vm.send(HomeIntent.SelectPrompt(prompt.id)) }
                VSpace(8)
            }
            item {
                VSpace(20)
                TrackedCaps(stringResource(R.string.home_recent_notes))
                VSpace(12)
                when {
                    state.recents.isEmpty() && state.isSyncing -> LoadingIndicator()
                    state.recents.isEmpty() -> EmptyState(
                        icon = Icons.Outlined.NoteAdd,
                        title = stringResource(R.string.home_empty_title),
                        message = stringResource(R.string.home_empty_message),
                    )
                    else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.recents) { note ->
                            RecentNoteCard(note) { vm.send(HomeIntent.OpenNote(note.id)) }
                        }
                    }
                }
            }
        }

        BottomTabBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onNotes = { vm.send(HomeIntent.OpenNotes) },
            onCapture = { vm.send(HomeIntent.OpenCapture) },
        )
    }
}

@Composable
private fun TopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(stringResource(R.string.app_name), style = MindNoteTheme.typography.titleSmall, color = MindNoteTheme.colors.text)
    }
}

@Composable
private fun InputArea(
    input: String,
    onChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    val canSend = input.isNotBlank()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MindNoteTheme.colors.surface)
            .border(1.dp, MindNoteTheme.colors.border, RoundedCornerShape(14.dp))
            .padding(18.dp),
    ) {
        if (input.isEmpty()) {
            Text(
                text = stringResource(R.string.home_input_placeholder),
                style = MindNoteTheme.typography.body,
                color = MindNoteTheme.colors.textSubtle,
            )
        }
        BasicTextField(
            value = input,
            onValueChange = onChange,
            textStyle = MindNoteTheme.typography.body.copy(color = MindNoteTheme.colors.text),
            cursorBrush = SolidColor(MindNoteTheme.colors.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 28.dp),
        )
        Icon(
            imageVector = Icons.Outlined.ArrowUpward,
            contentDescription = stringResource(R.string.chat_send_cd),
            tint = if (canSend) MindNoteTheme.colors.accent else MindNoteTheme.colors.textSubtle,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(20.dp)
                .clickable(enabled = canSend, onClick = onSend),
        )
    }
}

@Composable
private fun PromptRow(prompt: SuggestedPrompt, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MindNoteTheme.colors.surface)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.AutoAwesome,
            contentDescription = null,
            tint = MindNoteTheme.colors.accent,
            modifier = Modifier.size(16.dp),
        )
        HSpace(12)
        Text(prompt.text, style = MindNoteTheme.typography.bodySmall, color = MindNoteTheme.colors.text)
    }
}

@Composable
private fun RecentNoteCard(note: Note, onClick: () -> Unit) {
    val dateLabel = note.date.format(DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)).uppercase()
    Column(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MindNoteTheme.colors.surface)
            .clickable { onClick() }
            .padding(14.dp),
    ) {
        TrackedCaps(dateLabel)
        VSpace(8)
        Text(
            note.title,
            style = MindNoteTheme.typography.titleSmall,
            color = MindNoteTheme.colors.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        VSpace(6)
        Text(
            note.preview,
            style = MindNoteTheme.typography.caption,
            color = MindNoteTheme.colors.textMuted,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BottomTabBar(modifier: Modifier, onNotes: () -> Unit, onCapture: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(MindNoteTheme.colors.bg),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItem(stringResource(R.string.tab_chat), Icons.Outlined.ChatBubbleOutline, active = true) {}
            Box(modifier = Modifier.size(48.dp)) {}
            TabItem(stringResource(R.string.tab_notes), Icons.Outlined.MenuBook, active = false, onClick = onNotes)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-20).dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(MindNoteTheme.colors.accent)
                .clickable { onCapture() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun TabItem(label: String, icon: ImageVector, active: Boolean, onClick: () -> Unit) {
    val tint = if (active) MindNoteTheme.colors.accent else MindNoteTheme.colors.textMuted
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        VSpace(4)
        Text(label, style = MindNoteTheme.typography.caption, color = tint)
    }
}
