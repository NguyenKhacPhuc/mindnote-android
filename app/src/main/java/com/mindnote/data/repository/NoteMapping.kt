package com.mindnote.data.repository

import com.mindnote.data.db.entities.NoteWithTopics
import com.mindnote.domain.model.Note

internal fun NoteWithTopics.toDomain(): Note = Note(
    id = note.id,
    title = note.title,
    preview = note.preview,
    body = note.body,
    tags = topics.map { it.name },
    date = note.date,
)
