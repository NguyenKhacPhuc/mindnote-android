package com.mindnote.features.capture

import com.mindnote.core.ext.Result
import com.mindnote.core.ext.safeApiCall
import com.mindnote.core.ext.userMessage
import com.mindnote.core.mvi.MviViewModel
import com.mindnote.domain.model.Note
import com.mindnote.domain.repository.NotesRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CaptureViewModel(
    private val notesRepository: NotesRepository,
) : MviViewModel<CaptureIntent, CaptureState, CaptureEffect>(
    initial = CaptureState(
        title = "",
        body = "",
        dateLabel = LocalDate.now().format(dateLabelFormat).uppercase(),
        tags = emptyList(),
    ),
) {
    override suspend fun handle(intent: CaptureIntent) {
        when (intent) {
            is CaptureIntent.UpdateTitle -> setState { copy(title = intent.text) }
            is CaptureIntent.UpdateBody -> setState { copy(body = intent.text) }
            CaptureIntent.AddTag -> setState { copy(tagInput = "") }
            is CaptureIntent.UpdateTagInput -> setState { copy(tagInput = intent.text) }
            CaptureIntent.ConfirmTag -> setState {
                val input = tagInput?.trim().orEmpty()
                if (input.isBlank() || input in tags) copy(tagInput = null)
                else copy(tags = tags + input, tagInput = null)
            }

            CaptureIntent.CancelTag -> setState { copy(tagInput = null) }
            is CaptureIntent.RemoveTag -> setState { copy(tags = tags - intent.tag) }
            CaptureIntent.AttachLink -> Unit
            CaptureIntent.Cancel -> emit(CaptureEffect.Dismiss)
            CaptureIntent.Save -> {
                val s = currentState
                if (s.title.isBlank() && s.body.isBlank()) {
                    emit(CaptureEffect.Dismiss)
                    return
                }
                if (s.isSaving) return
                setState { copy(isSaving = true) }
                val id = "n-" + System.currentTimeMillis()
                val note = Note(
                    id = id,
                    title = s.title.ifBlank { "Untitled note" },
                    preview = s.body.lineSequence().firstOrNull().orEmpty().take(120),
                    body = s.body,
                    tags = s.tags,
                    date = LocalDate.now(),
                )
                val result = safeApiCall { notesRepository.create(note) }
                setState { copy(isSaving = false) }
                when (result) {
                    is Result.Success -> emit(CaptureEffect.Saved(id))
                    is Result.Error -> emit(CaptureEffect.ShowError(result.userMessage("Couldn't save note")))
                    Result.Loading -> Unit
                }
            }
        }
    }

    private companion object {
        val dateLabelFormat: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
    }
}
