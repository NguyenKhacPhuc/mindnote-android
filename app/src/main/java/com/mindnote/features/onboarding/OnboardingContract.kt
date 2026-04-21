package com.mindnote.features.onboarding

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState
import com.mindnote.domain.model.OnboardingOption

sealed interface OnboardingIntent : UiIntent {
    data class SelectOption(val id: String) : OnboardingIntent
    data class UpdateUsername(val name: String) : OnboardingIntent
    data object Continue : OnboardingIntent
    data object Back : OnboardingIntent
    data object Skip : OnboardingIntent
}

data class OnboardingState(
    val options: List<OnboardingOption>,
    val selectedId: String?,
    val step: Int = 0,
    val username: String = "",
) : UiState

sealed interface OnboardingEffect : UiEffect {
    data object NavigateNext : OnboardingEffect
}
