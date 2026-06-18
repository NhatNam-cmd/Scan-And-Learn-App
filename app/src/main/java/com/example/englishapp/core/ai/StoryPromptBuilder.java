package com.example.englishapp.core.ai;

import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.util.List;

public class StoryPromptBuilder {
    public String buildPrompt(List<VocabularyEntity> words) {
        StringBuilder builder = new StringBuilder();
        builder.append("Create a short English learning story for Vietnamese learners. ");
        builder.append("Use every target word exactly once as a blank token in the story. ");
        builder.append("Return only valid JSON with keys: title, story, blanks. ");
        builder.append("The story must contain [BLANK_1], [BLANK_2]... matching the blanks array order. ");
        builder.append("Each blank item must contain index, word, meaning. Target words: ");
        for (int i = 0; i < words.size(); i++) {
            VocabularyEntity word = words.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(word.getWord())
                    .append(" = ")
                    .append(word.getMeaning());
            if (i < words.size() - 1) {
                builder.append("; ");
            }
        }
        return builder.toString();
    }
}

