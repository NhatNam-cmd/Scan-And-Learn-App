package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "quiz_session")
public class QuizSessionEntity {

    @PrimaryKey(autoGenerate = true)
    private long quizId;

    private int score;
    private int totalQuestions;
    private long startedAt;
    private long completedAt;

    public QuizSessionEntity(long quizId, int score, int totalQuestions,
                             long startedAt, long completedAt) {
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }

    // Convenience constructor without auto-generated ID
    @Ignore
    public QuizSessionEntity(int score, int totalQuestions, long startedAt, long completedAt) {
        this(0, score, totalQuestions, startedAt, completedAt);
    }

    // Getters
    public long getQuizId() { return quizId; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public long getStartedAt() { return startedAt; }
    public long getCompletedAt() { return completedAt; }

    // Setters
    public void setQuizId(long quizId) { this.quizId = quizId; }
    public void setScore(int score) { this.score = score; }
    public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
    public void setStartedAt(long startedAt) { this.startedAt = startedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizSessionEntity that = (QuizSessionEntity) o;
        return quizId == that.quizId
                && score == that.score
                && totalQuestions == that.totalQuestions
                && startedAt == that.startedAt
                && completedAt == that.completedAt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, score, totalQuestions, startedAt, completedAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "QuizSessionEntity{" +
                "quizId=" + quizId +
                ", score=" + score +
                ", totalQuestions=" + totalQuestions +
                ", startedAt=" + startedAt +
                ", completedAt=" + completedAt +
                '}';
    }
}
