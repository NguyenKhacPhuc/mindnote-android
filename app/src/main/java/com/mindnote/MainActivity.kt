package com.mindnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mindnote.core.navigation.MindNoteNavHost
import com.mindnote.core.navigation.Routes
import com.mindnote.core.storage.UserPrefs
import com.mindnote.design.MindNoteTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val userPrefs: UserPrefs by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination = if (userPrefs.isOnboardedBlocking()) Routes.Home else Routes.Onboarding
        enableEdgeToEdge()
        setContent {
            MindNoteTheme { MindNoteNavHost(startDestination = startDestination) }
        }
    }
}
