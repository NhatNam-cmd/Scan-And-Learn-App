package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(
        tableName = "review_history",
        foreignKeys = {
                @ForeignKey(
                        entity = VocabularyEntity.class,
                        parentColumns = {"vocabularyId"},
                        childColumns = {"vocabularyId"},
                        onDelete = ForeignKey.CASCADE // Xóa từ thì xóa luôn lịch sử ôn tập
                )
        },
        indices = {@Index("vocabularyId")}
)
public class ReviewHistoryEntity {

    @PrimaryKey(autoGenerate = true)
    private long reviewId;

    private long vocabularyId;
    private boolean isCorrect;
    private int oldLevel;
    private int newLevel;
    private long reviewedAt;

    public ReviewHistoryEntity(long reviewId, long vocabularyId, boolean isCorrect,
                               int oldLevel, int newLevel, long reviewedAt) {
        this.reviewId = reviewId;
        this.vocabularyId = vocabularyId;
        this.isCorrect = isCorrect;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.reviewedAt = reviewedAt;
    }

    // Convenience constructor with defaults
    @Ignore
    public ReviewHistoryEntity(long vocabularyId, boolean isCorrect, int oldLevel, int newLevel) {
        this(0, vocabularyId, isCorrect, oldLevel, newLevel, System.currentTimeMillis());
    }

    // Getters
    public long getReviewId() { return reviewId; }
    public long getVocabularyId() { return vocabularyId; }
    public boolean isCorrect() { return isCorrect; }
    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }
    public long getReviewedAt() { return reviewedAt; }

    // Setters
    public void setReviewId(long reviewId) { this.reviewId = reviewId; }
    public void setVocabularyId(long vocabularyId) { this.vocabularyId = vocabularyId; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
    public void setOldLevel(int oldLevel) { this.oldLevel = oldLevel; }
    public void setNewLevel(int newLevel) { this.newLevel = newLevel; }
    public void setReviewedAt(long reviewedAt) { this.reviewedAt = reviewedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewHistoryEntity that = (ReviewHistoryEntity) o;
        return reviewId == that.reviewId
                && vocabularyId == that.vocabularyId
                && isCorrect == that.isCorrect
                && oldLevel == that.oldLevel
                && newLevel == that.newLevel
                && reviewedAt == that.reviewedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId, vocabularyId, isCorrect, oldLevel, newLevel, reviewedAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "ReviewHistoryEntity{" +
                "reviewId=" + reviewId +
                ", vocabularyId=" + vocabularyId +
                ", isCorrect=" + isCorrect +
                ", oldLevel=" + oldLevel +
                ", newLevel=" + newLevel +
                ", reviewedAt=" + reviewedAt +
                '}';
    }
}
