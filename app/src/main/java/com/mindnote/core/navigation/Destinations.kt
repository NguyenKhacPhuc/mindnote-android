package com.mindnote.core.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object Routes {
    const val Onboarding = "onboarding"
    const val Home = "home"
    const val Chat = "chat/{conversationId}?text={text}"
    const val Notes = "notes"
    const val Capture = "capture"
    const val NoteDetail = "note/{noteId}"

    fun chat(id: String, text: String? = null): String {
        val base = "chat/$id"
        return if (text.isNullOrBlank()) base
        else "$base?text=${URLEncoder.encode(text, StandardCharsets.UTF_8.name())}"
    }

    fun noteDetail(id: String) = "note/$id"
}
