package com.example.englishapp.feature.quiz.usecase;

import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.quiz.domain.repository.model.QuizQuestion;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class SubmitQuizResultUseCase {

    private final QuizRepository repository;

    @Inject
    public SubmitQuizResultUseCase(QuizRepository repository) {
        this.repository = repository;
    }

    public void invoke(List<QuizQuestion> questions, long startedAt) {
        if (questions == null || questions.isEmpty()) {
            return;
        }

        int score = 0;
        for (QuizQuestion q : questions) {
            if (q.isCorrect()) {
                score++;
            }
        }

        long completedAt = System.currentTimeMillis();
        QuizSessionEntity session = new QuizSessionEntity(score, questions.size(), startedAt, completedAt);

        List<QuizQuestionCrossRef> crossRefs = new ArrayList<>();
        List<VocabularyEntity> updatedVocabs = new ArrayList<>();
        List<ReviewHistoryEntity> reviewHistories = new ArrayList<>();

        for (QuizQuestion q : questions) {
            VocabularyEntity vocab = q.getTargetVocabulary();
            long vocabId = vocab.getVocabularyId();
            boolean isCorrect = q.isCorrect();

            crossRefs.add(new QuizQuestionCrossRef(0, vocabId, isCorrect));

            int oldLevel = vocab.getMasteryLevel();
            int newLevel = oldLevel;

            if (isCorrect) {
                newLevel = Math.min(5, oldLevel + 1);
            } else {
                newLevel = 0; // Reset to 0 if wrong
            }

            long offset = getNextReviewDateOffset(newLevel);

            vocab.setMasteryLevel(newLevel);
            vocab.setNextReviewDate(System.currentTimeMillis() + offset);
            vocab.setMastered(newLevel >= 5);
            vocab.setUpdatedAt(System.currentTimeMillis());

            updatedVocabs.add(vocab);
            reviewHistories.add(new ReviewHistoryEntity(vocabId, isCorrect, oldLevel, newLevel));
        }

        repository.submitQuizSessionTransaction(session, crossRefs, updatedVocabs, reviewHistories);
    }

    private long getNextReviewDateOffset(int level) {
        long oneDay = 24L * 60 * 60 * 1000;
        switch (level) {
            case 0: return 0;
            case 1: return oneDay;
            case 2: return 3 * oneDay;
            case 3: return 7 * oneDay;
            case 4: return 14 * oneDay;
            case 5: return 30 * oneDay;
            default: return 30 * oneDay;
        }
    }
}
