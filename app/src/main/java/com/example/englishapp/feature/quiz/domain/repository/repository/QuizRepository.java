package com.example.englishapp.feature.quiz.domain.repository.repository;

import androidx.lifecycle.LiveData;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import java.util.List;

public interface QuizRepository {

    // Returns LiveData that can be observed to get all vocabularies
    LiveData<List<VocabularyEntity>> getAllVocabularies();

    // Returns a synchronous list of vocabularies for background quiz generation
    List<VocabularyEntity> getAllVocabulariesSync();

    // Allows getting a subset of random vocabulary. Room doesn't have a built-in easy random query
    // without ORDER BY RANDOM(), but we can just fetch all and shuffle in memory.

    // Save quiz session and return its ID
    long saveQuizSession(int score, int totalQuestions, long startedAt, long completedAt);

    // Save the cross-ref data (correct/incorrect) for a specific question in a session
    void saveQuizQuestionResult(long quizId, long vocabularyId, boolean isCorrect);
}
