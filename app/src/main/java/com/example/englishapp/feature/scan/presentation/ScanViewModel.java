package com.example.englishapp.feature.scan.presentation;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.model.Vocabulary;
import com.example.englishapp.feature.scan.domain.usecase.GetWordDefinitionUseCase;
import com.example.englishapp.feature.scan.domain.usecase.SaveWordUseCase;
import com.example.englishapp.feature.scan.domain.usecase.ScanTextUseCase;
import com.example.englishapp.feature.scan.presentation.state.ScanUiEvent;
import com.example.englishapp.feature.scan.presentation.state.ScanUiState;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ScanViewModel extends ViewModel {

    private final ScanTextUseCase scanTextUseCase;
    private final GetWordDefinitionUseCase getWordDefinitionUseCase;
    private final SaveWordUseCase saveWordUseCase;

    private final MutableLiveData<ScanUiState> _state = new MutableLiveData<>(ScanUiState.Idle.INSTANCE);
    public LiveData<ScanUiState> getState() { return _state; }

    private String lastScannedWord = null;

    @Inject
    public ScanViewModel(ScanTextUseCase scanTextUseCase,
                         GetWordDefinitionUseCase getWordDefinitionUseCase,
                         SaveWordUseCase saveWordUseCase) {
        this.scanTextUseCase = scanTextUseCase;
        this.getWordDefinitionUseCase = getWordDefinitionUseCase;
        this.saveWordUseCase = saveWordUseCase;
    }

    public void handleEvent(ScanUiEvent event) {
        if (event instanceof ScanUiEvent.ScanImage) {
            scanImage(((ScanUiEvent.ScanImage) event).getBitmap());
        } else if (event instanceof ScanUiEvent.LookupWord) {
            lookupWord(((ScanUiEvent.LookupWord) event).getWord());
        } else if (event instanceof ScanUiEvent.SaveWord) {
            saveWord();
        } else if (event instanceof ScanUiEvent.ClearResult) {
            clearResult();
        }
    }

    private void scanImage(Bitmap bitmap) {
        _state.setValue(ScanUiState.Loading.INSTANCE);

        scanTextUseCase.execute(bitmap)
                .thenAccept(result -> {
                    if (result instanceof ApiResult.Success) {
                        String text = ((ApiResult.Success<String>) result).getData();
                        lastScannedWord = text;
                        _state.postValue(new ScanUiState.ScannedText(text));
                    } else if (result instanceof ApiResult.Error) {
                        String message = ((ApiResult.Error) result).getMessage();
                        _state.postValue(new ScanUiState.Error(message));
                    }
                })
                .exceptionally(throwable -> {
                    _state.postValue(new ScanUiState.Error("Scan failed: " + throwable.getMessage()));
                    return null;
                });
    }

    private void lookupWord(String word) {
        _state.setValue(ScanUiState.Loading.INSTANCE);

        getWordDefinitionUseCase.execute(word)
                .thenAccept(result -> {
                    if (result instanceof ApiResult.Success) {
                        Vocabulary vocabulary = ((ApiResult.Success<Vocabulary>) result).getData();
                        _state.postValue(new ScanUiState.WordFound(vocabulary));
                    } else if (result instanceof ApiResult.Error) {
                        String message = ((ApiResult.Error) result).getMessage();
                        _state.postValue(new ScanUiState.Error(message));
                    }
                })
                .exceptionally(throwable -> {
                    _state.postValue(new ScanUiState.Error("Lookup failed: " + throwable.getMessage()));
                    return null;
                });
    }

    private void saveWord() {
        ScanUiState currentState = _state.getValue();
        if (currentState instanceof ScanUiState.WordFound) {
            Vocabulary vocabulary = ((ScanUiState.WordFound) currentState).getVocabulary();

            saveWordUseCase.execute(vocabulary)
                    .thenAccept(result -> {
                        if (result instanceof ApiResult.Success) {
                            _state.postValue(new ScanUiState.WordSaved(vocabulary.getWord()));
                        } else if (result instanceof ApiResult.Error) {
                            String message = ((ApiResult.Error) result).getMessage();
                            _state.postValue(new ScanUiState.Error(message));
                        }
                    })
                    .exceptionally(throwable -> {
                        _state.postValue(new ScanUiState.Error("Save failed: " + throwable.getMessage()));
                        return null;
                    });
        }
    }

    private void clearResult() {
        lastScannedWord = null;
        _state.setValue(ScanUiState.Idle.INSTANCE);
    }
}