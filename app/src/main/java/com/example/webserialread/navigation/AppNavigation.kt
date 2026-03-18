package com.example.webserialread.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.webserialread.ui.screen.ChapterListScreen
import com.example.webserialread.ui.screen.MainScreen
import com.example.webserialread.ui.screen.ReaderScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {

        composable("main") {
            MainScreen(
                onSerialClick = { serialId -> navController.navigate("chapters/$serialId") },
                onChapterClick = { chapterId -> navController.navigate("reader/$chapterId") }
            )
        }

        composable(
            route = "chapters/{serialId}",
            arguments = listOf(navArgument("serialId") { type = NavType.LongType })
        ) { back ->
            ChapterListScreen(
                serialId = back.arguments!!.getLong("serialId"),
                onChapterClick = { navController.navigate("reader/$it") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "reader/{chapterId}",
            arguments = listOf(navArgument("chapterId") { type = NavType.LongType })
        ) { back ->
            val chapterId = back.arguments!!.getLong("chapterId")
            ReaderScreen(
                chapterId = chapterId,
                onBack = { navController.popBackStack() },
                onNavigateToChapter = { newId ->
                    navController.navigate("reader/$newId") {
                        popUpTo("reader/$chapterId") { inclusive = true }
                    }
                }
            )
        }
    }
}
