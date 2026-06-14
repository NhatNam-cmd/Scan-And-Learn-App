package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "vocabulary",
        foreignKeys = {
                @ForeignKey(
                        entity = TopicEntity.class,
                        parentColumns = {"topicId"},
                        childColumns = {"topicId"},
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {@Index("word"), @Index("topicId")}
)
public class VocabularyEntity {

    @PrimaryKey(autoGenerate = true)
    private long vocabularyId;

    @Nullable
    private Long topicId;

    @NonNull
    private String word;

    @NonNull
    private String meaning;

    @Nullable
    private String phonetic;

    @Nullable
    private String exampleSentence;

    @Nullable
    private String imagePath;

    @Nullable
    private String audioPath;

    @Nullable
    private String note;

    @NonNull
    private String sourceType; // "SCAN", "MANUAL", "TOPIC"

    // SRS Fields
    private int masteryLevel;
    private boolean isMastered;
    private long nextReviewDate;

    private long createdAt;
    private long updatedAt;

    public VocabularyEntity(long vocabularyId,
                            @Nullable Long topicId,
                            @NonNull String word,
                            @NonNull String meaning,
                            @Nullable String phonetic,
                            @Nullable String exampleSentence,
                            @Nullable String imagePath,
                            @Nullable String audioPath,
                            @Nullable String note,
                            @NonNull String sourceType,
                            int masteryLevel,
                            boolean isMastered,
                            long nextReviewDate,
                            long createdAt,
                            long updatedAt) {
        this.vocabularyId = vocabularyId;
        this.topicId = topicId;
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.imagePath = imagePath;
        this.audioPath = audioPath;
        this.note = note;
        this.sourceType = sourceType;
        this.masteryLevel = masteryLevel;
        this.isMastered = isMastered;
        this.nextReviewDate = nextReviewDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Convenience constructor with defaults
    @Ignore
    public VocabularyEntity(@NonNull String word, @NonNull String meaning, @NonNull String sourceType) {
        this(0, null, word, meaning, null, null, null, null, null,
                sourceType, 0, false, 0L,
                System.currentTimeMillis(), System.currentTimeMillis());
    }

    // Getters
    public long getVocabularyId() { return vocabularyId; }
    @Nullable public Long getTopicId() { return topicId; }
    @NonNull public String getWord() { return word; }
    @NonNull public String getMeaning() { return meaning; }
    @Nullable public String getPhonetic() { return phonetic; }
    @Nullable public String getExampleSentence() { return exampleSentence; }
    @Nullable public String getImagePath() { return imagePath; }
    @Nullable public String getAudioPath() { return audioPath; }
    @Nullable public String getNote() { return note; }
    @NonNull public String getSourceType() { return sourceType; }
    public int getMasteryLevel() { return masteryLevel; }
    public boolean isMastered() { return isMastered; }
    public long getNextReviewDate() { return nextReviewDate; }
    public long getCreatedAt() { return createdAt; }
    public long getUpdatedAt() { return updatedAt; }

    // Setters
    public void setVocabularyId(long vocabularyId) { this.vocabularyId = vocabularyId; }
    public void setTopicId(@Nullable Long topicId) { this.topicId = topicId; }
    public void setWord(@NonNull String word) { this.word = word; }
    public void setMeaning(@NonNull String meaning) { this.meaning = meaning; }
    public void setPhonetic(@Nullable String phonetic) { this.phonetic = phonetic; }
    public void setExampleSentence(@Nullable String exampleSentence) { this.exampleSentence = exampleSentence; }
    public void setImagePath(@Nullable String imagePath) { this.imagePath = imagePath; }
    public void setAudioPath(@Nullable String audioPath) { this.audioPath = audioPath; }
    public void setNote(@Nullable String note) { this.note = note; }
    public void setSourceType(@NonNull String sourceType) { this.sourceType = sourceType; }
    public void setMasteryLevel(int masteryLevel) { this.masteryLevel = masteryLevel; }
    public void setMastered(boolean mastered) { isMastered = mastered; }
    public void setNextReviewDate(long nextReviewDate) { this.nextReviewDate = nextReviewDate; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VocabularyEntity that = (VocabularyEntity) o;
        return vocabularyId == that.vocabularyId
                && masteryLevel == that.masteryLevel
                && isMastered == that.isMastered
                && nextReviewDate == that.nextReviewDate
                && createdAt == that.createdAt
                && updatedAt == that.updatedAt
                && Objects.equals(topicId, that.topicId)
                && Objects.equals(word, that.word)
                && Objects.equals(meaning, that.meaning)
                && Objects.equals(phonetic, that.phonetic)
                && Objects.equals(exampleSentence, that.exampleSentence)
                && Objects.equals(imagePath, that.imagePath)
                && Objects.equals(audioPath, that.audioPath)
                && Objects.equals(note, that.note)
                && Objects.equals(sourceType, that.sourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vocabularyId, topicId, word, meaning, phonetic,
                exampleSentence, imagePath, audioPath, note, sourceType,
                masteryLevel, isMastered, nextReviewDate, createdAt, updatedAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "VocabularyEntity{" +
                "vocabularyId=" + vocabularyId +
                ", topicId=" + topicId +
                ", word='" + word + '\'' +
                ", meaning='" + meaning + '\'' +
                ", phonetic='" + phonetic + '\'' +
                ", exampleSentence='" + exampleSentence + '\'' +
                ", imagePath='" + imagePath + '\'' +
                ", audioPath='" + audioPath + '\'' +
                ", note='" + note + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", masteryLevel=" + masteryLevel +
                ", isMastered=" + isMastered +
                ", nextReviewDate=" + nextReviewDate +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
