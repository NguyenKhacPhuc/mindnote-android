package com.mindnote.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mindnote.data.db.dao.FavoriteDao
import com.mindnote.data.db.dao.NoteDao
import com.mindnote.data.db.dao.TopicDao
import com.mindnote.data.db.dao.UserDao
import com.mindnote.data.db.entities.FavoriteEntity
import com.mindnote.data.db.entities.NoteEntity
import com.mindnote.data.db.entities.NoteTopicCrossRef
import com.mindnote.data.db.entities.TopicEntity
import com.mindnote.data.db.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        NoteEntity::class,
        TopicEntity::class,
        NoteTopicCrossRef::class,
        FavoriteEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class MindNoteDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
    abstract fun topicDao(): TopicDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DB_NAME = "mindnote.db"
        const val LOCAL_USER_ID = "local"
    }
}
