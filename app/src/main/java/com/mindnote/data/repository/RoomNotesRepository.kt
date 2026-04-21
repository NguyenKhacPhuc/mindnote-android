package com.mindnote.data.repository

import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.dao.NoteDao
import com.mindnote.data.db.dao.TopicDao
import com.mindnote.data.remote.NotesApi
import com.mindnote.data.remote.crossRefs
import com.mindnote.data.remote.toCreateDto
import com.mindnote.data.remote.toEntity
import com.mindnote.data.remote.topicEntities
import com.mindnote.domain.model.Note
import com.mindnote.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class RoomNotesRepository(
    private val noteDao: NoteDao,
    private val topicDao: TopicDao,
    private val api: NotesApi,
) : NotesRepository {

    override val notes: Flow<List<Note>> =
        noteDao.observeAll(LOCAL_USER_ID).map { list -> list.map { it.toDomain() } }

    override fun observeNote(id: String): Flow<Note?> =
        noteDao.observe(id).map { it?.toDomain() }.distinctUntilChanged()

    override suspend fun create(note: Note) {
        val saved = api.create(note.toCreateDto())
        noteDao.insert(saved.toEntity())
        if (saved.tags.isNotEmpty()) {
            topicDao.insertTopics(saved.topicEntities())
            topicDao.clearForNote(saved.id)
            topicDao.insertCrossRefs(saved.crossRefs())
        }
    }

    override suspend fun delete(id: String) {
        api.deleteNote(id)
        noteDao.deleteById(id)
    }

    override suspend fun refresh() {
        val remote = runCatching { api.listNotes() }.getOrNull() ?: return
        noteDao.insertAll(remote.map { it.toEntity() })
        remote.forEach { dto ->
            if (dto.tags.isNotEmpty()) {
                topicDao.insertTopics(dto.topicEntities())
                topicDao.clearForNote(dto.id)
                topicDao.insertCrossRefs(dto.crossRefs())
            }
        }
    }
}
