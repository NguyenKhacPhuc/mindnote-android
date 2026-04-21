package com.mindnote.data.db.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mindnote.data.db.entities.NoteEntity
import com.mindnote.data.db.entities.NoteWithTopics
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    suspend fun count(userId: String): Int

    @Query("SELECT COUNT(*) FROM notes WHERE userId = :userId")
    fun observeCount(userId: String): Flow<Int>

    @Query("DELETE FROM notes WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun observe(id: String): Flow<NoteWithTopics?>

    @Transaction
    @Query(
        """
        SELECT * FROM notes
        WHERE userId = :userId
        ORDER BY date DESC, id DESC
        LIMIT :limit
        """
    )
    fun observeRecent(userId: String, limit: Int): Flow<List<NoteWithTopics>>

    /**
     * Distinct tag list across the user's notes. Returns topic names actually in use.
     */
    @Query(
        """
        SELECT DISTINCT ct.topicName FROM note_topics ct
        INNER JOIN notes n ON n.id = ct.noteId
        WHERE n.userId = :userId
        ORDER BY ct.topicName ASC
        """
    )
    fun observeDistinctTags(userId: String): Flow<List<String>>

    /**
     * Single paged query that applies all notes-screen filters in SQL.
     *
     * - [favoritesOnly] = 1 joins favorites and keeps only matching notes.
     * - [tag] = "all" disables the tag filter; otherwise keeps notes tagged with [tag].
     * - [query] = "" disables search; otherwise LIKE against title/preview/body.
     */
    @Transaction
    @Query(
        """
        SELECT n.* FROM notes n
        WHERE n.userId = :userId
          AND (:favoritesOnly = 0 OR EXISTS (
                SELECT 1 FROM favorites f
                WHERE f.noteId = n.id AND f.userId = n.userId
          ))
          AND (:tag = 'all' OR EXISTS (
                SELECT 1 FROM note_topics ct
                WHERE ct.noteId = n.id AND ct.topicName = :tag
          ))
          AND (:query = ''
                OR n.title LIKE '%' || :query || '%'
                OR n.preview LIKE '%' || :query || '%'
                OR n.body LIKE '%' || :query || '%')
        ORDER BY n.date DESC, n.id DESC
        """
    )
    fun pagingFiltered(
        userId: String,
        favoritesOnly: Int,
        tag: String,
        query: String,
    ): PagingSource<Int, NoteWithTopics>
}
