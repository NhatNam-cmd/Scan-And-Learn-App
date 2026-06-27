package com.example.englishapp.feature.quiz.usecase;

import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.quiz.domain.repository.model.QuizQuestion;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

public class GenerateQuizUseCase {

    private final QuizRepository repository;
    private final Random random;

    @Inject
    public GenerateQuizUseCase(QuizRepository repository) {
        this.repository = repository;
        this.random = new Random();
    }

    public List<QuizQuestion> invoke(int numQuestions) {
        List<VocabularyEntity> allVocabs = repository.getAllVocabulariesSync();

        if (allVocabs == null || allVocabs.isEmpty()) {
            return new ArrayList<>();
        }

        // Shuffle to randomize target words
        Collections.shuffle(allVocabs, random);

        int actualNumQuestions = Math.min(numQuestions, allVocabs.size());
        List<QuizQuestion> questions = new ArrayList<>();

        for (int i = 0; i < actualNumQuestions; i++) {
            VocabularyEntity target = allVocabs.get(i);

            // 50% chance to be Word->Meaning or Meaning->Word
            boolean isMeaningToWord = random.nextBoolean();

            String correctAnswer = isMeaningToWord ? target.getWord() : target.getMeaning();

            List<String> options = new ArrayList<>();
            options.add(correctAnswer);

            // Get 3 distractors
            List<VocabularyEntity> distractorsPool = new ArrayList<>(allVocabs);
            distractorsPool.remove(target);
            Collections.shuffle(distractorsPool, random);

            for (int j = 0; j < Math.min(3, distractorsPool.size()); j++) {
                VocabularyEntity distractor = distractorsPool.get(j);
                options.add(isMeaningToWord ? distractor.getWord() : distractor.getMeaning());
            }

            // Shuffle options so correct answer is not always first
            Collections.shuffle(options, random);

            questions.add(new QuizQuestion(target, options, correctAnswer, isMeaningToWord));
        }

        return questions;
    }
}
