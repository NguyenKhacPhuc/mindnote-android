package com.mindnote.features.notedetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindnote.R
import com.mindnote.design.EmptyState
import com.mindnote.design.HairlineDivider
import com.mindnote.design.LoadingIndicator
import com.mindnote.design.MindNoteTheme
import com.mindnote.design.TagChip
import com.mindnote.design.TrackedCaps
import com.mindnote.design.VSpace
import com.mindnote.domain.model.Note
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBack: () -> Unit,
    vm: NoteDetailViewModel = koinViewModel(parameters = { parametersOf(noteId) }),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val snackbar = MindNoteTheme.snackbar
    LaunchedEffect(vm) {
        vm.effects.collect { effect ->
            when (effect) {
                NoteDetailEffect.NavigateBack -> onBack()
                is NoteDetailEffect.ShowError -> snackbar.showSnackbar(effect.message)
            }
        }
    }

    val note = state.note

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MindNoteTheme.colors.bg)
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            VSpace(44)
            TopBar(
                isFavorite = state.isFavorite,
                showActions = note != null,
                isTogglingFavorite = state.isTogglingFavorite,
                onBack = { vm.send(NoteDetailIntent.GoBack) },
                onToggleFavorite = { vm.send(NoteDetailIntent.ToggleFavorite) },
                onMore = { vm.send(NoteDetailIntent.RequestDelete) },
            )
            HairlineDivider()
            when {
                state.isLoading && note == null -> LoadingIndicator(
                    modifier = Modifier.fillMaxSize(),
                )
                note != null -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    item { Header(note) }
                    if (note.body.isNotBlank()) item { NoteBodySection(note.body) }
                }
                !state.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = stringResource(R.string.notedetail_not_found_title),
                        message = stringResource(R.string.notedetail_not_found_message),
                    )
                }
            }
        }

        if (state.confirmingDelete) {
            DeleteConfirmDialog(
                title = state.note?.title.orEmpty(),
                isDeleting = state.isDeleting,
                onConfirm = { vm.send(NoteDetailIntent.ConfirmDelete) },
                onDismiss = { vm.send(NoteDetailIntent.CancelDelete) },
            )
        }
    }
}

@Composable
private fun DeleteConfirmDialog(
    title: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = {
            Text(
                text = stringResource(R.string.notedetail_delete_title),
                style = MindNoteTheme.typography.titleSmall,
                color = MindNoteTheme.colors.text,
            )
        },
        text = {
            Text(
                text = if (title.isBlank()) stringResource(R.string.notedetail_delete_message_default)
                else stringResource(R.string.notedetail_delete_message_titled, title),
                style = MindNoteTheme.typography.bodySmall,
                color = MindNoteTheme.colors.textMuted,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isDeleting) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        color = MindNoteTheme.colors.accent,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp),
                    )
                } else {
                    Text(text = stringResource(R.string.action_delete), color = MindNoteTheme.colors.accent)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isDeleting) {
                Text(text = stringResource(R.string.action_cancel), color = MindNoteTheme.colors.textMuted)
            }
        },
        containerColor = MindNoteTheme.colors.bg,
    )
}

@Composable
private fun TopBar(
    isFavorite: Boolean,
    showActions: Boolean,
    isTogglingFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onMore: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.ArrowBack,
            contentDescription = stringResource(R.string.action_back),
            tint = MindNoteTheme.colors.text,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onBack),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(R.string.notedetail_title),
            style = MindNoteTheme.typography.titleSmall,
            color = MindNoteTheme.colors.text,
            modifier = Modifier.weight(1f),
        )
        if (showActions) {
            if (isTogglingFavorite) {
                CircularProgressIndicator(
                    color = MindNoteTheme.colors.accent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(18.dp),
                )
            } else {
                Icon(
                    imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.notedetail_favorite_remove_cd else R.string.notedetail_favorite_add_cd
                    ),
                    tint = if (isFavorite) MindNoteTheme.colors.accent else MindNoteTheme.colors.textMuted,
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(onClick = onToggleFavorite),
                )
            }
            Spacer(Modifier.width(16.dp))
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.action_delete),
                tint = MindNoteTheme.colors.textMuted,
                modifier = Modifier
                    .size(22.dp)
                    .clickable(onClick = onMore),
            )
        }
    }
}

@Composable
private fun Header(note: Note) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = note.title,
            style = MindNoteTheme.typography.h2.copy(fontSize = 24.sp, fontWeight = FontWeight.SemiBold),
            color = MindNoteTheme.colors.text,
        )
        TrackedCaps(text = note.date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH)))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            note.tags.forEach { TagChip(label = stringResource(R.string.tag_format, it), active = false) }
        }
    }
}

@Composable
private fun NoteBodySection(body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TrackedCaps(text = stringResource(R.string.notedetail_note_section))
        Text(
            text = body,
            style = MindNoteTheme.typography.bodySmall.copy(lineHeight = 23.sp),
            color = MindNoteTheme.colors.text,
        )
    }
}

