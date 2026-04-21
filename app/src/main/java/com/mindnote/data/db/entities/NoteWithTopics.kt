package com.mindnote.data.db.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class NoteWithTopics(
    @Embedded val note: NoteEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "name",
        associateBy = Junction(
            value = NoteTopicCrossRef::class,
            parentColumn = "noteId",
            entityColumn = "topicName",
        ),
    )
    val topics: List<TopicEntity>,
)
