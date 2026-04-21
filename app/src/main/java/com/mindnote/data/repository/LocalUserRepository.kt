package com.mindnote.data.repository

import com.mindnote.core.storage.UserPrefs
import com.mindnote.data.db.MindNoteDatabase.Companion.LOCAL_USER_ID
import com.mindnote.data.db.dao.UserDao
import com.mindnote.data.db.entities.UserEntity
import com.mindnote.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(
    private val userPrefs: UserPrefs,
    private val userDao: UserDao,
) : UserRepository {

    override val username: Flow<String> = userPrefs.usernameFlow

    override suspend fun setUsername(name: String) {
        val trimmed = name.trim()
        userPrefs.setUsername(trimmed)
        userDao.upsert(UserEntity(id = LOCAL_USER_ID, username = trimmed))
    }

    override suspend fun markOnboarded() {
        userPrefs.setOnboarded(true)
    }
}
