package com.project.englishapp.navigation

object Routes {
    // Auth & Splash
    const val SPLASH = "splash"
    const val LOGIN = "login"

    // Bottom Navigation (Main Tabs)
    const val DASHBOARD = "dashboard"
    const val SCAN = "scan"
    const val VOCABULARY = "vocabulary"
    const val STORY_HOME = "story_home"
    const val PROFILE = "profile"

    // Feature Screens (Các màn hình con)
    const val STORY_SESSION = "story_session"
    const val STORY_RESULT = "story_result"

    const val QUIZ_CONFIG = "quiz_config"
    const val QUIZ_SESSION = "quiz_session"
    const val QUIZ_RESULT = "quiz_result"

    const val VOCABULARY_DETAIL = "vocabulary_detail/{vocabularyId}"

    const val SETTINGS = "settings"
    const val LEADERBOARD = "leaderboard"
}