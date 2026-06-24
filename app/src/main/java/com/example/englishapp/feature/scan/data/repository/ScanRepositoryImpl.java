package com.example.englishapp.feature.scan.data.repository;

import androidx.lifecycle.MutableLiveData;
import com.example.englishapp.core.ui.ApiResult;
import com.example.englishapp.core.utils.ExecutorProvider;
import com.example.englishapp.data.local.dao.VocabularyDao;
import com.example.englishapp.data.local.entity.VocabularyEntity;
import com.example.englishapp.data.remote.api.DictionaryService;
import com.example.englishapp.data.remote.dto.DictionaryWordDto;
import com.example.englishapp.domain.model.VocabularyLookup;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Response;

@Singleton
public class ScanRepositoryImpl {

    private final DictionaryService dictionaryService;
    private final VocabularyDao vocabularyDao;
    private final ExecutorProvider executorProvider;

    @Inject
    public ScanRepositoryImpl(DictionaryService dictionaryService,
                              VocabularyDao vocabularyDao,
                              ExecutorProvider executorProvider) {
        this.dictionaryService = dictionaryService;
        this.vocabularyDao = vocabularyDao;
        this.executorProvider = executorProvider;
    }

    public void lookupWord(String word, MutableLiveData<ApiResult<VocabularyLookup>> resultLiveData) {
        resultLiveData.setValue(ApiResult.Loading.getInstance());

        executorProvider.getIoExecutor().execute(() -> {
            try {
                Response<List<DictionaryWordDto>> response = dictionaryService.getWordData(word.trim().toLowerCase()).execute();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    DictionaryWordDto dto = response.body().get(0);

                    String meaning = "Chưa có định nghĩa";
                    if (!dto.getMeanings().isEmpty() && !dto.getMeanings().get(0).getDefinitions().isEmpty()) {
                        meaning = dto.getMeanings().get(0).getDefinitions().get(0).getDefinition();
                    }

                    VocabularyLookup lookup = new VocabularyLookup(dto.getWord(), meaning, dto.getPhonetic());

                    executorProvider.postToMainThread(() ->
                            resultLiveData.setValue(ApiResult.Success.create(lookup))
                    );
                } else {
                    executorProvider.postToMainThread(() ->
                            resultLiveData.setValue(ApiResult.Error.create("Không tìm thấy từ vựng này trong hệ thống."))
                    );
                }
            } catch (Exception e) {
                executorProvider.postToMainThread(() ->
                        resultLiveData.setValue(ApiResult.Error.create("Lỗi kết nối: " + e.getMessage()))
                );
            }
        });
    }

    public void checkAndSaveVocabulary(VocabularyEntity entity, boolean forceUpdate, MutableLiveData<ApiResult<String>> saveStateLiveData) {
        saveStateLiveData.setValue(ApiResult.Loading.getInstance());

        executorProvider.getIoExecutor().execute(() -> {
            try {
                VocabularyEntity existing = vocabularyDao.findByWord(entity.getWord(), entity.getSourceType());

                if (existing != null && !forceUpdate) {
                    executorProvider.postToMainThread(() ->
                            saveStateLiveData.setValue(ApiResult.Fallback.getInstance())
                    );
                    return;
                }

                if (existing != null) {
                    VocabularyEntity updatedEntity = new VocabularyEntity(
                            existing.getVocabularyId(),
                            entity.getTopicId(),
                            existing.getWord(),
                            entity.getMeaning(),
                            entity.getPhonetic(),
                            entity.getExampleSentence(),
                            entity.getImagePath(),
                            entity.getAudioPath(),
                            entity.getSourceType(),
                            existing.getMasteryLevel(),
                            existing.isMastered(),
                            existing.getNextReviewDate(),
                            existing.getCreatedAt(),
                            System.currentTimeMillis()
                    );
                    vocabularyDao.update(updatedEntity);
                } else {
                    vocabularyDao.insert(entity);
                }

                executorProvider.postToMainThread(() ->
                        saveStateLiveData.setValue(ApiResult.Success.create("Lưu từ vựng thành công!"))
                );

            } catch (Exception e) {
                executorProvider.postToMainThread(() ->
                        saveStateLiveData.setValue(ApiResult.Error.create("Lỗi lưu cơ sở dữ liệu: " + e.getMessage()))
                );
            }
        });
    }
}