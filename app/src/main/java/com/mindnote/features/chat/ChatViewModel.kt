package com.mindnote.features.chat

import androidx.lifecycle.viewModelScope
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.data.remote.ChatApi
import com.mindnote.data.remote.ChatMessageDto
import com.mindnote.data.remote.StreamEvent
import com.mindnote.domain.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ChatViewModel(
    private val conversationId: String,
    private val chatApi: ChatApi,
) : MviViewModel<ChatIntent, ChatState, ChatEffect>(initial = ChatState()) {
    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            val history = runCatching { chatApi.messages(conversationId) }.getOrElse { emptyList() }
            setState {
                val next = if (messages.isEmpty()) history.map { it.toDomain() } else messages
                copy(messages = next, isLoading = false)
            }
        }
    }

    override suspend fun handle(intent: ChatIntent) {
        when (intent) {
            is ChatIntent.SendMessage -> startStream(intent.text)
            ChatIntent.GoBack -> emit(ChatEffect.NavigateBack)
        }
    }

    private fun startStream(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty()) return
        streamJob?.cancel()

        val userMsg = ChatMessage(
            id = "u-${System.currentTimeMillis()}",
            role = ChatMessage.Role.User,
            text = text,
        )
        val assistantId = "a-${System.currentTimeMillis()}"
        val assistantPlaceholder = ChatMessage(
            id = assistantId,
            role = ChatMessage.Role.Assistant,
            text = "",
            isThinking = true,
        )

        setState { copy(messages = messages + userMsg + assistantPlaceholder, input = "") }

        streamJob = viewModelScope.launch {
            val buffer = StringBuilder()
            chatApi.stream(conversationId, text).collect { ev ->
                when (ev) {
                    is StreamEvent.Token -> {
                        buffer.append(ev.text)
                        updateAssistant(assistantId, buffer.toString(), thinking = true)
                    }
                    StreamEvent.Done -> updateAssistant(assistantId, buffer.toString(), thinking = false)
                    is StreamEvent.Error -> updateAssistant(
                        assistantId,
                        (if (buffer.isEmpty()) "⚠ " else "$buffer\n\n⚠ ") + ev.message,
                        thinking = false,
                    )
                }
            }
        }
    }

    private fun updateAssistant(id: String, text: String, thinking: Boolean) {
        setState {
            copy(
                messages = messages.map { msg ->
                    if (msg.id == id) msg.copy(text = text, isThinking = thinking) else msg
                },
            )
        }
    }
}

private fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    role = if (role == "assistant") ChatMessage.Role.Assistant else ChatMessage.Role.User,
    text = content,
)
