package com.mindnote.features.notes

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.mindnote.core.ext.Result
import com.mindnote.core.ext.safeApiCall
import com.mindnote.core.ext.userMessage
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import com.mindnote.domain.repository.FavoritesRepository
import com.mindnote.domain.repository.NotesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val favoritesRepository: FavoritesRepository,
) : MviViewModel<NotesIntent, NotesState, NotesEffect>(
    initial = NotesState(
        filter = NoteFilter.All,
        tags = listOf("all"),
        activeTag = "all",
    ),
) {
    private val filterFlow = MutableStateFlow(NoteFilter.All)
    private val tagFlow = MutableStateFlow("all")
    private val queryFlow = MutableStateFlow("")

    val notesPager: Flow<PagingData<Note>> =
        combine(filterFlow, tagFlow, queryFlow) { f, t, q -> Triple(f, t, q) }
            .flatMapLatest { (f, t, q) -> notesRepository.notesPager(f, t, q) }
            .cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            notesRepository.observeDistinctTags().collect { topics ->
                setState { copy(tags = listOf("all") + topics) }
            }
        }
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { favs ->
                setState { copy(favoriteIds = favs.map { it.id }.toSet()) }
            }
        }
        viewModelScope.launch {
            runCatching { favoritesRepository.refresh() }
        }
    }

    override suspend fun handle(intent: NotesIntent) {
        when (intent) {
            is NotesIntent.SelectFilter -> {
                filterFlow.value = intent.filter
                setState { copy(filter = intent.filter) }
            }
            is NotesIntent.SelectTag -> {
                tagFlow.value = intent.tag
                setState { copy(activeTag = intent.tag) }
            }
            NotesIntent.ToggleSearch -> {
                val next = !currentState.isSearching
                if (!next) queryFlow.value = ""
                setState { copy(isSearching = next, query = if (next) query else "") }
            }
            is NotesIntent.UpdateQuery -> {
                queryFlow.value = intent.text
                setState { copy(query = intent.text) }
            }
            is NotesIntent.OpenNote -> emit(NotesEffect.NavigateToNote(intent.id))
            is NotesIntent.ToggleFavorite -> {
                val result = safeApiCall { favoritesRepository.toggle(intent.id) }
                if (result is Result.Error) {
                    emit(NotesEffect.ShowError(result.userMessage("Couldn't update favorite")))
                }
            }
            NotesIntent.OpenCapture -> emit(NotesEffect.NavigateToCapture)
            NotesIntent.GoBack -> emit(NotesEffect.NavigateBack)
        }
    }
}
