package com.mindnote.data.remote

import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.entities.NoteEntity
import com.mindnote.data.db.entities.NoteTopicCrossRef
import com.mindnote.data.db.entities.TopicEntity
import com.mindnote.domain.model.Note
import java.time.LocalDate

internal fun NoteDto.toEntity(): NoteEntity = NoteEntity(
    id = id,
    userId = LOCAL_USER_ID,
    title = title,
    preview = preview,
    body = body,
    date = LocalDate.parse(date),
)

internal fun NoteDto.topicEntities(): List<TopicEntity> = tags.map(::TopicEntity)

internal fun NoteDto.crossRefs(): List<NoteTopicCrossRef> =
    tags.map { NoteTopicCrossRef(noteId = id, topicName = it) }

internal fun Note.toCreateDto(): NoteCreateDto = NoteCreateDto(
    id = id,
    title = title,
    body = body,
    preview = preview,
    tags = tags,
    date = date.toString(),
)
