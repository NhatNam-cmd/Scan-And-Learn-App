package com.example.englishapp.feature.vocabulary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.srs.SrsEngine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * ViewModel cho phiên ôn tập Flashcard.
 *
 * Quản lý:
 *  - Danh sách từ cần ôn (shuffle để tránh nhàm)
 *  - Con trỏ vị trí thẻ hiện tại
 *  - Thống kê đã nhớ / chưa nhớ
 *  - Giao tiếp với SRS engine để cập nhật lịch ôn
 */
@HiltViewModel
public class FlashcardSessionViewModel extends ViewModel {

    private final VocabularyDao vocabularyDao;
    private final ReviewHistoryDao reviewHistoryDao;
    private final SrsEngine srsEngine;
    private final ExecutorProvider executorProvider;

    // Danh sách từ trong phiên ôn tập
    private final List<VocabularyEntity> sessionWords = new ArrayList<>();

    // Vị trí thẻ hiện tại (0-based)
    private final MutableLiveData<Integer> currentIndex = new MutableLiveData<>(0);

    // Trạng thái phiên
    private final MutableLiveData<SessionState> sessionState = new MutableLiveData<>(SessionState.LOADING);

    // Thống kê
    private int rememberedCount = 0;
    private int forgotCount = 0;

    // Danh sách id từ chưa nhớ (để hỗ trợ "ôn lại")
    private final List<Long> forgotIds = new ArrayList<>();

    /** Trạng thái phiên ôn tập */
    public enum SessionState {
        LOADING,    // Đang tải từ
        ACTIVE,     // Đang ôn tập
        FINISHED    // Đã hoàn thành
    }

    @Inject
    public FlashcardSessionViewModel(VocabularyDao vocabularyDao,
                                     ReviewHistoryDao reviewHistoryDao,
                                     SrsEngine srsEngine,
                                     ExecutorProvider executorProvider) {
        this.vocabularyDao = vocabularyDao;
        this.reviewHistoryDao = reviewHistoryDao;
        this.srsEngine = srsEngine;
        this.executorProvider = executorProvider;
    }

    // ======== Setup ========

    /**
     * Khởi tạo phiên ôn từ danh sách ID cụ thể (từ banner hoặc batch select).
     * Shuffle để tránh học theo thứ tự cố định.
     */
    public void initWithIds(long[] ids) {
        if (ids == null || ids.length == 0) {
            sessionState.setValue(SessionState.FINISHED);
            return;
        }
        List<Long> idList = new ArrayList<>();
        for (long id : ids) idList.add(id);

        executorProvider.getIoExecutor().execute(() -> {
            List<VocabularyEntity> words = vocabularyDao.getVocabularyByIds(idList);
            executorProvider.postToMainThread(() -> {
                sessionWords.clear();
                sessionWords.addAll(words);
                Collections.shuffle(sessionWords); // shuffle để học ngẫu nhiên
                rememberedCount = 0;
                forgotCount = 0;
                forgotIds.clear();
                currentIndex.setValue(0);
                sessionState.setValue(words.isEmpty() ? SessionState.FINISHED : SessionState.ACTIVE);
            });
        });
    }

    /**
     * Khởi tạo phiên ôn từ tất cả từ đến hạn (daily review).
     */
    public void initDailyReview() {
        executorProvider.getIoExecutor().execute(() -> {
            long now = System.currentTimeMillis();
            List<VocabularyEntity> words = vocabularyDao.getDueWords(now, 200);
            executorProvider.postToMainThread(() -> {
                sessionWords.clear();
                sessionWords.addAll(words);
                Collections.shuffle(sessionWords);
                rememberedCount = 0;
                forgotCount = 0;
                forgotIds.clear();
                currentIndex.setValue(0);
                sessionState.setValue(words.isEmpty() ? SessionState.FINISHED : SessionState.ACTIVE);
            });
        });
    }

    /**
     * Khởi tạo phiên ôn tất cả từ chưa mastered (ôn tổng quát).
     */
    public void initAllUnmastered() {
        executorProvider.getIoExecutor().execute(() -> {
            // Lấy tất cả từ chưa mastered
            List<VocabularyEntity> allWords = vocabularyDao.getVocabularyByIds(new ArrayList<>());
            // Thay bằng query riêng qua getDueWords + getUnmasteredWords
            // Dùng getDueWords với giới hạn lớn + lọc thêm
            long farFuture = Long.MAX_VALUE;
            List<VocabularyEntity> words = vocabularyDao.getDueWords(farFuture, 500);
            executorProvider.postToMainThread(() -> {
                sessionWords.clear();
                sessionWords.addAll(words);
                Collections.shuffle(sessionWords);
                rememberedCount = 0;
                forgotCount = 0;
                forgotIds.clear();
                currentIndex.setValue(0);
                sessionState.setValue(words.isEmpty() ? SessionState.FINISHED : SessionState.ACTIVE);
            });
        });
    }

    // ======== Getters ========

    public LiveData<Integer> getCurrentIndex() { return currentIndex; }
    public LiveData<SessionState> getSessionState() { return sessionState; }

    public VocabularyEntity getCurrentWord() {
        Integer idx = currentIndex.getValue();
        if (idx == null || idx >= sessionWords.size()) return null;
        return sessionWords.get(idx);
    }

    public int getTotalCount() { return sessionWords.size(); }
    public int getRememberedCount() { return rememberedCount; }
    public int getForgotCount() { return forgotCount; }
    public List<Long> getForgotIds() { return new ArrayList<>(forgotIds); }

    /**
     * Tiến độ 0–100 dựa vào số từ đã xử lý / tổng
     */
    public int getProgressPercent() {
        if (sessionWords.isEmpty()) return 100;
        Integer idx = currentIndex.getValue();
        int done = (idx == null ? 0 : idx);
        return (int) ((done / (float) sessionWords.size()) * 100);
    }

    // ======== Actions ========

    /**
     * Ghi nhận kết quả ôn tập và chuyển sang thẻ tiếp theo.
     * @param remembered true = đã nhớ, false = chưa nhớ
     */
    public void submitAnswer(boolean remembered) {
        VocabularyEntity word = getCurrentWord();
        if (word == null) return;

        if (remembered) {
            rememberedCount++;
        } else {
            forgotCount++;
            forgotIds.add(word.getVocabularyId());
        }

        // Cập nhật SRS trong background
        final boolean isCorrect = remembered;
        executorProvider.getIoExecutor().execute(() ->
                srsEngine.applyReview(word, isCorrect, vocabularyDao, reviewHistoryDao));

        // Chuyển thẻ tiếp theo
        advanceToNext();
    }

    private void advanceToNext() {
        Integer idx = currentIndex.getValue();
        if (idx == null) return;
        int next = idx + 1;
        if (next >= sessionWords.size()) {
            sessionState.setValue(SessionState.FINISHED);
        } else {
            currentIndex.setValue(next);
        }
    }
}
