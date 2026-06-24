package com.example.englishapp.feature.scan.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.ui.ApiResult;
import com.example.englishapp.data.local.entity.VocabularyEntity;
import com.example.englishapp.domain.model.VocabularyLookup;
import com.example.englishapp.feature.scan.data.repository.ScanRepositoryImpl;

import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScanViewModel extends ViewModel {

    private final ScanRepositoryImpl repository;

    private final MutableLiveData<ApiResult<VocabularyLookup>> _lookupResult = new MutableLiveData<>();
    public LiveData<ApiResult<VocabularyLookup>> lookupResult = _lookupResult;

    private final MutableLiveData<ApiResult<String>> _saveResult = new MutableLiveData<>();
    public LiveData<ApiResult<String>> saveResult = _saveResult;

    @Inject
    public ScanViewModel(ScanRepositoryImpl repository) {
        this.repository = repository;
    }

    public void lookupScannedWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            _lookupResult.setValue(ApiResult.Error.create("Từ vựng không được để trống"));
            return;
        }
        repository.lookupWord(word, _lookupResult);
    }

    public void saveScannedVocabulary(VocabularyEntity entity, boolean forceUpdate) {
        if (entity == null) {
            _saveResult.setValue(ApiResult.Error.create("Dữ liệu không hợp lệ"));
            return;
        }
        repository.checkAndSaveVocabulary(entity, forceUpdate, _saveResult);
    }

    public void resetSaveState() {
        _saveResult.setValue(null);
    }

    public void resetLookupState() {
        _lookupResult.setValue(null);
    }
}