package com.example.englishapp.core.model;

import com.example.englishapp.core.database.entity.VocabularyEntity;

public class Vocabulary {
    private long id;
    private String word;
    private String meaning;
    private String phonetic;
    private String exampleSentence;
    private String partOfSpeech;
    private String audioUrl;
    private String sourceType;
    private int masteryLevel;
    private boolean isMastered;
    private long nextReviewDate;

    // Full constructor
    public Vocabulary(long id, String word, String meaning, String phonetic,
                      String exampleSentence, String partOfSpeech, String audioUrl,
                      String sourceType, int masteryLevel, boolean isMastered,
                      long nextReviewDate) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.partOfSpeech = partOfSpeech;
        this.audioUrl = audioUrl;
        this.sourceType = sourceType;
        this.masteryLevel = masteryLevel;
        this.isMastered = isMastered;
        this.nextReviewDate = nextReviewDate;
    }

    // Constructor cho từ mới từ scan
    public Vocabulary(String word, String meaning, String sourceType) {
        this(0, word, meaning, "", "", "", "", sourceType, 0, false, 0L);
    }

    // Chuyển sang Entity để lưu database
    public VocabularyEntity toEntity() {
        VocabularyEntity entity = new VocabularyEntity(
                word, meaning, sourceType
        );
        entity.setVocabularyId(id);
        entity.setPhonetic(phonetic.isEmpty() ? null : phonetic);
        entity.setExampleSentence(exampleSentence.isEmpty() ? null : exampleSentence);
        entity.setMasteryLevel(masteryLevel);
        entity.setMastered(isMastered);
        entity.setNextReviewDate(nextReviewDate);
        return entity;
    }

    // Tạo từ Entity
    public static Vocabulary fromEntity(VocabularyEntity entity) {
        return new Vocabulary(
                entity.getVocabularyId(),
                entity.getWord(),
                entity.getMeaning(),
                entity.getPhonetic() != null ? entity.getPhonetic() : "",
                entity.getExampleSentence() != null ? entity.getExampleSentence() : "",
                "",
                "",
                entity.getSourceType(),
                entity.getMasteryLevel(),
                entity.isMastered(),
                entity.getNextReviewDate()
        );
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public String getExampleSentence() { return exampleSentence; }
    public void setExampleSentence(String exampleSentence) { this.exampleSentence = exampleSentence; }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public String getAudioUrl() { return audioUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public int getMasteryLevel() { return masteryLevel; }
    public void setMasteryLevel(int masteryLevel) { this.masteryLevel = masteryLevel; }

    public boolean isMastered() { return isMastered; }
    public void setMastered(boolean mastered) { isMastered = mastered; }

    public long getNextReviewDate() { return nextReviewDate; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }
}