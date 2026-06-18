package com.example.englishapp.core.ai;

import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;

import java.util.ArrayList;
import java.util.List;
public class StoryFallbackBuilder {
    public StoryGameData buildOfflineStory(List<VocabularyEntity> words) {
        StringBuilder story = new StringBuilder();
        List<StoryBlank> blanks = new ArrayList<>();
        for (int i = 0; i < words.size(); i++) {
            VocabularyEntity vocabulary = words.get(i);
            int index = i + 1;
            String sentence = buildSentence(vocabulary, index);
            if (story.length() > 0) {
                story.append("\n\n");
            }
            story.append(sentence);
            blanks.add(new StoryBlank(index, vocabulary.getVocabularyId(), vocabulary.getWord(), vocabulary.getMeaning()));
        }
        return new StoryGameData("Offline Practice Story", story.toString(), blanks, true);
    }

    private String buildSentence(VocabularyEntity vocabulary, int index) {
        String example = vocabulary.getExampleSentence();
        String blank = "[BLANK_" + index + "]";
        if (example == null || example.trim().isEmpty()) {
            return "Today I learned how to use " + blank + " in a clear sentence.";
        }
        String word = vocabulary.getWord();
        return example.replaceAll("(?i)\\b" + java.util.regex.Pattern.quote(word) + "\\b", blank);
    }
}

