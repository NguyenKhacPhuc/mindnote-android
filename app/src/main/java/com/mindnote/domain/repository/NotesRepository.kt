package com.mindnote.domain.repository

import com.mindnote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    val notes: Flow<List<Note>>
    fun observeNote(id: String): Flow<Note?>
    suspend fun create(note: Note)
    suspend fun delete(id: String)
    suspend fun refresh()
}
