package com.mindnote.features.onboarding

import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.model.OnboardingOption
import com.mindnote.domain.repository.UserRepository

private val defaultOptions = listOf(
    OnboardingOption(
        id = "work",
        title = "Work & meetings",
        description = "capture meetings, projects, and decisions",
        iconKey = "briefcase"
    ),
    OnboardingOption(
        id = "research",
        title = "Research & learning",
        description = "study notes, papers, and ideas",
        iconKey = "graduation"
    ),
    OnboardingOption(
        id = "creative",
        title = "Creative work",
        description = "drafts, fragments, and inspiration",
        iconKey = "pen"
    ),
    OnboardingOption(
        id = "mixed",
        title = "A little of everything",
        description = "I'll figure it out as I go",
        iconKey = "sparkles"
    )
)

class OnboardingViewModel(
    private val userRepository: UserRepository,
) : MviViewModel<OnboardingIntent, OnboardingState, OnboardingEffect>(
    OnboardingState(options = defaultOptions, selectedId = "research")
) {
    override suspend fun handle(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.SelectOption -> setState { copy(selectedId = intent.id) }
            is OnboardingIntent.UpdateUsername -> setState { copy(username = intent.name) }
            OnboardingIntent.Continue -> {
                if (currentState.step == 0) {
                    setState { copy(step = 1) }
                } else {
                    userRepository.setUsername(currentState.username)
                    userRepository.markOnboarded()
                    emit(OnboardingEffect.NavigateNext)
                }
            }
            OnboardingIntent.Back -> {
                if (currentState.step > 0) setState { copy(step = step - 1) }
            }
            OnboardingIntent.Skip -> {
                userRepository.markOnboarded()
                emit(OnboardingEffect.NavigateNext)
            }
        }
    }
}
