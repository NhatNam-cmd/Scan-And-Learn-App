package com.example.englishapp.feature.scan.domain.usecase;

import android.graphics.Bitmap;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class ScanTextUseCase {
    private final ScanRepository repository;

    @Inject
    public ScanTextUseCase(ScanRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<ApiResult<String>> execute(Bitmap bitmap) {
        return repository.scanTextFromImage(bitmap);
    }
}