package com.example.englishapp.core.common;

/**
 * Application-wide constants.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Database
    public static final String DATABASE_NAME = "scan_learn_db";

    // Network
    public static final String DICTIONARY_BASE_URL = "https://api.dictionaryapi.dev/api/v2/";
    public static final long API_TIMEOUT_SECONDS = 30L;

    // SRS Levels (Theo SRS SPECIFICATION)
    public static final int SRS_LEVEL_NEW = 0;
    public static final int SRS_LEVEL_LEARNING_1 = 1;
    public static final int SRS_LEVEL_LEARNING_2 = 2;
    public static final int SRS_LEVEL_REVIEWING_1 = 3;
    public static final int SRS_LEVEL_REVIEWING_2 = 4;
    public static final int SRS_LEVEL_MASTERED = 5;

    // Fallback & Pagination
    public static final int STORY_WORD_LIMIT = 5;
    public static final int QUIZ_QUESTION_LIMIT = 10;
}
