package com.example.englishapp.feature.vocabulary.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.TopicDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.TopicEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.database.entity.VocabularyFtsEntity;
import com.example.englishapp.core.srs.SrsEngine;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class VocabularyViewModel extends ViewModel {

    private final VocabularyDao vocabularyDao;
    private final TopicDao topicDao;
    private final ReviewHistoryDao reviewHistoryDao;
    private final SrsEngine srsEngine;
    private final ExecutorProvider executorProvider;
    private final MutableLiveData<List<TopicEntity>> topics = new MutableLiveData<>();

    @Inject
    public VocabularyViewModel(VocabularyDao vocabularyDao, TopicDao topicDao,
                               ReviewHistoryDao reviewHistoryDao,
                               SrsEngine srsEngine, ExecutorProvider executorProvider) {
        this.vocabularyDao = vocabularyDao;
        this.topicDao = topicDao;
        this.reviewHistoryDao = reviewHistoryDao;
        this.srsEngine = srsEngine;
        this.executorProvider = executorProvider;
        rebuildSearchIndex();
        loadTopics();
    }

    public LiveData<List<VocabularyEntity>> getAllVocabularies() {
        return vocabularyDao.getAllVocabularies();
    }

    public LiveData<List<VocabularyEntity>> searchVocabularies(String rawQuery) {
        return vocabularyDao.searchVocabularies(toFtsQuery(rawQuery));
    }

    public LiveData<List<TopicEntity>> getTopics() {
        return topics;
    }

    public LiveData<VocabularyEntity> observeVocabulary(long vocabularyId) {
        return vocabularyDao.observeVocabularyById(vocabularyId);
    }

    public void updateVocabulary(VocabularyEntity entity) {
        if (entity == null) return;
        entity.setUpdatedAt(System.currentTimeMillis());
        executorProvider.getIoExecutor().execute(() -> {
            vocabularyDao.update(entity);
            vocabularyDao.upsertFts(toFtsEntity(entity));
        });
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
        executorProvider.getIoExecutor().execute(() -> {
            long id = vocabularyDao.insert(entity);
            entity.setVocabularyId(id);
            vocabularyDao.upsertFts(toFtsEntity(entity));
        });
    }

    public void deleteVocabulary(VocabularyEntity entity) {
        if (entity == null) return;
        executorProvider.getIoExecutor().execute(() -> {
            vocabularyDao.deleteFts(entity.getVocabularyId());
            vocabularyDao.delete(entity);
        });
    }

    public void recordReview(VocabularyEntity entity, boolean isCorrect) {
        if (entity == null) return;
        executorProvider.getIoExecutor().execute(() ->
                srsEngine.applyReview(entity, isCorrect, vocabularyDao, reviewHistoryDao));
    }

    public void moveToTopic(List<Long> vocabularyIds, Long topicId) {
        if (vocabularyIds == null || vocabularyIds.isEmpty()) return;
        executorProvider.getIoExecutor().execute(() ->
                vocabularyDao.updateTopicForIds(vocabularyIds, topicId, System.currentTimeMillis()));
    }

    public void createTopicAndMove(List<Long> vocabularyIds, String topicName) {
        if (vocabularyIds == null || vocabularyIds.isEmpty()
                || topicName == null || topicName.trim().isEmpty()) {
            return;
        }
        executorProvider.getIoExecutor().execute(() -> {
            long topicId = topicDao.insertTopic(new TopicEntity(topicName.trim()));
            vocabularyDao.updateTopicForIds(vocabularyIds, topicId, System.currentTimeMillis());
            loadTopics();
        });
    }

    private void loadTopics() {
        executorProvider.getIoExecutor().execute(() -> {
            List<TopicEntity> result = topicDao.getAllTopicsSync();
            executorProvider.postToMainThread(() -> topics.setValue(result));
        });
    }

    private void rebuildSearchIndex() {
        executorProvider.getIoExecutor().execute(() -> {
            vocabularyDao.clearFts();
            vocabularyDao.rebuildFts();
        });
    }

    private VocabularyFtsEntity toFtsEntity(VocabularyEntity entity) {
        return new VocabularyFtsEntity(
                entity.getVocabularyId(),
                entity.getWord(),
                entity.getMeaning(),
                entity.getPhonetic(),
                entity.getExampleSentence(),
                entity.getNote()
        );
    }

    private String toFtsQuery(String rawQuery) {
        if (rawQuery == null) return "";
        String cleaned = rawQuery.trim()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ");
        String[] tokens = cleaned.split("\\s+");
        StringBuilder builder = new StringBuilder();
        for (String token : tokens) {
            if (token.isEmpty()) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(token).append('*');
        }
        return builder.length() == 0 ? "*" : builder.toString();
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
