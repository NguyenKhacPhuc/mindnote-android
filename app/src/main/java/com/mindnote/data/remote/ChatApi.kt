package com.mindnote.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow

class ChatApi(private val client: HttpClient) {

    /**
     * Load a page of messages newest-first. Pass [before] = oldest known `createdAt`
     * to fetch the previous (older) page. Fewer than [limit] results signals end-of-history.
     */
    suspend fun messages(
        conversationId: String,
        before: Long? = null,
        limit: Int = 30,
    ): List<ChatMessageDto> =
        client.get("conversations/$conversationId/messages") {
            if (before != null) parameter("before", before)
            parameter("limit", limit)
        }.body()

    fun stream(conversationId: String, text: String): Flow<StreamEvent> = channelFlow {
        client.sse(
            urlString = "conversations/$conversationId/stream",
            request = {
                method = HttpMethod.Post
                contentType(ContentType.Application.Json)
                setBody(SendMessageDto(text))
            },
        ) {
            incoming.collect { event ->
                when (event.event) {
                    "token" -> send(StreamEvent.Token(event.data.orEmpty()))
                    "done" -> send(StreamEvent.Done)
                    "error" -> send(StreamEvent.Error(event.data.orEmpty()))
                }
            }
        }
    }
}

sealed interface StreamEvent {
    data class Token(val text: String) : StreamEvent
    data object Done : StreamEvent
    data class Error(val message: String) : StreamEvent
}
