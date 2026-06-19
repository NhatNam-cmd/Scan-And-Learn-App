package com.example.englishapp.feature.scan.presentation.state;

import com.example.englishapp.core.model.Vocabulary;

public abstract class ScanUiState {
    private ScanUiState() {}

    public static final class Idle extends ScanUiState {
        public static final Idle INSTANCE = new Idle();
        private Idle() {}
    }

    public static final class Loading extends ScanUiState {
        public static final Loading INSTANCE = new Loading();
        private Loading() {}
    }

    public static final class ScannedText extends ScanUiState {
        private final String text;
        public ScannedText(String text) { this.text = text; }
        public String getText() { return text; }
    }

    public static final class WordFound extends ScanUiState {
        private final Vocabulary vocabulary;
        public WordFound(Vocabulary vocabulary) { this.vocabulary = vocabulary; }
        public Vocabulary getVocabulary() { return vocabulary; }
    }

    public static final class WordSaved extends ScanUiState {
        private final String word;
        public WordSaved(String word) { this.word = word; }
        public String getWord() { return word; }
    }

    public static final class Error extends ScanUiState {
        private final String message;
        public Error(String message) { this.message = message; }
        public String getMessage() { return message; }
    }
}