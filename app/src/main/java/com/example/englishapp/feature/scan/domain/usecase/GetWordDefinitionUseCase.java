package com.example.englishapp.feature.scan.domain.usecase;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.model.Vocabulary;
import com.example.englishapp.feature.scan.domain.repository.ScanRepository;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

public class GetWordDefinitionUseCase {
    private final ScanRepository repository;

    @Inject
    public GetWordDefinitionUseCase(ScanRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<ApiResult<Vocabulary>> execute(String word) {
        return repository.getWordDefinition(word);
    }
}