package com.mindnote.domain.repository

import com.mindnote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    fun observeFavorites(): Flow<List<Note>>
    fun observeIsFavorite(noteId: String): Flow<Boolean>
    suspend fun toggle(noteId: String)
    suspend fun refresh()
}
