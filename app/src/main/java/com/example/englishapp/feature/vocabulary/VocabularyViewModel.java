package com.example.englishapp.feature.vocabulary;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.srs.SrsEngine;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class VocabularyViewModel extends ViewModel {

    private final VocabularyDao vocabularyDao;
    private final ReviewHistoryDao reviewHistoryDao;
    private final SrsEngine srsEngine;
    private final ExecutorProvider executorProvider;

    @Inject
    public VocabularyViewModel(VocabularyDao vocabularyDao, ReviewHistoryDao reviewHistoryDao,
                               SrsEngine srsEngine, ExecutorProvider executorProvider) {
        this.vocabularyDao = vocabularyDao;
        this.reviewHistoryDao = reviewHistoryDao;
        this.srsEngine = srsEngine;
        this.executorProvider = executorProvider;
    }

    public LiveData<List<VocabularyEntity>> getAllVocabularies() {
        return vocabularyDao.getAllVocabularies();
    }

    public LiveData<VocabularyEntity> observeVocabulary(long vocabularyId) {
        return vocabularyDao.observeVocabularyById(vocabularyId);
    }

    public void updateVocabulary(VocabularyEntity entity) {
        if (entity == null) return;
        entity.setUpdatedAt(System.currentTimeMillis());
        executorProvider.getIoExecutor().execute(() -> vocabularyDao.update(entity));
    }

    public void addManualVocabulary(String word, String meaning, String phonetic,
                                    String example, String note) {
        if (word == null || word.trim().isEmpty() || meaning == null || meaning.trim().isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        VocabularyEntity entity = new VocabularyEntity(
                0L,
                null,
                word.trim(),
                meaning.trim(),
                blankToNull(phonetic),
                blankToNull(example),
                null,
                null,
                blankToNull(note),
                "MANUAL",
                0,
                false,
                now,
                now,
                now
        );
        executorProvider.getIoExecutor().execute(() -> vocabularyDao.insert(entity));
    }

    public void deleteVocabulary(VocabularyEntity entity) {
        if (entity == null) return;
        executorProvider.getIoExecutor().execute(() -> vocabularyDao.delete(entity));
    }

    public void recordReview(VocabularyEntity entity, boolean isCorrect) {
        if (entity == null) return;
        executorProvider.getIoExecutor().execute(() ->
                srsEngine.applyReview(entity, isCorrect, vocabularyDao, reviewHistoryDao));
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
