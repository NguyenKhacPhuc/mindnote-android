package com.mindnote.data.db.dao

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

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun count(): Int

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: String)

    @Transaction
    @Query("SELECT * FROM notes WHERE userId = :userId ORDER BY date DESC")
    fun observeAll(userId: String): Flow<List<NoteWithTopics>>

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :id")
    fun observe(id: String): Flow<NoteWithTopics?>
}
