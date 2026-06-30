package com.example.englishapp.feature.story.data;

import androidx.lifecycle.LiveData;

import com.example.englishapp.core.ai.GeminiService;
import com.example.englishapp.core.ai.StoryFallbackBuilder;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.StoryDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.core.database.entity.StoryVocabularyCrossRef;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.srs.SrsEngine;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class StoryRepositoryImpl implements StoryRepository {
    private final VocabularyDao vocabularyDao;
    private final StoryDao storyDao;
    private final ReviewHistoryDao reviewHistoryDao;
    private final SrsEngine srsEngine;
    private final GeminiService geminiService;
    private final StoryFallbackBuilder fallbackBuilder;
    private final Gson gson;

    @Inject
    public StoryRepositoryImpl(VocabularyDao vocabularyDao, StoryDao storyDao, ReviewHistoryDao reviewHistoryDao,
                               SrsEngine srsEngine) {
        this.vocabularyDao = vocabularyDao;
        this.storyDao = storyDao;
        this.reviewHistoryDao = reviewHistoryDao;
        this.srsEngine = srsEngine;
        this.geminiService = new GeminiService();
        this.fallbackBuilder = new StoryFallbackBuilder();
        this.gson = new Gson();
    }

    @Override
    public LiveData<List<VocabularyEntity>> getUnmasteredWords() {
        return vocabularyDao.getUnmasteredWords();
    }

    @Override
    public LiveData<List<StoryEntity>> getCompletedStories() {
        return storyDao.getAllStories();
    }

    @Override
    public StoryGameData generateStory(List<Long> vocabularyIds) {
        List<VocabularyEntity> words = getWordsInSelectionOrder(vocabularyIds);
        StoryGameData story;
        try {
            story = geminiService.generateStory(words);
        } catch (Exception ignored) {
            story = fallbackBuilder.buildOfflineStory(words);
        }
        return story;
    }

    @Override
    public void updateReviewProgress(StoryGameData story, List<String> answers) {
        if (story == null || story.getBlanks() == null || answers == null) {
            return;
        }
        saveCompletedStory(story);
        for (int i = 0; i < story.getBlanks().size() && i < answers.size(); i++) {
            StoryBlank blank = story.getBlanks().get(i);
            String answer = answers.get(i);
            boolean isCorrect = blank.getWord() != null && blank.getWord().equalsIgnoreCase(answer);
            VocabularyEntity entity = vocabularyDao.getVocabularyById(blank.getVocabularyId());
            if (entity != null) {
                srsEngine.applyReview(entity, isCorrect, vocabularyDao, reviewHistoryDao);
            }
        }
    }

    private List<VocabularyEntity> getWordsInSelectionOrder(List<Long> vocabularyIds) {
        List<VocabularyEntity> words = vocabularyDao.getVocabularyByIds(vocabularyIds);
        Map<Long, VocabularyEntity> wordsById = new HashMap<>();
        for (VocabularyEntity word : words) {
            wordsById.put(word.getVocabularyId(), word);
        }

        List<VocabularyEntity> orderedWords = new ArrayList<>();
        for (Long id : vocabularyIds) {
            VocabularyEntity word = wordsById.get(id);
            if (word != null) {
                orderedWords.add(word);
            }
        }
        return orderedWords;
    }

    private void saveCompletedStory(StoryGameData story) {
        if (story == null || story.getTitle() == null || story.getStory() == null) {
            return;
        }
        if (story.getStoryId() > 0) {
            return;
        }

        StoryEntity entity = new StoryEntity(
                story.getTitle(),
                gson.toJson(story),
                story.isOffline() ? "OFFLINE" : "AI"
        );
        long storyId = storyDao.insertStory(entity);
        story.setStoryId(storyId);

        if (story.getBlanks() == null) {
            return;
        }
        for (StoryBlank blank : story.getBlanks()) {
            if (blank != null && blank.getVocabularyId() > 0) {
                storyDao.insertStoryVocabularyCrossRef(
                        new StoryVocabularyCrossRef(storyId, blank.getVocabularyId())
                );
            }
        }
    }
}
