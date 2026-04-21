package com.mindnote.data.repository

import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.dao.FavoriteDao
import com.mindnote.data.db.entities.FavoriteEntity
import com.mindnote.data.remote.NotesApi
import com.mindnote.domain.model.Note
import com.mindnote.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RoomFavoritesRepository(
    private val favoriteDao: FavoriteDao,
    private val api: NotesApi,
) : FavoritesRepository {

    override fun observeFavorites(): Flow<List<Note>> =
        favoriteDao.observeFavoriteNotes(LOCAL_USER_ID).map { list -> list.map { it.toDomain() } }

    override fun observeIsFavorite(noteId: String): Flow<Boolean> =
        favoriteDao.observeIsFavorite(LOCAL_USER_ID, noteId)

    override suspend fun toggle(noteId: String) {
        val isFav = favoriteDao.observeIsFavorite(LOCAL_USER_ID, noteId).first()
        if (isFav) {
            api.removeFavorite(noteId)
            favoriteDao.remove(LOCAL_USER_ID, noteId)
        } else {
            api.addFavorite(noteId)
            favoriteDao.add(FavoriteEntity(userId = LOCAL_USER_ID, noteId = noteId))
        }
    }

    override suspend fun refresh() {
        val remote = runCatching { api.favorites() }.getOrNull() ?: return
        favoriteDao.clearForUser(LOCAL_USER_ID)
        favoriteDao.addAll(remote.map { FavoriteEntity(userId = LOCAL_USER_ID, noteId = it.id) })
    }
}
