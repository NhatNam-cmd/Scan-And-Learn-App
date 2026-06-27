package com.example.englishapp.core.firebase.dto;

import java.util.List;
import java.util.Map;

public class UserStatsDto {
    private String userId;
    private int totalWords;
    private int masteredWords;
    private int streakDays;
    private int completedQuizzes;
    private int completedStories;
    private long lastSyncedAt;
    private Map<String, Object> vocabularyProgress;
    private List<Map<String, Object>> reviewHistory;

    // Constructors
    public UserStatsDto() {} // Bắt buộc cho Firestore

    public UserStatsDto(String userId, int totalWords, int masteredWords,
                        int streakDays, int completedQuizzes, int completedStories,
                        long lastSyncedAt, Map<String, Object> vocabularyProgress,
                        List<Map<String, Object>> reviewHistory) {
        this.userId = userId;
        this.totalWords = totalWords;
        this.masteredWords = masteredWords;
        this.streakDays = streakDays;
        this.completedQuizzes = completedQuizzes;
        this.completedStories = completedStories;
        this.lastSyncedAt = lastSyncedAt;
        this.vocabularyProgress = vocabularyProgress;
        this.reviewHistory = reviewHistory;
    }

    // Getters & Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public int getTotalWords() { return totalWords; }
    public void setTotalWords(int totalWords) { this.totalWords = totalWords; }

    public int getMasteredWords() { return masteredWords; }
    public void setMasteredWords(int masteredWords) { this.masteredWords = masteredWords; }

    public int getStreakDays() { return streakDays; }
    public void setStreakDays(int streakDays) { this.streakDays = streakDays; }

    public int getCompletedQuizzes() { return completedQuizzes; }
    public void setCompletedQuizzes(int completedQuizzes) { this.completedQuizzes = completedQuizzes; }

    public int getCompletedStories() { return completedStories; }
    public void setCompletedStories(int completedStories) { this.completedStories = completedStories; }

    public long getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(long lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }

    public Map<String, Object> getVocabularyProgress() { return vocabularyProgress; }
    public void setVocabularyProgress(Map<String, Object> vocabularyProgress) {
        this.vocabularyProgress = vocabularyProgress;
    }

    public List<Map<String, Object>> getReviewHistory() { return reviewHistory; }
    public void setReviewHistory(List<Map<String, Object>> reviewHistory) {
        this.reviewHistory = reviewHistory;
    }
}