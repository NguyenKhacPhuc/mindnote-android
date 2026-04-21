package com.mindnote.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

interface UiState
interface UiIntent
interface UiEffect

abstract class MviViewModel<I : UiIntent, S : UiState, E : UiEffect>(initial: S) : ViewModel() {

    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    protected val currentState: S get() = _state.value

    fun send(intent: I) {
        viewModelScope.launch { handle(intent) }
    }

    protected abstract suspend fun handle(intent: I)

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected suspend fun emit(effect: E) {
        _effects.send(effect)
    }
}
