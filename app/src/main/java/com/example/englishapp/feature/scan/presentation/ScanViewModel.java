package com.example.englishapp.feature.scan.presentation;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.model.VocabularyLookup;
import com.example.englishapp.feature.scan.data.repository.ScanRepositoryImpl;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScanViewModel extends ViewModel {

    private final ScanRepositoryImpl repository;

    /*
     * Lookup Result
     */
    private final MutableLiveData<ApiResult<VocabularyLookup>> lookupResult =
            new MutableLiveData<>();

    /*
     * Save Result
     */
    private final MutableLiveData<ApiResult<String>> saveResult =
            new MutableLiveData<>();

    @Inject
    public ScanViewModel(@NonNull ScanRepositoryImpl repository) {
        this.repository = repository;
    }

    //====================================================
    // LiveData
    //====================================================

    public LiveData<ApiResult<VocabularyLookup>> getLookupResult() {
        return lookupResult;
    }

    public LiveData<ApiResult<String>> getSaveResult() {
        return saveResult;
    }

    //====================================================
    // Lookup
    //====================================================

    public void lookupScannedWord(String word) {

        if (word == null) {
            lookupResult.setValue(
                    ApiResult.Error.create("Word is null")
            );
            return;
        }

        word = word.trim();

        if (word.isEmpty()) {

            lookupResult.setValue(
                    ApiResult.Error.create("Word is empty")
            );

            return;
        }

        repository.lookupWord(
                word,
                lookupResult
        );
    }

    //====================================================
    // Save
    //====================================================

    public void saveVocabulary(VocabularyEntity entity) {

        saveVocabulary(
                entity,
                false
        );

    }

    public void saveVocabulary(
            VocabularyEntity entity,
            boolean forceUpdate
    ) {

        if (entity == null) {

            saveResult.setValue(
                    ApiResult.Error.create("Vocabulary is null")
            );

            return;
        }

        repository.checkAndSaveVocabulary(
                entity,
                forceUpdate,
                saveResult
        );

    }

    //====================================================
    // Reset State
    //====================================================

    public void clearLookupResult() {

        lookupResult.setValue(null);

    }

    public void clearSaveResult() {

        saveResult.setValue(null);

    }

}