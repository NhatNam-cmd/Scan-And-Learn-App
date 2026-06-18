package com.example.englishapp.feature.story.domain;

public class StoryBlank {
    private int index;
    private long vocabularyId;
    private String word;
    private String meaning;

    public StoryBlank() {
    }

    public StoryBlank(int index, long vocabularyId, String word, String meaning) {
        this.index = index;
        this.vocabularyId = vocabularyId;
        this.word = word;
        this.meaning = meaning;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(long vocabularyId) {
        this.vocabularyId = vocabularyId;
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
}
