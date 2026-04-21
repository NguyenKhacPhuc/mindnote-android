package com.mindnote.features.notedetail

import androidx.lifecycle.viewModelScope
import com.mindnote.core.ext.Result
import com.mindnote.core.ext.safeApiCall
import com.mindnote.core.ext.userMessage
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.repository.FavoritesRepository
import com.mindnote.domain.repository.NotesRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NoteDetailViewModel(
    private val noteId: String,
    private val notesRepository: NotesRepository,
    private val favoritesRepository: FavoritesRepository,
) : MviViewModel<NoteDetailIntent, NoteDetailState, NoteDetailEffect>(NoteDetailState()) {

    init {
        viewModelScope.launch {
            combine(
                notesRepository.observeNote(noteId),
                favoritesRepository.observeIsFavorite(noteId),
            ) { note, isFavorite -> note to isFavorite }
                .collect { (note, isFavorite) ->
                    setState {
                        copy(note = note, isFavorite = isFavorite, isLoading = false)
                    }
                }
        }
    }

    override suspend fun handle(intent: NoteDetailIntent) {
        when (intent) {
            NoteDetailIntent.ToggleFavorite -> {
                if (currentState.isTogglingFavorite) return
                setState { copy(isTogglingFavorite = true) }
                val result = safeApiCall { favoritesRepository.toggle(noteId) }
                setState { copy(isTogglingFavorite = false) }
                if (result is Result.Error) {
                    emit(NoteDetailEffect.ShowError(result.userMessage("Couldn't update favorite")))
                }
            }
            NoteDetailIntent.RequestDelete -> setState { copy(confirmingDelete = true) }
            NoteDetailIntent.CancelDelete -> setState { copy(confirmingDelete = false) }
            NoteDetailIntent.ConfirmDelete -> {
                if (currentState.isDeleting) return
                setState { copy(isDeleting = true) }
                val result = safeApiCall { notesRepository.delete(noteId) }
                setState { copy(isDeleting = false, confirmingDelete = false) }
                when (result) {
                    is Result.Success -> emit(NoteDetailEffect.NavigateBack)
                    is Result.Error -> emit(NoteDetailEffect.ShowError(result.userMessage("Couldn't delete note")))
                    Result.Loading -> Unit
                }
            }
            NoteDetailIntent.GoBack -> emit(NoteDetailEffect.NavigateBack)
        }
    }
}
