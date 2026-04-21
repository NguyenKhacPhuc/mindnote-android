package com.mindnote.features.notes

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mindnote.R
import com.mindnote.design.EmptyState
import com.mindnote.design.HairlineDivider
import com.mindnote.design.LoadingIndicator
import com.mindnote.design.MindNoteTheme
import com.mindnote.design.RowWithSpaceBetween
import com.mindnote.design.TagChip
import com.mindnote.design.TrackedCaps
import com.mindnote.design.VSpace
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun NotesScreen(
    onBack: () -> Unit,
    onOpenNote: (String) -> Unit,
    onOpenCapture: () -> Unit,
    onOpenChat: () -> Unit,
    vm: NotesViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()

    val snackbar = MindNoteTheme.snackbar
    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                NotesEffect.NavigateBack -> onBack()
                is NotesEffect.NavigateToNote -> onOpenNote(effect.id)
                NotesEffect.NavigateToCapture -> onOpenCapture()
                is NotesEffect.ShowError -> snackbar.showSnackbar(effect.message)
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
            contentPadding = PaddingValues(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                VSpace(4)
                if (state.isSearching) {
                    SearchBar(
                        query = state.query,
                        onQueryChange = { vm.send(NotesIntent.UpdateQuery(it)) },
                        onClose = { vm.send(NotesIntent.ToggleSearch) },
                    )
                } else {
                    RowWithSpaceBetween {
                        Text(
                            text = stringResource(R.string.notes_title),
                            style = MindNoteTheme.typography.h2,
                            color = MindNoteTheme.colors.text,
                        )
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.action_search),
                            tint = MindNoteTheme.colors.text,
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { vm.send(NotesIntent.ToggleSearch) },
                        )
                    }
                }
            }
            item {
                VSpace(4)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    NoteFilter.values().forEach { filter ->
                        FilterTab(
                            label = filter.label,
                            active = state.filter == filter,
                            onClick = { vm.send(NotesIntent.SelectFilter(filter)) },
                        )
                    }
                }
            }
            item {
                HairlineDivider()
            }
            item {
                VSpace(4)
                RowWithSpaceBetween {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.tags) { tag ->
                            Box(modifier = Modifier.clickable { vm.send(NotesIntent.SelectTag(tag)) }) {
                                TagChip(label = stringResource(R.string.tag_format, tag), active = tag == state.activeTag)
                            }
                        }
                    }
                    TrackedCaps(stringResource(R.string.notes_count_suffix, state.notes.size))
                }
            }
            if (state.notes.isEmpty() && state.isSyncing) {
                item {
                    VSpace(40)
                    LoadingIndicator()
                }
            } else if (state.notes.isEmpty()) {
                item {
                    VSpace(40)
                    val isFavoritesTab = state.filter == NoteFilter.Favorites
                    EmptyState(
                        icon = if (isFavoritesTab) Icons.Outlined.FavoriteBorder else Icons.Outlined.NoteAdd,
                        title = when {
                            isFavoritesTab -> stringResource(R.string.notes_empty_favorites_title)
                            state.activeTag != "all" -> stringResource(R.string.notes_empty_tag_title, state.activeTag)
                            else -> stringResource(R.string.notes_empty_all_title)
                        },
                        message = when {
                            isFavoritesTab -> stringResource(R.string.notes_empty_favorites_message)
                            state.activeTag != "all" -> stringResource(R.string.notes_empty_tag_message)
                            else -> stringResource(R.string.notes_empty_all_message)
                        },
                    )
                }
            } else {
                items(state.notes) { note ->
                    NoteCard(
                        note = note,
                        isFavorite = note.id in state.favoriteIds,
                        onClick = { vm.send(NotesIntent.OpenNote(note.id)) },
                        onToggleFavorite = { vm.send(NotesIntent.ToggleFavorite(note.id)) },
                    )
                }
            }
        }

        BottomTabBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onCapture = { vm.send(NotesIntent.OpenCapture) },
            onChat = onOpenChat,
        )
    }
}

@Composable
private fun FilterTab(label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = label,
            style = MindNoteTheme.typography.label,
            color = if (active) MindNoteTheme.colors.text else MindNoteTheme.colors.textMuted,
            fontWeight = if (active) FontWeight.Medium else FontWeight.Normal,
        )
        VSpace(6)
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(if (active) MindNoteTheme.colors.accent else Color.Transparent),
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    val dateLabel = note.date
        .format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH))
        .uppercase()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MindNoteTheme.colors.bg)
            .border(1.dp, MindNoteTheme.colors.border, shape)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RowWithSpaceBetween {
            Text(
                text = note.title,
                style = MindNoteTheme.typography.titleSmall,
                color = MindNoteTheme.colors.text,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = if (isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = stringResource(
                    if (isFavorite) R.string.notedetail_favorite_remove_cd else R.string.notedetail_favorite_add_cd
                ),
                tint = if (isFavorite) MindNoteTheme.colors.accent else MindNoteTheme.colors.textMuted,
                modifier = Modifier
                    .size(18.dp)
                    .clickable(onClick = onToggleFavorite),
            )
        }
        Text(
            text = note.preview,
            style = MindNoteTheme.typography.bodySmall,
            color = MindNoteTheme.colors.textMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 21.sp,
        )
        RowWithSpaceBetween {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                note.tags.take(2).forEach { tag ->
                    TagChip(label = stringResource(R.string.tag_format, tag))
                }
            }
            TrackedCaps(dateLabel)
        }
    }
}

@Composable
private fun BottomTabBar(modifier: Modifier, onCapture: () -> Unit, onChat: () -> Unit) {
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
            TabItem(stringResource(R.string.tab_chat), Icons.Outlined.ChatBubbleOutline, active = false, onClick = onChat)
            Box(modifier = Modifier.size(48.dp)) {}
            TabItem(stringResource(R.string.tab_notes), Icons.AutoMirrored.Outlined.MenuBook, active = true) {}
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

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MindNoteTheme.colors.surface)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MindNoteTheme.colors.textMuted,
            modifier = Modifier.size(18.dp),
        )
        Box(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            if (query.isEmpty()) {
                Text(
                    text = stringResource(R.string.notes_search_placeholder),
                    style = MindNoteTheme.typography.body,
                    color = MindNoteTheme.colors.textSubtle,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MindNoteTheme.typography.body.copy(color = MindNoteTheme.colors.text),
                cursorBrush = SolidColor(MindNoteTheme.colors.accent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
            )
        }
        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = stringResource(R.string.notes_search_close_cd),
            tint = MindNoteTheme.colors.textMuted,
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onClose),
        )
    }
}
