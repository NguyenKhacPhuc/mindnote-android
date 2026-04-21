package com.mindnote.features.capture

import com.mindnote.util.FakeNotesRepository
import com.mindnote.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `save with content writes to repository and emits Saved effect`() = runTest {
        val repo = FakeNotesRepository()
        val vm = CaptureViewModel(repo)
        vm.send(CaptureIntent.UpdateTitle("Hello"))
        vm.send(CaptureIntent.UpdateBody("World\nSecond line"))
        vm.send(CaptureIntent.Save)

        val effect = vm.effects.first()
        assertTrue(effect is CaptureEffect.Saved)

        assertEquals(1, repo.created.size)
        val saved = repo.created[0]
        assertEquals("Hello", saved.title)
        assertEquals("World\nSecond line", saved.body)
        assertEquals("World", saved.preview)
    }

    @Test
    fun `save with blank title and body dismisses without writing`() = runTest {
        val repo = FakeNotesRepository()
        val vm = CaptureViewModel(repo)
        vm.send(CaptureIntent.Save)

        assertEquals(CaptureEffect.Dismiss, vm.effects.first())
        assertTrue(repo.created.isEmpty())
    }

    @Test
    fun `save with body only uses Untitled note as title`() = runTest {
        val repo = FakeNotesRepository()
        val vm = CaptureViewModel(repo)
        vm.send(CaptureIntent.UpdateBody("just a body"))
        vm.send(CaptureIntent.Save)
        vm.effects.first()
        assertEquals("Untitled note", repo.created.single().title)
    }

    @Test
    fun `cancel emits Dismiss without writing`() = runTest {
        val repo = FakeNotesRepository()
        val vm = CaptureViewModel(repo)
        vm.send(CaptureIntent.UpdateTitle("x"))
        vm.send(CaptureIntent.Cancel)

        assertEquals(CaptureEffect.Dismiss, vm.effects.first())
        assertTrue(repo.created.isEmpty())
    }
}
