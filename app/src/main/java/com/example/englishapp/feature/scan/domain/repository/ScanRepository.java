package com.example.englishapp.feature.scan.domain.repository;

import androidx.lifecycle.MutableLiveData;

import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.model.VocabularyLookup;
import com.example.englishapp.core.common.ApiResult;

public interface ScanRepository {

    void lookupWord(String word, MutableLiveData<ApiResult<VocabularyLookup>> resultLiveData);

    void checkAndSaveVocabulary(VocabularyEntity entity, boolean forceUpdate, MutableLiveData<ApiResult<String>> saveStateLiveData);
}