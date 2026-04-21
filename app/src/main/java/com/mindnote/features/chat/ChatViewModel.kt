package com.mindnote.features.chat

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.data.remote.ChatApi
import com.mindnote.data.remote.ChatMessageDto
import com.mindnote.data.remote.ChatPagingSource
import com.mindnote.data.remote.StreamEvent
import com.mindnote.domain.model.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ChatViewModel(
    private val conversationId: String,
    private val chatApi: ChatApi,
) : MviViewModel<ChatIntent, ChatState, ChatEffect>(initial = ChatState()) {

    private val _liveMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    /** Messages sent or streamed during this session, chronological. Rendered on top of paged history. */
    val liveMessages: StateFlow<List<ChatMessage>> = _liveMessages.asStateFlow()

    /** Paged server-side history, newest-first. */
    val historyPager: Flow<PagingData<ChatMessage>> = Pager(
        config = PagingConfig(pageSize = PAGE_SIZE, enablePlaceholders = false),
        pagingSourceFactory = { ChatPagingSource(chatApi, conversationId) },
    ).flow
        .map { data -> data.map { it.toDomain() } }
        .cachedIn(viewModelScope)

    private var streamJob: Job? = null

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

        _liveMessages.value = _liveMessages.value + userMsg + assistantPlaceholder
        setState { copy(input = "") }

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
        _liveMessages.value = _liveMessages.value.map { msg ->
            if (msg.id == id) msg.copy(text = text, isThinking = thinking) else msg
        }
    }

    companion object {
        private const val PAGE_SIZE = 30
    }
}

private fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    role = if (role == "assistant") ChatMessage.Role.Assistant else ChatMessage.Role.User,
    text = content,
)
