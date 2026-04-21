package com.mindnote.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.mindnote.data.db.entities.FavoriteEntity
import com.mindnote.data.db.entities.NoteWithTopics
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND noteId = :noteId")
    suspend fun remove(userId: String, noteId: String)

    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun clearForUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addAll(favorites: List<FavoriteEntity>)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND noteId = :noteId)")
    fun observeIsFavorite(userId: String, noteId: String): Flow<Boolean>

    @Transaction
    @Query(
        "SELECT n.* FROM notes n " +
            "INNER JOIN favorites f ON f.noteId = n.id " +
            "WHERE f.userId = :userId " +
            "ORDER BY n.date DESC"
    )
    fun observeFavoriteNotes(userId: String): Flow<List<NoteWithTopics>>
}
