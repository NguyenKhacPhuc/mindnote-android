package com.mindnote.features.notedetail

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState
import com.mindnote.domain.model.Note

sealed interface NoteDetailIntent : UiIntent {
    data object ToggleFavorite : NoteDetailIntent
    data object RequestDelete : NoteDetailIntent
    data object ConfirmDelete : NoteDetailIntent
    data object CancelDelete : NoteDetailIntent
    data object GoBack : NoteDetailIntent
}

data class NoteDetailState(
    val note: Note? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = true,
    val isTogglingFavorite: Boolean = false,
    val confirmingDelete: Boolean = false,
    val isDeleting: Boolean = false,
) : UiState

sealed interface NoteDetailEffect : UiEffect {
    data object NavigateBack : NoteDetailEffect
    data class ShowError(val message: String) : NoteDetailEffect
}
