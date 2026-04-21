package com.mindnote.features.home

import com.mindnote.util.FakeNotesRepository
import com.mindnote.util.FakeUserRepository
import com.mindnote.util.MainDispatcherRule
import com.mindnote.util.sampleNote
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `greetingFor appends name when provided`() {
        assertEquals("Good morning, Phuc", greetingFor("Phuc", LocalTime.of(8, 0)))
        assertEquals("Good afternoon, Phuc", greetingFor("Phuc", LocalTime.of(14, 0)))
        assertEquals("Good evening, Phuc", greetingFor("Phuc", LocalTime.of(21, 0)))
    }

    @Test
    fun `greetingFor omits name when blank`() {
        assertEquals("Good morning", greetingFor("", LocalTime.of(6, 0)))
        assertEquals("Good afternoon", greetingFor("   ", LocalTime.of(13, 0)))
    }

    @Test
    fun `greetingFor picks evening bucket for midnight through early morning`() {
        assertEquals("Good evening", greetingFor("", LocalTime.of(0, 30)))
        assertEquals("Good evening", greetingFor("", LocalTime.of(4, 59)))
        assertEquals("Good morning", greetingFor("", LocalTime.of(5, 0)))
    }

    @Test
    fun `recents contains 3 most-recent notes and summary counts all`() = runTest {
        val notes = listOf(
            sampleNote("a", date = LocalDate.of(2026, 3, 1)),
            sampleNote("b", date = LocalDate.of(2026, 3, 5)),
            sampleNote("c", date = LocalDate.of(2026, 3, 3)),
            sampleNote("d", date = LocalDate.of(2026, 3, 7)),
        )
        val vm = HomeViewModel(FakeUserRepository(), FakeNotesRepository(notes))

        val state = vm.state.value
        assertEquals(listOf("d", "b", "c"), state.recents.map { it.id })
        assertTrue(state.summary.contains("4 notes"))
    }

    @Test
    fun `greeting updates when username flow emits`() = runTest {
        val user = FakeUserRepository()
        val vm = HomeViewModel(user, FakeNotesRepository())
        val blankGreeting = vm.state.value.greeting
        assertTrue(blankGreeting.startsWith("Good ") && !blankGreeting.contains(","))

        user.setUsername("Phuc")

        val named = vm.state.value.greeting
        assertTrue(named.endsWith(", Phuc"))
    }
}
