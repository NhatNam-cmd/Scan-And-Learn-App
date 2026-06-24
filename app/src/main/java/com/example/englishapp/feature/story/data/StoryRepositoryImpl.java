package com.example.englishapp.feature.story.data;

import androidx.lifecycle.LiveData;

import com.example.englishapp.core.ai.GeminiService;
import com.example.englishapp.core.ai.StoryFallbackBuilder;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class StoryRepositoryImpl implements StoryRepository {
    private final VocabularyDao vocabularyDao;
    private final GeminiService geminiService;
    private final StoryFallbackBuilder fallbackBuilder;

    @Inject
    public StoryRepositoryImpl(VocabularyDao vocabularyDao) {
        this.vocabularyDao = vocabularyDao;
        this.geminiService = new GeminiService();
        this.fallbackBuilder = new StoryFallbackBuilder();
    }

    @Override
    public LiveData<List<VocabularyEntity>> getUnmasteredWords() {
        return vocabularyDao.getUnmasteredWords();
    }

    @Override
    public StoryGameData generateStory(List<Long> vocabularyIds) {
        List<VocabularyEntity> words = getWordsInSelectionOrder(vocabularyIds);
        try {
            return geminiService.generateStory(words);
        } catch (Exception ignored) {
            return fallbackBuilder.buildOfflineStory(words);
        }
    }

    @Override
    public void updateReviewProgress(StoryGameData story, List<String> answers) {
        if (story == null || story.getBlanks() == null || answers == null) {
            return;
        }
        for (int i = 0; i < story.getBlanks().size() && i < answers.size(); i++) {
            StoryBlank blank = story.getBlanks().get(i);
            String answer = answers.get(i);
            if (blank.getWord() != null && blank.getWord().equalsIgnoreCase(answer)) {
                VocabularyEntity entity = vocabularyDao.getVocabularyById(blank.getVocabularyId());
                if (entity != null) {
                    entity.setMasteryLevel(Math.max(1, entity.getMasteryLevel() + 1));
                    entity.setMastered(entity.getMasteryLevel() >= 5);
                    entity.setNextReviewDate(System.currentTimeMillis() + 24L * 60L * 60L * 1000L);
                    entity.setUpdatedAt(System.currentTimeMillis());
                    vocabularyDao.updateVocabulary(entity);
                }
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
}
