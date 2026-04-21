package com.mindnote.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindnote.data.db.entities.NoteTopicCrossRef
import com.mindnote.data.db.entities.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTopics(topics: List<TopicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<NoteTopicCrossRef>)

    @Query("DELETE FROM note_topics WHERE noteId = :noteId")
    suspend fun clearForNote(noteId: String)

    @Query("SELECT * FROM topics ORDER BY name")
    fun observeAll(): Flow<List<TopicEntity>>
}
