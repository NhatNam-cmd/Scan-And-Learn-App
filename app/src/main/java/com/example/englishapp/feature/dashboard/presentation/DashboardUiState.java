package com.example.englishapp.feature.dashboard.presentation;

/**
 * Immutable state object for the Dashboard screen.
 * Holds all statistics and progress information needed to render the UI.
 */
public class DashboardUiState {

    private final int totalWords;
    private final int masteredWords;
    private final int streakDays;
    private final int completedQuizzes;
    private final int completedStories;
    private final int dueForReviewCount;
    private final int dailyGoal;
    private final int wordsLearnedToday;
    private final boolean isLoading;

    public DashboardUiState(
            int totalWords,
            int masteredWords,
            int streakDays,
            int completedQuizzes,
            int completedStories,
            int dueForReviewCount,
            int dailyGoal,
            int wordsLearnedToday,
            boolean isLoading) {
        this.totalWords = totalWords;
        this.masteredWords = masteredWords;
        this.streakDays = streakDays;
        this.completedQuizzes = completedQuizzes;
        this.completedStories = completedStories;
        this.dueForReviewCount = dueForReviewCount;
        this.dailyGoal = dailyGoal;
        this.wordsLearnedToday = wordsLearnedToday;
        this.isLoading = isLoading;
    }

    /** Returns a loading placeholder state. */
    public static DashboardUiState loading() {
        return new DashboardUiState(0, 0, 0, 0, 0, 0, 10, 0, true);
    }

    public int getTotalWords() { return totalWords; }
    public int getMasteredWords() { return masteredWords; }
    public int getStreakDays() { return streakDays; }
    public int getCompletedQuizzes() { return completedQuizzes; }
    public int getCompletedStories() { return completedStories; }
    public int getDueForReviewCount() { return dueForReviewCount; }
    public int getDailyGoal() { return dailyGoal; }
    public int getWordsLearnedToday() { return wordsLearnedToday; }
    public boolean isLoading() { return isLoading; }

    /**
     * Daily goal progress as a float in [0, 1].
     * Capped at 1.0 even if wordsLearnedToday exceeds the goal.
     */
    public float getDailyGoalProgress() {
        if (dailyGoal <= 0) return 0f;
        return Math.min(1f, (float) wordsLearnedToday / (float) dailyGoal);
    }

    /** Percentage label for daily goal (e.g. "7/10"). */
    public String getDailyGoalLabel() {
        return wordsLearnedToday + "/" + dailyGoal;
    }

    /** Mastery percentage label (e.g. "35%"). */
    public int getMasteryPercent() {
        if (totalWords <= 0) return 0;
        return Math.round((float) masteredWords * 100f / (float) totalWords);
    }
}
