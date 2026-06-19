package com.example.englishapp.feature.scan.domain.usecase;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.model.Vocabulary;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class SaveWordUseCase {
    private final ScanRepository repository;

    @Inject
    public SaveWordUseCase(ScanRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<ApiResult<Void>> execute(Vocabulary vocabulary) {
        return repository.saveVocabulary(vocabulary);
    }
}