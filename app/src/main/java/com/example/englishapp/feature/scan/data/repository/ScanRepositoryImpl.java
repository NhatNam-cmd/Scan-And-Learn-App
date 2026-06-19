package com.example.englishapp.feature.scan.data.repository;

import android.graphics.Bitmap;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.mapper.DictionaryMapper;
import com.example.englishapp.core.model.Vocabulary;
import com.example.englishapp.core.network.dictionary.DictionaryService;
import com.example.englishapp.core.network.dictionary.dto.DictionaryResponseDto;
import com.example.englishapp.core.service.OcrManager;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

@Singleton
public class ScanRepositoryImpl implements ScanRepository {

    private final OcrManager ocrManager;
    private final DictionaryService dictionaryService;
    private final VocabularyDao vocabularyDao;

    @Inject
    public ScanRepositoryImpl(OcrManager ocrManager,
                              DictionaryService dictionaryService,
                              VocabularyDao vocabularyDao) {
        this.ocrManager = ocrManager;
        this.dictionaryService = dictionaryService;
        this.vocabularyDao = vocabularyDao;
    }

    @Override
    public CompletableFuture<ApiResult<String>> scanTextFromImage(Bitmap bitmap) {
        CompletableFuture<ApiResult<String>> future = new CompletableFuture<>();

        ocrManager.recognizeText(bitmap)
                .thenAccept(result -> {
                    if (result != null && !result.isEmpty()) {
                        future.complete(ApiResult.success(result));
                    } else {
                        future.complete(ApiResult.error("No text found in image"));
                    }
                })
                .exceptionally(throwable -> {
                    future.complete(ApiResult.error("OCR failed: " + throwable.getMessage()));
                    return null;
                });

        return future;
    }

    @Override
    public CompletableFuture<ApiResult<Vocabulary>> getWordDefinition(String word) {
        CompletableFuture<ApiResult<Vocabulary>> future = new CompletableFuture<>();

        try {
            Response<List<DictionaryResponseDto>> response =
                    dictionaryService.lookupWord(word.trim()).execute();

            if (response.isSuccessful() && response.body() != null) {
                List<DictionaryResponseDto> dtos = response.body();
                if (!dtos.isEmpty()) {
                    Vocabulary vocabulary = DictionaryMapper.mapToVocabulary(dtos.get(0));
                    if (vocabulary != null) {
                        future.complete(ApiResult.success(vocabulary));
                    } else {
                        future.complete(ApiResult.error("Failed to parse word data"));
                    }
                } else {
                    future.complete(ApiResult.error("Word '" + word + "' not found"));
                }
            } else {
                future.complete(ApiResult.error("Network error: " + response.code()));
            }
        } catch (Exception e) {
            future.complete(ApiResult.error("Error: " + e.getMessage()));
        }

        return future;
    }

    @Override
    public CompletableFuture<ApiResult<Void>> saveVocabulary(Vocabulary vocabulary) {
        CompletableFuture<ApiResult<Void>> future = new CompletableFuture<>();

        try {
            // Kiểm tra từ đã tồn tại
            List<VocabularyEntity> existing = vocabularyDao.getAllVocabularies().getValue();
            if (existing != null) {
                for (VocabularyEntity entity : existing) {
                    if (entity.getWord().equalsIgnoreCase(vocabulary.getWord())) {
                        future.complete(ApiResult.error(
                                "Word '" + vocabulary.getWord() + "' already exists"
                        ));
                        return future;
                    }
                }
            }

            VocabularyEntity entity = vocabulary.toEntity();
            long id = vocabularyDao.insertVocabulary(entity);

            if (id > 0) {
                future.complete(ApiResult.success(null));
            } else {
                future.complete(ApiResult.error("Failed to save vocabulary"));
            }
        } catch (Exception e) {
            future.complete(ApiResult.error("Save error: " + e.getMessage()));
        }

        return future;
    }
}