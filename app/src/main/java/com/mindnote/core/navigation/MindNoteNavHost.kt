package com.mindnote.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mindnote.features.capture.CaptureScreen
import com.mindnote.features.chat.ChatScreen
import com.mindnote.features.home.HomeScreen
import com.mindnote.features.notedetail.NoteDetailScreen
import com.mindnote.features.notes.NotesScreen
import com.mindnote.features.onboarding.OnboardingScreen

@Composable
fun MindNoteNavHost(startDestination: String = Routes.Onboarding) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = startDestination) {
        composable(Routes.Onboarding) {
            OnboardingScreen(onContinue = { nav.navigate(Routes.Home) })
        }
        composable(Routes.Home) {
            HomeScreen(
                onOpenChat = { id, text -> nav.navigate(Routes.chat(id, text)) },
                onOpenNotes = { nav.navigate(Routes.Notes) },
                onOpenCapture = { nav.navigate(Routes.Capture) },
                onOpenNote = { id -> nav.navigate(Routes.noteDetail(id)) },
            )
        }
        composable(
            route = Routes.Chat,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("text") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { backStack ->
            val id = backStack.arguments?.getString("conversationId").orEmpty()
            val initialText = backStack.arguments?.getString("text")?.let {
                java.net.URLDecoder.decode(it, java.nio.charset.StandardCharsets.UTF_8.name())
            }
            ChatScreen(
                conversationId = id,
                initialText = initialText,
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.Notes) {
            NotesScreen(
                onBack = { nav.popBackStack() },
                onOpenNote = { id -> nav.navigate(Routes.noteDetail(id)) },
                onOpenCapture = { nav.navigate(Routes.Capture) },
                onOpenChat = {
                    nav.navigate(Routes.Home) {
                        popUpTo(Routes.Home) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.Capture) {
            CaptureScreen(onDismiss = { nav.popBackStack() }, onSaved = { nav.popBackStack() })
        }
        composable(
            route = Routes.NoteDetail,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
        ) { backStack ->
            val id = backStack.arguments?.getString("noteId").orEmpty()
            NoteDetailScreen(
                noteId = id,
                onBack = { nav.popBackStack() },
            )
        }
    }
}
