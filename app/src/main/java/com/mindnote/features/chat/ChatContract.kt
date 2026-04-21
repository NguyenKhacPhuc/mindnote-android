package com.mindnote.features.chat

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState
import com.mindnote.domain.model.ChatMessage

sealed interface ChatIntent : UiIntent {
    data class SendMessage(val text: String) : ChatIntent
    data object GoBack : ChatIntent
}

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val isLoading: Boolean = true,
) : UiState

sealed interface ChatEffect : UiEffect {
    data object NavigateBack : ChatEffect
}
