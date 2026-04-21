package com.mindnote.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NotesApi(private val client: HttpClient) {

    suspend fun listNotes(offset: Int = 0, limit: Int = 30): List<NoteDto> =
        client.get("notes") {
            parameter("offset", offset)
            parameter("limit", limit)
        }.body()

    suspend fun getNote(id: String): NoteDto =
        client.get("notes/$id").body()

    suspend fun related(id: String): List<NoteDto> =
        client.get("notes/$id/related").body()

    suspend fun create(note: NoteCreateDto): NoteDto =
        client.post("notes") {
            contentType(ContentType.Application.Json)
            setBody(note)
        }.body()

    suspend fun deleteNote(id: String) {
        client.delete("notes/$id")
    }

    suspend fun favorites(): List<NoteDto> =
        client.get("favorites").body()

    suspend fun addFavorite(noteId: String) {
        client.post("favorites/$noteId")
    }

    suspend fun removeFavorite(noteId: String) {
        client.delete("favorites/$noteId")
    }
}
