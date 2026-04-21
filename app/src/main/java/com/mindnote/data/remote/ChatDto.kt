package com.mindnote.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val role: String, // "user" | "assistant"
    val content: String,
    val createdAt: Long,
)

@Serializable
data class SendMessageDto(val text: String)
