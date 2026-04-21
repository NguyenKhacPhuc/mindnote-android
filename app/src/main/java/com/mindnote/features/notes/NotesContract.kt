package com.mindnote.features.notes

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState
import com.mindnote.domain.model.NoteFilter

sealed interface NotesIntent : UiIntent {
    data class SelectFilter(val filter: NoteFilter) : NotesIntent
    data class SelectTag(val tag: String) : NotesIntent
    data class OpenNote(val id: String) : NotesIntent
    data class ToggleFavorite(val id: String) : NotesIntent
    data object ToggleSearch : NotesIntent
    data class UpdateQuery(val text: String) : NotesIntent
    data object OpenCapture : NotesIntent
    data object GoBack : NotesIntent
}

data class NotesState(
    val filter: NoteFilter,
    val tags: List<String>,
    val activeTag: String,
    val favoriteIds: Set<String> = emptySet(),
    val query: String = "",
    val isSearching: Boolean = false,
) : UiState

sealed interface NotesEffect : UiEffect {
    data object NavigateBack : NotesEffect
    data class NavigateToNote(val id: String) : NotesEffect
    data object NavigateToCapture : NotesEffect
    data class ShowError(val message: String) : NotesEffect
}
