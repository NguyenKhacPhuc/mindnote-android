package com.mindnote.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.mindnote.data.db.MindNoteDatabase
import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.dao.NoteDao
import com.mindnote.data.db.dao.TopicDao
import com.mindnote.data.remote.NotesApi
import com.mindnote.data.remote.crossRefs
import com.mindnote.data.remote.toCreateDto
import com.mindnote.data.remote.toEntity
import com.mindnote.data.remote.topicEntities
import com.mindnote.domain.model.Note
import com.mindnote.domain.model.NoteFilter
import androidx.room.withTransaction
import com.mindnote.domain.repository.NotesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
class RoomNotesRepository(
    private val db: MindNoteDatabase,
    private val noteDao: NoteDao,
    private val topicDao: TopicDao,
    private val api: NotesApi,
) : NotesRepository {

    override fun notesPager(
        filter: NoteFilter,
        tag: String,
        query: String,
    ): Flow<PagingData<Note>> {
        val favoritesOnly = if (filter == NoteFilter.Favorites) 1 else 0
        return Pager(
            config = PagingConfig(
                pageSize = NotesRemoteMediator.PAGE_SIZE,
                enablePlaceholders = false,
            ),
            remoteMediator = NotesRemoteMediator(db, noteDao, topicDao, api),
            pagingSourceFactory = {
                noteDao.pagingFiltered(
                    userId = LOCAL_USER_ID,
                    favoritesOnly = favoritesOnly,
                    tag = tag,
                    query = query,
                )
            },
        ).flow.map { data -> data.map { it.toDomain() } }
    }

    override fun observeRecent(limit: Int): Flow<List<Note>> =
        noteDao.observeRecent(LOCAL_USER_ID, limit)
            .map { list -> list.map { it.toDomain() } }

    override fun observeDistinctTags(): Flow<List<String>> =
        noteDao.observeDistinctTags(LOCAL_USER_ID)

    override fun observeCount(): Flow<Int> =
        noteDao.observeCount(LOCAL_USER_ID).distinctUntilChanged()

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

    override suspend fun syncFirstPage(limit: Int) {
        val page = runCatching { api.listNotes(offset = 0, limit = limit) }.getOrNull() ?: return
        db.withTransaction {
            noteDao.clearForUser(LOCAL_USER_ID)
            if (page.isNotEmpty()) {
                noteDao.insertAll(page.map { it.toEntity() })
                val topics = page.flatMap { it.topicEntities() }.distinctBy { it.name }
                if (topics.isNotEmpty()) topicDao.insertTopics(topics)
                page.forEach { dto ->
                    topicDao.clearForNote(dto.id)
                    if (dto.tags.isNotEmpty()) topicDao.insertCrossRefs(dto.crossRefs())
                }
            }
        }
    }
}
