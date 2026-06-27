package com.example.englishapp.feature.quiz.domain.repository.model;

import com.example.englishapp.core.database.entity.VocabularyEntity;
import java.util.List;

public class QuizQuestion {
    private VocabularyEntity targetVocabulary;
    private List<String> options;
    private String correctAnswer;
    private String selectedAnswer;
    private boolean isMeaningToWord; // true if question gives meaning, false if gives word

    public QuizQuestion(VocabularyEntity targetVocabulary, List<String> options, String correctAnswer, boolean isMeaningToWord) {
        this.targetVocabulary = targetVocabulary;
        this.options = options;
        this.correctAnswer = correctAnswer;
        this.isMeaningToWord = isMeaningToWord;
        this.selectedAnswer = null;
    }

    public VocabularyEntity getTargetVocabulary() {
        return targetVocabulary;
    }

    public List<String> getOptions() {
        return options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public boolean isMeaningToWord() {
        return isMeaningToWord;
    }

    public boolean isCorrect() {
        return correctAnswer != null && correctAnswer.equals(selectedAnswer);
    }
}
