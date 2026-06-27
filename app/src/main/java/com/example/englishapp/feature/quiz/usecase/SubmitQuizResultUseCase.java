package com.example.englishapp.feature.quiz.usecase;

import com.example.englishapp.feature.quiz.domain.repository.model.QuizQuestion;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

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
        long quizId = repository.saveQuizSession(score, questions.size(), startedAt, completedAt);

        for (QuizQuestion q : questions) {
            long vocabId = q.getTargetVocabulary().getVocabularyId();
            boolean isCorrect = q.isCorrect();
            repository.saveQuizQuestionResult(quizId, vocabId, isCorrect);
        }
    }
}
