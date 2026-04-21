package com.mindnote.features.notedetail

import com.mindnote.util.FakeFavoritesRepository
import com.mindnote.util.FakeNotesRepository
import com.mindnote.util.MainDispatcherRule
import com.mindnote.util.sampleNote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NoteDetailViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `loads note and isFavorite from repositories`() = runTest {
        val note = sampleNote("p")
        val notes = FakeNotesRepository(listOf(note))
        val favs = FakeFavoritesRepository(initial = setOf("p"))

        val vm = NoteDetailViewModel("p", notes, favs)

        val s = vm.state.value
        assertEquals("p", s.note?.id)
        assertTrue(s.isFavorite)
        assertFalse(s.isLoading)
    }

    @Test
    fun `missing note produces null note`() = runTest {
        val vm = NoteDetailViewModel(
            noteId = "missing",
            notesRepository = FakeNotesRepository(),
            favoritesRepository = FakeFavoritesRepository(),
        )
        assertNull(vm.state.value.note)
    }

    @Test
    fun `ToggleFavorite flips the stored favorite`() = runTest {
        val favs = FakeFavoritesRepository()
        val vm = NoteDetailViewModel("p", FakeNotesRepository(listOf(sampleNote("p"))), favs)

        assertFalse(vm.state.value.isFavorite)
        vm.send(NoteDetailIntent.ToggleFavorite)
        assertTrue(vm.state.value.isFavorite)
        vm.send(NoteDetailIntent.ToggleFavorite)
        assertFalse(vm.state.value.isFavorite)
    }
}
