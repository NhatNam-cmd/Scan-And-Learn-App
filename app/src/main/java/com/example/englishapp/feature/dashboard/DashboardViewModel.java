package com.example.englishapp.feature.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.datastore.UserPreferences;

import java.util.List;


import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel for the Dashboard screen.
 * Aggregates statistics from the local Room database and UserPreferences.
 */
@HiltViewModel
public class DashboardViewModel extends ViewModel {

    private final AppDatabase database;
    private final ExecutorProvider executorProvider;
    private final UserPreferences userPreferences;

    private final MutableLiveData<DashboardUiState> uiState = new MutableLiveData<>(DashboardUiState.loading());

    @Inject
    public DashboardViewModel(AppDatabase database, ExecutorProvider executorProvider,
                              UserPreferences userPreferences) {
        this.database = database;
        this.executorProvider = executorProvider;
        this.userPreferences = userPreferences;
    }

    public LiveData<DashboardUiState> getUiState() {
        return uiState;
    }

    /**
     * Loads all dashboard statistics from the database on the IO thread,
     * then posts the result to the main thread.
     */
    public void loadStats() {
        executorProvider.getIoExecutor().execute(() -> {
            try {
                int totalWords = database.vocabularyDao().countAllWords();
                int masteredWords = database.vocabularyDao().countMasteredWords();
                int dueForReviewCount = database.vocabularyDao().countDueWords(System.currentTimeMillis());
                int completedQuizzes = database.quizDao().countCompletedQuizzes();

                // Count completed stories from story dao
                int completedStories = countCompletedStories();

                // Streak calculation
                List<Long> reviewedDays = database.reviewHistoryDao().getReviewedDaysDesc();
                int streakDays = calculateStreak(reviewedDays);

                // Words learned today: count vocab added today
                int wordsLearnedToday = countWordsLearnedToday();

                int dailyGoal = userPreferences.getDailyGoal();

                DashboardUiState state = new DashboardUiState(
                        totalWords,
                        masteredWords,
                        streakDays,
                        completedQuizzes,
                        completedStories,
                        dueForReviewCount,
                        dailyGoal,
                        wordsLearnedToday,
                        false
                );

                executorProvider.postToMainThread(() -> uiState.setValue(state));

            } catch (Exception e) {
                // On error, post an empty non-loading state rather than crashing
                executorProvider.postToMainThread(() ->
                        uiState.setValue(new DashboardUiState(0, 0, 0, 0, 0, 0,
                                userPreferences.getDailyGoal(), 0, false)));
            }
        });
    }

    /**
     * Counts vocabulary entries that were created today (since midnight local time).
     * Uses the synchronous {@code countWordsAddedSince} DAO query — safe on IO thread.
     */
    private int countWordsLearnedToday() {
        try {
            return database.vocabularyDao().countWordsAddedSince(getStartOfDayMillis());
        } catch (Exception e) {
            return 0;
        }
    }



    /**
     * Counts total stories that have been completed (stored in the DB).
     * Safe to call on the IO thread — uses a synchronous @Query.
     */
    private int countCompletedStories() {
        try {
            return database.storyDao().countStories();
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Calculates the current learning streak in days.
     * A streak is the number of consecutive days (ending today or yesterday) with at least one review.
     */
    private int calculateStreak(List<Long> reviewedDaysDesc) {
        if (reviewedDaysDesc == null || reviewedDaysDesc.isEmpty()) return 0;
        long currentDay = System.currentTimeMillis() / 86400000L;
        long expectedDay = reviewedDaysDesc.get(0).equals(currentDay) ? currentDay : currentDay - 1;
        int streak = 0;
        for (Long reviewedDay : reviewedDaysDesc) {
            if (reviewedDay == null) continue;
            if (reviewedDay == expectedDay) {
                streak++;
                expectedDay--;
            } else if (reviewedDay < expectedDay) {
                break;
            }
        }
        return streak;
    }

    private long getStartOfDayMillis() {
        long now = System.currentTimeMillis();
        return (now / 86400000L) * 86400000L;
    }
}
