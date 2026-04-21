package com.mindnote

import android.app.Application
import com.mindnote.core.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MindNoteApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MindNoteApp)
            modules(appModule)
        }
    }
}
