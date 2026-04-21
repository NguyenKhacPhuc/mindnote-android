package com.mindnote.features.home

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.SuggestedPrompt

sealed interface HomeIntent : UiIntent {
    data class UpdateInput(val text: String) : HomeIntent
    data class AskNotes(val text: String) : HomeIntent
    data class SelectPrompt(val id: String) : HomeIntent
    data class OpenNote(val id: String) : HomeIntent
    data object OpenNotes : HomeIntent
    data object OpenCapture : HomeIntent
    data object OpenProfile : HomeIntent
}

data class HomeState(
    val greeting: String,
    val summary: String,
    val input: String,
    val prompts: List<SuggestedPrompt>,
    val recents: List<Note>,
    val isSyncing: Boolean = false,
) : UiState

sealed interface HomeEffect : UiEffect {
    data class NavigateToChat(val conversationId: String, val initialText: String? = null) : HomeEffect
    data object NavigateToNotes : HomeEffect
    data object NavigateToCapture : HomeEffect
    data class NavigateToNote(val id: String) : HomeEffect
}
