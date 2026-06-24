package com.example.englishapp.core.model;

public class VocabularyLookup {
    private String word;
    private String meaning;
    private String phonetic;

    public VocabularyLookup(String word, String meaning, String phonetic) {
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }
}