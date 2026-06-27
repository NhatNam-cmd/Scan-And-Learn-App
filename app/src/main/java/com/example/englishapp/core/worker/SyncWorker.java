package com.example.englishapp.core.worker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.firebase.dto.UserStatsDto;
import com.example.englishapp.core.firebase.dto.VocabularySyncDto;
import com.example.englishapp.core.firebase.service.FirestoreService;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;


public class SyncWorker extends Worker {

    private static final String TAG = "SyncWorker";

    private AppDatabase database;
    private FirestoreService firestoreService;


    public SyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }


    public void setDependencies(AppDatabase database, FirestoreService firestoreService) {
        this.database = database;
        this.firestoreService = firestoreService;
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Log.d(TAG, "Starting sync worker");

            // Check dependencies
            if (database == null || firestoreService == null) {
                Log.e(TAG, "Dependencies not injected");
                return Result.failure();
            }

            // Check if user is authenticated
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                Log.w(TAG, "User not authenticated, skipping sync");
                return Result.success();
            }

            // Perform sync
            boolean success = performFullSync();

            if (success) {
                Log.d(TAG, "Sync completed successfully");
                return Result.success();
            } else {
                Log.e(TAG, "Sync failed");
                return Result.retry();
            }

        } catch (Exception e) {
            Log.e(TAG, "Sync worker exception", e);
            return Result.retry();
        }
    }

    private boolean performFullSync() throws ExecutionException, InterruptedException {
        // 1. Get all data from local database
        List<VocabularyEntity> localVocab = database.vocabularyDao().getAllVocabulariesSync();
        List<ReviewHistoryEntity> reviewHistory = database.reviewHistoryDao().getAllReviewHistorySync();

        // 2. Build sync data
        List<VocabularySyncDto> vocabList = new ArrayList<>();
        for (VocabularyEntity entity : localVocab) {
            vocabList.add(FirestoreService.toSyncDto(entity));
        }

        // 3. Build user stats
        int totalWords = database.vocabularyDao().countAllWords();
        int masteredWords = database.vocabularyDao().countMasteredWords();
        int streakDays = calculateStreak();
        int completedQuizzes = database.quizDao().countCompletedQuizzes();
        int completedStories = database.storyDao().countStories();

        Map<String, Object> vocabProgress = buildVocabularyProgress(localVocab);
        List<Map<String, Object>> reviewHistoryData = buildReviewHistoryData(reviewHistory);

        UserStatsDto stats = FirestoreService.buildUserStats(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                totalWords,
                masteredWords,
                streakDays,
                completedQuizzes,
                completedStories,
                vocabProgress,
                reviewHistoryData
        );

        // 4. Upload all data to Firestore
        Tasks.await(firestoreService.syncAllData(stats, vocabList));

        return true;
    }

    private int calculateStreak() {
        List<Long> reviewedDays = database.reviewHistoryDao().getReviewedDaysDesc();
        if (reviewedDays == null || reviewedDays.isEmpty()) {
            return 0;
        }
        long currentDay = System.currentTimeMillis() / 86400000L;
        long expectedDay = reviewedDays.get(0) == currentDay ? currentDay : currentDay - 1;
        int streak = 0;
        for (Long reviewedDay : reviewedDays) {
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

    private Map<String, Object> buildVocabularyProgress(List<VocabularyEntity> vocab) {
        Map<String, Object> progress = new HashMap<>();
        for (VocabularyEntity entity : vocab) {
            Map<String, Object> wordData = new HashMap<>();
            wordData.put("masteryLevel", entity.getMasteryLevel());
            wordData.put("isMastered", entity.isMastered());
            wordData.put("nextReviewDate", entity.getNextReviewDate());
            wordData.put("updatedAt", entity.getUpdatedAt());
            progress.put(entity.getWord().toLowerCase(), wordData);
        }
        return progress;
    }

    private List<Map<String, Object>> buildReviewHistoryData(List<ReviewHistoryEntity> reviewHistory) {
        List<Map<String, Object>> historyData = new ArrayList<>();
        for (ReviewHistoryEntity entity : reviewHistory) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("vocabularyId", entity.getVocabularyId());
            entry.put("isCorrect", entity.isCorrect());
            entry.put("oldLevel", entity.getOldLevel());
            entry.put("newLevel", entity.getNewLevel());
            entry.put("reviewedAt", entity.getReviewedAt());
            historyData.add(entry);
        }
        return historyData;
    }
}