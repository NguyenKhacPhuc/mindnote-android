package com.mindnote.domain.repository

import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val username: Flow<String>
    suspend fun setUsername(name: String)
    suspend fun markOnboarded()
}
