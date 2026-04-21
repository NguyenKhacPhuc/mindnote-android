package com.mindnote.features.onboarding

import com.mindnote.util.FakeUserRepository
import com.mindnote.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun `default state starts at step 0 with research preselected`() {
        val vm = OnboardingViewModel(FakeUserRepository())
        val s = vm.state.value
        assertEquals(0, s.step)
        assertEquals("research", s.selectedId)
    }

    @Test
    fun `selecting an option updates selectedId`() = runTest {
        val vm = OnboardingViewModel(FakeUserRepository())
        vm.send(OnboardingIntent.SelectOption("creative"))
        assertEquals("creative", vm.state.value.selectedId)
    }

    @Test
    fun `continue from step 0 advances to step 1 without persisting`() = runTest {
        val user = FakeUserRepository()
        val vm = OnboardingViewModel(user)
        vm.send(OnboardingIntent.Continue)
        assertEquals(1, vm.state.value.step)
        assertNull(user.savedName)
    }

    @Test
    fun `continue from step 1 persists username and emits NavigateNext`() = runTest {
        val user = FakeUserRepository()
        val vm = OnboardingViewModel(user)
        vm.send(OnboardingIntent.Continue)                     // step 0 -> 1
        vm.send(OnboardingIntent.UpdateUsername("  Phuc  "))
        vm.send(OnboardingIntent.Continue)                     // step 1 -> nav

        assertEquals("Phuc", user.savedName)
        assertEquals(OnboardingEffect.NavigateNext, vm.effects.first())
    }

    @Test
    fun `back from step 1 returns to step 0`() = runTest {
        val vm = OnboardingViewModel(FakeUserRepository())
        vm.send(OnboardingIntent.Continue)
        vm.send(OnboardingIntent.Back)
        assertEquals(0, vm.state.value.step)
    }

    @Test
    fun `skip emits NavigateNext without saving username`() = runTest {
        val user = FakeUserRepository()
        val vm = OnboardingViewModel(user)
        vm.send(OnboardingIntent.Skip)

        assertEquals(OnboardingEffect.NavigateNext, vm.effects.first())
        assertNull(user.savedName)
    }
}
