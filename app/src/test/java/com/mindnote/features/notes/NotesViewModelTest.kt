package com.mindnote.features.notes

import com.mindnote.domain.model.NoteFilter
import com.mindnote.util.FakeFavoritesRepository
import com.mindnote.util.FakeNotesRepository
import com.mindnote.util.MainDispatcherRule
import com.mindnote.util.sampleNote
import java.time.LocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val notes = listOf(
        sampleNote("a", tags = listOf("work"), date = LocalDate.of(2026, 3, 3)),
        sampleNote("b", tags = listOf("reading"), date = LocalDate.of(2026, 3, 5)),
        sampleNote("c", tags = listOf("work", "hiring"), date = LocalDate.of(2026, 3, 1)),
    )

    @Test
    fun `tags list includes all-plus derived distinct sorted tags from data`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        assertEquals(listOf("all", "hiring", "reading", "work"), vm.state.value.tags)
    }

    @Test
    fun `initial state shows all notes sorted by date descending`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        assertEquals(listOf("b", "a", "c"), vm.state.value.notes.map { it.id })
    }

    @Test
    fun `selecting a tag filters the visible notes`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        vm.send(NotesIntent.SelectTag("work"))
        assertEquals(listOf("a", "c"), vm.state.value.notes.map { it.id })
        assertEquals("work", vm.state.value.activeTag)
    }

    @Test
    fun `switching filter to Favorites swaps data source`() = runTest {
        val favs = FakeFavoritesRepository(initial = setOf("b"))
        favs.setNotesSource(notes)

        val vm = NotesViewModel(FakeNotesRepository(notes), favs)
        vm.send(NotesIntent.SelectFilter(NoteFilter.Favorites))

        assertEquals(listOf("b"), vm.state.value.notes.map { it.id })
        assertEquals(NoteFilter.Favorites, vm.state.value.filter)
    }

    @Test
    fun `OpenNote intent emits NavigateToNote effect with the id`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        vm.send(NotesIntent.OpenNote("a"))
        val effect = vm.effects.first()
        assertTrue(effect is NotesEffect.NavigateToNote && effect.id == "a")
    }
}
