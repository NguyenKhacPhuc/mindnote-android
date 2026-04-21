package com.mindnote.features.capture

import com.mindnote.core.mvi.UiEffect
import com.mindnote.core.mvi.UiIntent
import com.mindnote.core.mvi.UiState

sealed interface CaptureIntent : UiIntent {
    data class UpdateTitle(val text: String) : CaptureIntent
    data class UpdateBody(val text: String) : CaptureIntent
    data object AddTag : CaptureIntent
    data class UpdateTagInput(val text: String) : CaptureIntent
    data object ConfirmTag : CaptureIntent
    data object CancelTag : CaptureIntent
    data class RemoveTag(val tag: String) : CaptureIntent
    data object AttachLink : CaptureIntent
    data object Cancel : CaptureIntent
    data object Save : CaptureIntent
}

data class CaptureState(
    val title: String,
    val body: String,
    val dateLabel: String,
    val tags: List<String>,
    val tagInput: String? = null,
    val isSaving: Boolean = false,
) : UiState

sealed interface CaptureEffect : UiEffect {
    data object Dismiss : CaptureEffect
    data class Saved(val noteId: String) : CaptureEffect
    data class ShowError(val message: String) : CaptureEffect
}
