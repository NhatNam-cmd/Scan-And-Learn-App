package com.example.englishapp.feature.story.data;

import androidx.lifecycle.LiveData;

import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.domain.StoryGameData;

import java.util.List;

public interface StoryRepository {
    LiveData<List<VocabularyEntity>> getUnmasteredWords();

    LiveData<List<StoryEntity>> getCompletedStories();

    StoryGameData generateStory(List<Long> vocabularyIds);

    void updateReviewProgress(StoryGameData story, List<String> answers);
}
