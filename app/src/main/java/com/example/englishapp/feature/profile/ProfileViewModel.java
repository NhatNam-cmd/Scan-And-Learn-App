package com.example.englishapp.feature.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.AppDatabase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {
    private final AppDatabase database;
    private final ExecutorProvider executorProvider;
    private final MutableLiveData<ProfileStats> stats = new MutableLiveData<>();

    @Inject
    public ProfileViewModel(AppDatabase database, ExecutorProvider executorProvider) {
        this.database = database;
        this.executorProvider = executorProvider;
    }

    public LiveData<ProfileStats> getStats() {
        return stats;
    }

    public void loadStats() {
        executorProvider.getIoExecutor().execute(() -> {
            int totalWords = database.vocabularyDao().countAllWords();
            int masteredWords = database.vocabularyDao().countMasteredWords();
            int completedQuizzes = database.quizDao().countCompletedQuizzes();
            int streakDays = calculateStreak(database.reviewHistoryDao().getReviewedDaysDesc());
            ProfileStats value = new ProfileStats(totalWords, masteredWords, streakDays,
                    completedQuizzes, System.currentTimeMillis());
            executorProvider.postToMainThread(() -> stats.setValue(value));
        });
    }

    private int calculateStreak(List<Long> reviewedDaysDesc) {
        if (reviewedDaysDesc == null || reviewedDaysDesc.isEmpty()) {
            return 0;
        }
        long currentDay = System.currentTimeMillis() / 86400000L;
        long expectedDay = reviewedDaysDesc.get(0) == currentDay ? currentDay : currentDay - 1;
        int streak = 0;
        for (Long reviewedDay : reviewedDaysDesc) {
            if (reviewedDay == null) {
                continue;
            }
            if (reviewedDay == expectedDay) {
                streak++;
                expectedDay--;
            } else if (reviewedDay < expectedDay) {
                break;
            }
        }
        return streak;
    }
}
