package com.mindnote.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.mindnote.data.db.MindNoteDatabase
import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.dao.NoteDao
import com.mindnote.data.db.dao.TopicDao
import com.mindnote.data.db.entities.NoteWithTopics
import com.mindnote.data.remote.NotesApi
import com.mindnote.data.remote.crossRefs
import com.mindnote.data.remote.toEntity
import com.mindnote.data.remote.topicEntities
import io.ktor.client.plugins.ResponseException
import java.io.IOException

/**
 * Paginates the full notes dataset from the server into Room. Filter/tag/query are applied
 * locally by [NoteDao.pagingFiltered] on whatever is already cached — so search only finds
 * what's been synced. Over time that grows to everything.
 *
 * Offset-based. REFRESH resets to offset 0 and truncates the local cache; APPEND continues.
 * End-of-pagination is detected when the server returns fewer rows than [PAGE_SIZE].
 */
@OptIn(ExperimentalPagingApi::class)
class NotesRemoteMediator(
    private val db: MindNoteDatabase,
    private val noteDao: NoteDao,
    private val topicDao: TopicDao,
    private val api: NotesApi,
) : RemoteMediator<Int, NoteWithTopics>() {

    private var nextOffset: Int = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, NoteWithTopics>,
    ): MediatorResult {
        val offset = when (loadType) {
            LoadType.REFRESH -> 0
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> nextOffset
        }

        return try {
            val page = api.listNotes(offset = offset, limit = PAGE_SIZE)

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    noteDao.clearForUser(LOCAL_USER_ID)
                }
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

            nextOffset = offset + page.size
            MediatorResult.Success(endOfPaginationReached = page.size < PAGE_SIZE)
        } catch (e: IOException) {
            MediatorResult.Error(e)
        } catch (e: ResponseException) {
            MediatorResult.Error(e)
        }
    }

    companion object {
        const val PAGE_SIZE = 30
    }
}
