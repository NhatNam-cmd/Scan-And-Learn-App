package com.project.englishapp.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD, // Tạm thời để Dashboard làm trang chủ để bạn test UI
        modifier = modifier
    ) {

        /*
         * FULL SCREEN ROUTES
         */
        composable(Routes.SPLASH) {
            PlaceholderScreen("Splash Screen")
        }

        composable(Routes.LOGIN) {
            PlaceholderScreen("Login Screen")
        }

        /*
         * BOTTOM NAV TABS
         */
        composable(Routes.DASHBOARD) {
            PlaceholderScreen("Dashboard Tab")
        }

        composable(Routes.SCAN) {
            PlaceholderScreen("Scanner Tab")
        }

        composable(Routes.VOCABULARY) {
            PlaceholderScreen("Vocabulary Tab")
        }

        composable(Routes.STORY_HOME) {
            PlaceholderScreen("Story AI Tab")
        }

        composable(Routes.PROFILE) {
            PlaceholderScreen("Profile Tab")
        }

        /*
         * SUB SCREENS
         */
        composable(Routes.VOCABULARY_DETAIL) { backStackEntry ->
            val vocabularyId = backStackEntry.arguments?.getString("vocabularyId")
            PlaceholderScreen("Vocabulary Detail: ID = $vocabularyId")
        }

        composable(Routes.QUIZ_CONFIG) { PlaceholderScreen("Quiz Config") }
        composable(Routes.QUIZ_SESSION) { PlaceholderScreen("Quiz Session") }
        composable(Routes.QUIZ_RESULT) { PlaceholderScreen("Quiz Result") }
        composable(Routes.STORY_SESSION) { PlaceholderScreen("Story Session") }
        composable(Routes.STORY_RESULT) { PlaceholderScreen("Story Result") }
        composable(Routes.SETTINGS) { PlaceholderScreen("Settings") }
        composable(Routes.LEADERBOARD) { PlaceholderScreen("Leaderboard") }
    }
}

// ---------------------------------------------------------------------------
// 🛠 HÀM MẪU (DÙNG TẠM): Hiển thị text ra giữa màn hình để test Navigation
// ---------------------------------------------------------------------------
@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = title)
    }
}