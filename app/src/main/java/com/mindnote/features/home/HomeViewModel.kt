package com.mindnote.features.home

import androidx.lifecycle.viewModelScope
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.model.SuggestedPrompt
import com.mindnote.domain.repository.NotesRepository
import com.mindnote.domain.repository.UserRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalTime

class HomeViewModel(
    private val userRepository: UserRepository,
    private val notesRepository: NotesRepository,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>(
    initial = HomeState(
        greeting = greetingFor(name = ""),
        summary = "",
        input = "",
        prompts = listOf(
            SuggestedPrompt("p1", "Summarize today's notes"),
            SuggestedPrompt("p2", "What am I working on this week?"),
            SuggestedPrompt("p3", "Draft a blog outline from my startup ideas"),
            SuggestedPrompt("p4", "Show unfinished ideas I should revisit"),
        ),
        recents = emptyList(),
    ),
) {
    init {
        viewModelScope.launch {
            userRepository.username.collectLatest { name ->
                setState { copy(greeting = greetingFor(name)) }
            }
        }
        viewModelScope.launch {
            combine(
                notesRepository.observeRecent(limit = 3),
                notesRepository.observeCount(),
            ) { recents, count -> recents to count }.collectLatest { (recents, count) ->
                setState { copy(recents = recents, summary = "You have $count notes.") }
            }
        }
        viewModelScope.launch {
            setState { copy(isSyncing = true) }
            runCatching { notesRepository.syncFirstPage() }
            setState { copy(isSyncing = false) }
        }
    }

    override suspend fun handle(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.UpdateInput -> setState { copy(input = intent.text) }
            is HomeIntent.AskNotes -> {
                val text = intent.text.trim()
                if (text.isEmpty()) return
                setState { copy(input = "") }
                emit(
                    HomeEffect.NavigateToChat(
                        conversationId = "new-" + System.currentTimeMillis(),
                        initialText = text,
                    )
                )
            }
            is HomeIntent.SelectPrompt -> {
                val prompt = currentState.prompts.firstOrNull { it.id == intent.id } ?: return
                setState { copy(input = prompt.text) }
            }
            is HomeIntent.OpenNote -> emit(HomeEffect.NavigateToNote(intent.id))
            HomeIntent.OpenNotes -> emit(HomeEffect.NavigateToNotes)
            HomeIntent.OpenCapture -> emit(HomeEffect.NavigateToCapture)
            HomeIntent.OpenProfile -> Unit
        }
    }
}

internal fun greetingFor(name: String, now: LocalTime = LocalTime.now()): String {
    val timeOfDay = when (now.hour) {
        in 5..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }
    return if (name.isBlank()) timeOfDay else "$timeOfDay, $name"
}
