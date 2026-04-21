package com.mindnote.features.notes

import androidx.lifecycle.viewModelScope
import com.mindnote.core.ext.Result
import com.mindnote.core.ext.safeApiCall
import com.mindnote.core.ext.userMessage
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import com.mindnote.domain.repository.FavoritesRepository
import com.mindnote.domain.repository.NotesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class NotesViewModel(
    private val notesRepository: NotesRepository,
    private val favoritesRepository: FavoritesRepository,
) : MviViewModel<NotesIntent, NotesState, NotesEffect>(
    initial = NotesState(
        filter = NoteFilter.All,
        tags = listOf("all"),
        activeTag = "all",
        notes = emptyList(),
    ),
) {
    private val filterFlow = MutableStateFlow(NoteFilter.All)
    private val tagFlow = MutableStateFlow("all")
    private val queryFlow = MutableStateFlow("")

    init {
        viewModelScope.launch {
            combine(
                filterFlow.flatMapLatest { filter ->
                    when (filter) {
                        NoteFilter.Favorites -> favoritesRepository.observeFavorites()
                        else -> notesRepository.notes
                    }
                },
                tagFlow,
                queryFlow,
            ) { source, tag, query ->
                source
                    .let { if (tag == "all") it else it.filter { note -> tag in note.tags } }
                    .let { if (query.isBlank()) it else it.filter { note -> note.matches(query) } }
                    .sortedByDescending { it.date }
            }.collect { notes ->
                setState { copy(notes = notes) }
            }
        }
        viewModelScope.launch {
            notesRepository.notes.collect { all ->
                val tags = listOf("all") + all.flatMap { it.tags }.distinct().sorted()
                setState { copy(tags = tags) }
            }
        }
        viewModelScope.launch {
            favoritesRepository.observeFavorites().collect { favs ->
                setState { copy(favoriteIds = favs.map { it.id }.toSet()) }
            }
        }
        viewModelScope.launch {
            setState { copy(isSyncing = true) }
            runCatching {
                notesRepository.refresh()
                favoritesRepository.refresh()
            }
            setState { copy(isSyncing = false) }
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

private fun Note.matches(query: String): Boolean {
    return title.contains(query, ignoreCase = true) ||
        preview.contains(query, ignoreCase = true) ||
        body.contains(query, ignoreCase = true) ||
        tags.any { it.contains(query, ignoreCase = true) }
}
