package com.example.englishapp.feature.scan.domain.repository;

import android.graphics.Bitmap;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.model.Vocabulary;

import java.util.concurrent.CompletableFuture;

public interface ScanRepository {
    CompletableFuture<ApiResult<String>> scanTextFromImage(Bitmap bitmap);
    CompletableFuture<ApiResult<Vocabulary>> getWordDefinition(String word);
    CompletableFuture<ApiResult<Void>> saveVocabulary(Vocabulary vocabulary);
}