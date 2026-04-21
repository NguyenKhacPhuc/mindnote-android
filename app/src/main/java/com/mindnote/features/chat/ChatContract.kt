package com.mindnote.features.chat

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState

sealed interface ChatIntent : UiIntent {
    data class SendMessage(val text: String) : ChatIntent
    data object GoBack : ChatIntent
}

data class ChatState(
    val input: String = "",
) : UiState

sealed interface ChatEffect : UiEffect {
    data object NavigateBack : ChatEffect
}
