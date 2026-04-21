package com.mindnote.core.di

import android.util.Log
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindnote.BuildConfig
import com.mindnote.core.storage.UserPrefs
import com.mindnote.data.db.MindNoteDatabase
import com.mindnote.data.remote.ChatApi
import com.mindnote.data.remote.NotesApi
import com.mindnote.data.repository.LocalUserRepository
import com.mindnote.data.repository.RoomFavoritesRepository
import com.mindnote.data.repository.RoomNotesRepository
import com.mindnote.domain.repository.FavoritesRepository
import com.mindnote.domain.repository.NotesRepository
import com.mindnote.domain.repository.UserRepository
import com.mindnote.features.capture.CaptureViewModel
import com.mindnote.features.chat.ChatViewModel
import com.mindnote.features.home.HomeViewModel
import com.mindnote.features.notedetail.NoteDetailViewModel
import com.mindnote.features.notes.NotesViewModel
import com.mindnote.features.onboarding.OnboardingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { UserPrefs(androidContext()) }

    single {
        Room.databaseBuilder(androidContext(), MindNoteDatabase::class.java, MindNoteDatabase.DB_NAME)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    db.execSQL(
                        "INSERT OR IGNORE INTO users (id, username) VALUES (?, ?)",
                        arrayOf(MindNoteDatabase.LOCAL_USER_ID, ""),
                    )
                }
            })
            .fallbackToDestructiveMigration()
            .build()
    }
    single { get<MindNoteDatabase>().userDao() }
    single { get<MindNoteDatabase>().noteDao() }
    single { get<MindNoteDatabase>().topicDao() }
    single { get<MindNoteDatabase>().favoriteDao() }

    single {
        val deviceId = get<UserPrefs>().deviceIdBlocking()
        Log.i("MindNote", "Device ID: $deviceId")
        HttpClient(OkHttp) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
            install(HttpTimeout) {
                connectTimeoutMillis = 15_000
                requestTimeoutMillis = 30_000
            }
            install(Logging) {
                level = if (BuildConfig.DEBUG) LogLevel.INFO else LogLevel.NONE
            }
            install(SSE)
            defaultRequest {
                url(BuildConfig.BASE_URL)
                contentType(ContentType.Application.Json)
                header("X-Device-Id", deviceId)
            }
        }
    }
    single { NotesApi(get()) }
    single { ChatApi(get()) }

    single<NotesRepository> { RoomNotesRepository(get(), get(), get(), get()) }
    single<FavoritesRepository> { RoomFavoritesRepository(get(), get()) }
    single<UserRepository> { LocalUserRepository(get(), get()) }

    viewModel { OnboardingViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { NotesViewModel(get(), get()) }
    viewModel { CaptureViewModel(get()) }
    viewModel { (conversationId: String) -> ChatViewModel(conversationId, get()) }
    viewModel { (noteId: String) -> NoteDetailViewModel(noteId, get(), get()) }
}
