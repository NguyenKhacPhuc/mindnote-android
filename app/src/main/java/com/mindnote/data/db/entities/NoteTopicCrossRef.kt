package com.mindnote.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "note_topics",
    primaryKeys = ["noteId", "topicName"],
    foreignKeys = [
        ForeignKey(
            entity = NoteEntity::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["name"],
            childColumns = ["topicName"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("topicName")],
)
data class NoteTopicCrossRef(
    val noteId: String,
    val topicName: String,
)
