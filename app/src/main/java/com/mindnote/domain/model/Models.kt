package com.mindnote.domain.model

import java.time.LocalDate

data class Note(
    val id: String,
    val title: String,
    val preview: String,
    val body: String,
    val tags: List<String>,
    val date: LocalDate,
)

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val isThinking: Boolean = false,
) {
    enum class Role { User, Assistant }
}

data class SuggestedPrompt(val id: String, val text: String)

data class OnboardingOption(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String,
)

enum class NoteFilter(val label: String) { All("All"), Favorites("Favorites") }
