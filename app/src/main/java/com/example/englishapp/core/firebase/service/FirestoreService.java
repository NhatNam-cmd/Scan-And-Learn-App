package com.example.englishapp.core.firebase.service;

import android.util.Log;

import com.example.englishapp.core.firebase.dto.UserStatsDto;
import com.example.englishapp.core.firebase.dto.VocabularySyncDto;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class FirestoreService {
    private static final String TAG = "FirestoreService";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_VOCABULARY = "vocabulary";
    private static final String COLLECTION_REVIEW_HISTORY = "review_history";
    private static final String COLLECTION_QUIZZES = "quizzes";
    private static final String COLLECTION_STORIES = "stories";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    @Inject
    public FirestoreService(FirebaseFirestore firestore, FirebaseAuth firebaseAuth) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    private String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return user.getUid();
    }

    // === USER STATS OPERATIONS ===

    /**
     * Upload user statistics to Firestore
     */
    public Task<Void> uploadUserStats(UserStatsDto stats) {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .set(stats)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "User stats uploaded successfully for user: " + userId))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload user stats", e));
    }

    /**
     * Fetch user statistics from Firestore
     */
    public Task<UserStatsDto> fetchUserStats() {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            return document.toObject(UserStatsDto.class);
                        }
                    }
                    return null;
                });
    }

    // === VOCABULARY SYNC OPERATIONS ===

    /**
     * Upload all vocabulary to Firestore (full sync)
     */
    public Task<Void> uploadAllVocabulary(List<VocabularySyncDto> vocabList) {
        String userId = getCurrentUserId();
        WriteBatch batch = firestore.batch();
        CollectionReference vocabRef = firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VOCABULARY);

        for (VocabularySyncDto vocab : vocabList) {
            DocumentReference docRef = vocabRef.document();
            batch.set(docRef, vocab);
        }

        return batch.commit()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "All vocabulary uploaded successfully"))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload vocabulary", e));
    }

    /**
     * Upload single vocabulary item (incremental sync)
     */
    public Task<Void> uploadVocabularyItem(VocabularySyncDto vocab) {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VOCABULARY)
                .document(vocab.getWord().toLowerCase()) // Use word as document ID
                .set(vocab)
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Vocabulary uploaded: " + vocab.getWord()))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload vocabulary: " + vocab.getWord(), e));
    }

    /**
     * Fetch all vocabulary from Firestore
     */
    public Task<List<VocabularySyncDto>> fetchAllVocabulary() {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VOCABULARY)
                .get()
                .continueWith(task -> {
                    List<VocabularySyncDto> vocabList = new ArrayList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        for (DocumentSnapshot document : result) {
                            VocabularySyncDto vocab = document.toObject(VocabularySyncDto.class);
                            if (vocab != null) {
                                vocabList.add(vocab);
                            }
                        }
                    }
                    return vocabList;
                });
    }

    /**
     * Delete vocabulary item from Firestore
     */
    public Task<Void> deleteVocabularyItem(String word) {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_VOCABULARY)
                .document(word.toLowerCase())
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Vocabulary deleted: " + word))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to delete vocabulary: " + word, e));
    }

    // === REVIEW HISTORY SYNC ===

    /**
     * Upload review history
     */
    public Task<Void> uploadReviewHistory(String word, Map<String, Object> reviewData) {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_REVIEW_HISTORY)
                .document(word.toLowerCase())
                .collection("entries")
                .add(reviewData)
                .addOnSuccessListener(docRef ->
                        Log.d(TAG, "Review history uploaded for: " + word))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to upload review history for: " + word, e))
                .continueWith(task -> null);
    }

    /**
     * Fetch review history for a specific word
     */
    public Task<List<Map<String, Object>>> fetchReviewHistory(String word) {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_REVIEW_HISTORY)
                .document(word.toLowerCase())
                .collection("entries")
                .orderBy("reviewedAt", Query.Direction.DESCENDING)
                .get()
                .continueWith(task -> {
                    List<Map<String, Object>> history = new ArrayList<>();
                    if (task.isSuccessful()) {
                        QuerySnapshot result = task.getResult();
                        for (DocumentSnapshot doc : result) {
                            history.add(doc.getData());
                        }
                    }
                    return history;
                });
    }

    // === BATCH SYNC ===

    /**
     * Complete sync of all user data
     */
    public Task<Void> syncAllData(UserStatsDto stats, List<VocabularySyncDto> vocabulary) {
        return Tasks.whenAll(
                uploadUserStats(stats),
                uploadAllVocabulary(vocabulary)
        );
    }

    /**
     * Check if user has any data in Firestore
     */
    public Task<Boolean> hasUserData() {
        String userId = getCurrentUserId();
        return firestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        return task.getResult().exists();
                    }
                    return false;
                });
    }

    // === UTILITY METHODS ===

    /**
     * Convert VocabularyEntity to VocabularySyncDto
     */
    public static VocabularySyncDto toSyncDto(com.example.englishapp.core.database.entity.VocabularyEntity entity) {
        return new VocabularySyncDto(
                entity.getWord(),
                entity.getMeaning(),
                entity.getPhonetic() != null ? entity.getPhonetic() : "",
                entity.getExampleSentence() != null ? entity.getExampleSentence() : "",
                entity.getSourceType(),
                entity.getMasteryLevel(),
                entity.isMastered(),
                entity.getNextReviewDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Convert VocabularySyncDto to VocabularyEntity
     */
    public static com.example.englishapp.core.database.entity.VocabularyEntity toEntity(VocabularySyncDto dto) {
        long now = System.currentTimeMillis();
        com.example.englishapp.core.database.entity.VocabularyEntity entity =
                new com.example.englishapp.core.database.entity.VocabularyEntity(
                        dto.getWord(),
                        dto.getMeaning(),
                        dto.getSourceType()
                );
        entity.setPhonetic(dto.getPhonetic().isEmpty() ? null : dto.getPhonetic());
        entity.setExampleSentence(dto.getExampleSentence().isEmpty() ? null : dto.getExampleSentence());
        entity.setMasteryLevel(dto.getMasteryLevel());
        entity.setMastered(dto.isMastered());
        entity.setNextReviewDate(dto.getNextReviewDate());
        entity.setCreatedAt(dto.getCreatedAt() > 0 ? dto.getCreatedAt() : now);
        entity.setUpdatedAt(dto.getUpdatedAt() > 0 ? dto.getUpdatedAt() : now);
        return entity;
    }

    /**
     * Build UserStatsDto from database stats
     */
    public static UserStatsDto buildUserStats(
            String userId,
            int totalWords,
            int masteredWords,
            int streakDays,
            int completedQuizzes,
            int completedStories,
            Map<String, Object> vocabularyProgress,
            List<Map<String, Object>> reviewHistory
    ) {
        return new UserStatsDto(
                userId,
                totalWords,
                masteredWords,
                streakDays,
                completedQuizzes,
                completedStories,
                System.currentTimeMillis(),
                vocabularyProgress != null ? vocabularyProgress : new HashMap<>(),
                reviewHistory != null ? reviewHistory : new ArrayList<>()
        );
    }
}