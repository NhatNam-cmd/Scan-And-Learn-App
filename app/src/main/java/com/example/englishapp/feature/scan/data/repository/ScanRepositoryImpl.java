package com.example.englishapp.feature.scan.data.repository;

import android.util.Log;
import com.example.englishapp.core.network.dictionary.dto.DefinitionDto;
import com.example.englishapp.core.network.dictionary.dto.DictionaryResponseDto;
import com.example.englishapp.core.network.dictionary.dto.MeaningDto;
import com.example.englishapp.core.network.dictionary.dto.PhoneticDto;
import androidx.lifecycle.MutableLiveData;
import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.database.entity.VocabularyFtsEntity;
import com.example.englishapp.core.network.dictionary.DictionaryService;
import com.example.englishapp.core.model.VocabularyLookup;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import retrofit2.Response;
import com.example.englishapp.core.network.dictionary.dto.DictionaryResponseDto;
@Singleton
public class ScanRepositoryImpl implements ScanRepository {

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
        resultLiveData.postValue((ApiResult) ApiResult.Loading.getInstance());

        executorProvider.getIoExecutor().execute(() -> {
            try {
                Log.d("LOOKUP", "Word = [" + word + "]");
                Response<List<DictionaryResponseDto>> response = dictionaryService.getWordData(word.trim().toLowerCase()).execute();

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    DictionaryResponseDto dto = response.body().get(0);

                    String meaning = "Chưa có định nghĩa";

                    String example = "";

                    String partOfSpeech = "";

                    String phonetic = dto.getPhonetic();
                    if (phonetic == null) {
                        phonetic = "";
                    }
                    String audioUrl = "";


//========================
// phonetic
//========================

                    if ((phonetic == null || phonetic.isEmpty())
                            && dto.getPhonetics() != null
                            && !dto.getPhonetics().isEmpty()) {

                        phonetic = dto.getPhonetics()
                                .get(0)
                                .getText();

                    }


//========================
// audio
//========================

                    if (dto.getPhonetics() != null) {

                        for (PhoneticDto p : dto.getPhonetics()) {

                            if (p.getAudio() != null
                                    && !p.getAudio().isEmpty()) {

                                audioUrl = p.getAudio();

                                break;

                            }

                        }

                    }


//========================
// meaning
//========================

                    if (dto.getMeanings() != null
                            && !dto.getMeanings().isEmpty()) {

                        MeaningDto meaningDto =
                                dto.getMeanings().get(0);

                        partOfSpeech =
                                meaningDto.getPartOfSpeech();
                        if (partOfSpeech == null) {
                            partOfSpeech = "";
                        }
                        if (meaningDto.getDefinitions() != null
                                && !meaningDto.getDefinitions().isEmpty()) {

                            DefinitionDto definition =
                                    meaningDto.getDefinitions().get(0);

                            meaning =
                                    definition.getDefinition();

                            example =
                                    definition.getExample();
                            if (example == null) {
                                example = "";
                            }
                        }

                    }

                    VocabularyLookup lookup =

                            new VocabularyLookup(

                                    dto.getWord(),

                                    meaning,

                                    phonetic,

                                    partOfSpeech,

                                    example,

                                    audioUrl

                            );

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
                            entity.getNote(),
                            entity.getSourceType(),
                            existing.getMasteryLevel(),
                            existing.isMastered(),
                            existing.getNextReviewDate(),
                            existing.getCreatedAt(),
                            System.currentTimeMillis()
                    );
                    vocabularyDao.update(updatedEntity);
                    vocabularyDao.upsertFts(toFtsEntity(updatedEntity));
                } else {
                    long id = vocabularyDao.insert(entity);
                    entity.setVocabularyId(id);
                    vocabularyDao.upsertFts(toFtsEntity(entity));
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
}
