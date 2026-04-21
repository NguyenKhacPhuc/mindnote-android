package com.mindnote.util

import androidx.paging.PagingData
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import com.mindnote.domain.repository.FavoritesRepository
import com.mindnote.domain.repository.NotesRepository
import com.mindnote.domain.repository.UserRepository
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

fun sampleNote(
    id: String = "n1",
    title: String = id,
    tags: List<String> = emptyList(),
    date: LocalDate = LocalDate.of(2026, 3, 1),
): Note = Note(
    id = id,
    title = title,
    preview = "",
    body = "",
    tags = tags,
    date = date,
)

class FakeNotesRepository(initial: List<Note> = emptyList()) : NotesRepository {
    private val _notes = MutableStateFlow(initial)
    val created: MutableList<Note> = mutableListOf()
    val deleted: MutableList<String> = mutableListOf()
    var syncCount = 0

    /** When non-null, [create] / [delete] / [syncFirstPage] will throw this. */
    var nextFailure: Throwable? = null

    override fun notesPager(filter: NoteFilter, tag: String, query: String): Flow<PagingData<Note>> =
        _notes.map { list -> PagingData.from(list.sortedByDescending { it.date }) }

    override fun observeRecent(limit: Int): Flow<List<Note>> =
        _notes.map { list -> list.sortedByDescending { it.date }.take(limit) }

    override fun observeDistinctTags(): Flow<List<String>> =
        _notes.map { list -> list.flatMap { it.tags }.distinct().sorted() }

    override fun observeCount(): Flow<Int> = _notes.map { it.size }

    override fun observeNote(id: String): Flow<Note?> =
        _notes.map { list -> list.firstOrNull { it.id == id } }.distinctUntilChanged()

    override suspend fun create(note: Note) {
        nextFailure?.let { nextFailure = null; throw it }
        created += note
        _notes.update { listOf(note) + it }
    }

    override suspend fun delete(id: String) {
        nextFailure?.let { nextFailure = null; throw it }
        deleted += id
        _notes.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun syncFirstPage(limit: Int) {
        nextFailure?.let { nextFailure = null; throw it }
        syncCount++
    }

    fun setNotes(list: List<Note>) {
        _notes.value = list
    }
}

class FakeFavoritesRepository(initial: Set<String> = emptySet()) : FavoritesRepository {
    private val favoriteIds = MutableStateFlow(initial)
    private val notesSource = MutableStateFlow<List<Note>>(emptyList())
    var refreshCount = 0

    fun setNotesSource(notes: List<Note>) {
        notesSource.value = notes
    }

    override fun observeFavorites(): Flow<List<Note>> =
        combine(favoriteIds, notesSource) { ids, list -> list.filter { it.id in ids } }

    override fun observeIsFavorite(noteId: String): Flow<Boolean> =
        favoriteIds.map { noteId in it }.distinctUntilChanged()

    override suspend fun toggle(noteId: String) {
        favoriteIds.update { if (noteId in it) it - noteId else it + noteId }
    }

    override suspend fun refresh() {
        refreshCount++
    }
}

class FakeUserRepository(initial: String = "") : UserRepository {
    private val _username = MutableStateFlow(initial)
    var savedName: String? = null

    override val username: Flow<String> = _username.asStateFlow()

    override suspend fun setUsername(name: String) {
        savedName = name.trim()
        _username.value = name.trim()
    }

    override suspend fun markOnboarded() = Unit
}
