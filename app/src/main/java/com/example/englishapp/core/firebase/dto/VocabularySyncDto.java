package com.example.englishapp.core.firebase.dto;

import java.util.Map;

public class VocabularySyncDto {
    private String word;
    private String meaning;
    private String phonetic;
    private String exampleSentence;
    private String sourceType;
    private int masteryLevel;
    private boolean isMastered;
    private long nextReviewDate;
    private long createdAt;
    private long updatedAt;
    private Map<String, Object> metadata;

    public VocabularySyncDto() {} // Bắt buộc cho Firestore

    public VocabularySyncDto(String word, String meaning, String phonetic,
                             String exampleSentence, String sourceType,
                             int masteryLevel, boolean isMastered,
                             long nextReviewDate, long createdAt, long updatedAt) {
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.sourceType = sourceType;
        this.masteryLevel = masteryLevel;
        this.isMastered = isMastered;
        this.nextReviewDate = nextReviewDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters & Setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public int getMasteryLevel() { return masteryLevel; }
    public void setMasteryLevel(int masteryLevel) { this.masteryLevel = masteryLevel; }

    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { isMastered = mastered; }

    public long getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}