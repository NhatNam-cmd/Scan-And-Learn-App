package com.example.englishapp.feature.quiz.domain.repository.repository;

import androidx.lifecycle.LiveData;
import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import java.util.List;

public interface QuizRepository {

    // Returns LiveData that can be observed to get all vocabularies
    LiveData<List<VocabularyEntity>> getAllVocabularies();

    // Returns a synchronous list of vocabularies for background quiz generation
    List<VocabularyEntity> getAllVocabulariesSync();

    // Get due words for Spaced Repetition (SRS)
    List<VocabularyEntity> getDueWords(long currentDate);

    // Save quiz session and return its ID
    long saveQuizSession(int score, int totalQuestions, long startedAt, long completedAt);

    // Save the cross-ref data (correct/incorrect) for a specific question in a session
    void saveQuizQuestionResult(long quizId, long vocabularyId, boolean isCorrect);

    // Save the entire quiz session result in a single transaction (SRS update)
    void submitQuizSessionTransaction(
            QuizSessionEntity session,
            List<QuizQuestionCrossRef> pendingCrossRefs,
            List<VocabularyEntity> updatedVocabs,
            List<ReviewHistoryEntity> reviewHistories
    );
}
