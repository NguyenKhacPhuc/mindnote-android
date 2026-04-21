package com.mindnote.domain.repository

import androidx.paging.PagingData
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import kotlinx.coroutines.flow.Flow

interface NotesRepository {
    fun notesPager(filter: NoteFilter, tag: String, query: String): Flow<PagingData<Note>>
    fun observeRecent(limit: Int): Flow<List<Note>>
    fun observeDistinctTags(): Flow<List<String>>
    fun observeCount(): Flow<Int>
    fun observeNote(id: String): Flow<Note?>
    suspend fun create(note: Note)
    suspend fun delete(id: String)

    suspend fun syncFirstPage(limit: Int = 30)
}
