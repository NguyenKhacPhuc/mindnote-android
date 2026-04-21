package com.mindnote.data.remote

import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.domain.model.Note
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class DtoMappingTest {

    @Test
    fun `NoteDto toEntity copies fields and assigns local user`() {
        val dto = NoteDto(
            id = "n4",
            title = "Pricing brainstorm",
            preview = "anchored on the middle",
            body = "body text",
            tags = listOf("work", "strategy"),
            date = "2026-03-08",
        )

        val entity = dto.toEntity()

        assertEquals("n4", entity.id)
        assertEquals(LOCAL_USER_ID, entity.userId)
        assertEquals("Pricing brainstorm", entity.title)
        assertEquals(LocalDate.of(2026, 3, 8), entity.date)
    }

    @Test
    fun `topicEntities and crossRefs mirror the tag list`() {
        val dto = NoteDto(
            id = "x",
            title = "t",
            preview = "",
            body = "",
            tags = listOf("work", "hiring"),
            date = "2026-01-01",
        )

        assertEquals(listOf("work", "hiring"), dto.topicEntities().map { it.name })
        assertEquals(
            listOf("x-work", "x-hiring"),
            dto.crossRefs().map { "${it.noteId}-${it.topicName}" },
        )
    }

    @Test
    fun `Note toCreateDto serializes date as ISO string`() {
        val note = Note(
            id = "n",
            title = "hi",
            preview = "p",
            body = "b",
            tags = listOf("t1"),
            date = LocalDate.of(2026, 4, 5),
        )
        val dto = note.toCreateDto()

        assertEquals("n", dto.id)
        assertEquals("hi", dto.title)
        assertEquals("b", dto.body)
        assertEquals(listOf("t1"), dto.tags)
        assertEquals("2026-04-05", dto.date)
    }
}
