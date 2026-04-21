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

    // Filter/tag/query results live in SQL (NoteDao.pagingFiltered) + PagingSource, so
    // the notes-order/filter behavior is covered by DAO/integration tests rather than here.
    // These unit tests cover the state-machine slice of the ViewModel.

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
    fun `SelectTag updates activeTag`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        vm.send(NotesIntent.SelectTag("work"))
        assertEquals("work", vm.state.value.activeTag)
    }

    @Test
    fun `SelectFilter updates filter`() = runTest {
        val vm = NotesViewModel(FakeNotesRepository(notes), FakeFavoritesRepository())
        vm.send(NotesIntent.SelectFilter(NoteFilter.Favorites))
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
