package com.mindnote.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class NoteDto(
    val id: String,
    val title: String,
    val preview: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val date: String,
)

@Serializable
data class NoteCreateDto(
    val id: String? = null,
    val title: String,
    val body: String,
    val preview: String = "",
    val tags: List<String> = emptyList(),
    val date: String? = null,
)
