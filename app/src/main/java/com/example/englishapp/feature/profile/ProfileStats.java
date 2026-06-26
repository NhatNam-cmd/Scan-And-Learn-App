package com.example.englishapp.feature.profile;

public class ProfileStats {
    private final int totalWords;
    private final int masteredWords;
    private final int streakDays;
    private final int completedQuizzes;
    private final long lastSyncedAt;

    public ProfileStats(int totalWords, int masteredWords, int streakDays,
            int completedQuizzes, long lastSyncedAt) {
        this.totalWords = totalWords;
        this.masteredWords = masteredWords;
        this.streakDays = streakDays;
        this.completedQuizzes = completedQuizzes;
        this.lastSyncedAt = lastSyncedAt;
    }

    public int getTotalWords() {
        return totalWords;
    }

    public int getMasteredWords() {
        return masteredWords;
    }

    public int getStreakDays() {
        return streakDays;
    }

    public int getCompletedQuizzes() {
        return completedQuizzes;
    }

    public long getLastSyncedAt() {
        return lastSyncedAt;
    }
}
