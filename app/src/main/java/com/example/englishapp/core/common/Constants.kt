package com.example.englishapp.core.common

object Constants {
    // Database
    const val DATABASE_NAME = "scan_learn_db"

    // Network
    const val DICTIONARY_BASE_URL = "https://api.dictionaryapi.dev/api/v2/"
    const val API_TIMEOUT_SECONDS = 30L

    // SRS Levels (Theo SRS SPECIFICATION)
    const val SRS_LEVEL_NEW = 0
    const val SRS_LEVEL_LEARNING_1 = 1
    const val SRS_LEVEL_LEARNING_2 = 2
    const val SRS_LEVEL_REVIEWING_1 = 3
    const val SRS_LEVEL_REVIEWING_2 = 4
    const val SRS_LEVEL_MASTERED = 5

    // Fallback & Pagination
    const val STORY_WORD_LIMIT = 5
    const val QUIZ_QUESTION_LIMIT = 10
}