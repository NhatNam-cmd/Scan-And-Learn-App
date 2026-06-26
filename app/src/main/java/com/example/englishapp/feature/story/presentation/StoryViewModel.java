package com.example.englishapp.feature.story.presentation;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.core.common.ExecutorProvider;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.data.StoryRepositoryImpl;
import com.example.englishapp.feature.story.domain.StoryGameData;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class StoryViewModel extends ViewModel {
    private final StoryRepositoryImpl repository;
    private final ExecutorProvider executorProvider;
    private final MutableLiveData<List<Long>> selectedIds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<StoryGameData> currentStory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> answers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> offlineMode = new MutableLiveData<>(false);

    @Inject
    public StoryViewModel(StoryRepositoryImpl repository, ExecutorProvider executorProvider) {
        this.repository = repository;
        this.executorProvider = executorProvider;
    }

    public LiveData<List<VocabularyEntity>> getWords() {
        return repository.getUnmasteredWords();
    }

    public LiveData<List<Long>> getSelectedIds() {
        return selectedIds;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<StoryGameData> getCurrentStory() {
        return currentStory;
    }

    public LiveData<List<String>> getAnswers() {
        return answers;
    }

    public LiveData<Integer> getScore() {
        return score;
    }

    public LiveData<Boolean> getOfflineMode() {
        return offlineMode;
    }

    public void toggleSelection(long id) {
        List<Long> ids = new ArrayList<>(selectedIds.getValue());
        if (ids.contains(id)) {
            ids.remove(id);
        } else if (ids.size() < 10) {
            ids.add(id);
        }
        selectedIds.setValue(ids);
    }

    public void generateStory() {
        List<Long> ids = new ArrayList<>(selectedIds.getValue());
        if (ids.size() < 5) {
            return;
        }
        currentStory.setValue(null);
        loading.setValue(true);
        executorProvider.getIoExecutor().execute(() -> {
            StoryGameData story = repository.generateStory(ids);
            List<String> emptyAnswers = new ArrayList<>();
            for (int i = 0; story != null && story.getBlanks() != null && i < story.getBlanks().size(); i++) {
                emptyAnswers.add("");
            }
            executorProvider.postToMainThread(() -> {
                answers.setValue(emptyAnswers);
                offlineMode.setValue(story != null && story.isOffline());
                loading.setValue(false);
                currentStory.setValue(story);
            });
        });
    }

    public void fillNextBlank(String word) {
        List<String> current = new ArrayList<>(answers.getValue());
        for (int i = 0; i < current.size(); i++) {
            if (current.get(i) == null || current.get(i).isEmpty()) {
                current.set(i, word);
                answers.setValue(current);
                return;
            }
        }
    }

    public void clearBlank(int index) {
        List<String> current = new ArrayList<>(answers.getValue());
        if (index >= 0 && index < current.size()) {
            current.set(index, "");
            answers.setValue(current);
        }
    }

    public void submitAnswers() {
        StoryGameData story = currentStory.getValue();
        List<String> currentAnswers = new ArrayList<>(answers.getValue());
        int correct = 0;
        if (story != null && story.getBlanks() != null) {
            for (int i = 0; i < story.getBlanks().size() && i < currentAnswers.size(); i++) {
                if (story.getBlanks().get(i).getWord().equalsIgnoreCase(currentAnswers.get(i))) {
                    correct++;
                }
            }
        }
        score.setValue(correct);
        executorProvider.getIoExecutor().execute(() -> repository.updateReviewProgress(story, currentAnswers));
    }

    public void prepareNewStory() {
        selectedIds.setValue(new ArrayList<>());
        currentStory.setValue(null);
        answers.setValue(new ArrayList<>());
        score.setValue(0);
        offlineMode.setValue(false);
    }
}
