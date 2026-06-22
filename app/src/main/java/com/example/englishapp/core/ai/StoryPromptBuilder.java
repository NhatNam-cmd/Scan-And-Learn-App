package com.example.englishapp.core.ai;

import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.util.List;

public class StoryPromptBuilder {
    public String buildPrompt(List<VocabularyEntity> words) {
        int wordCount = words.size();
        StringBuilder builder = new StringBuilder();
        builder.append("You are an English learning story generator for Vietnamese learners. ");
        builder.append("Create a short, coherent English story using EXACTLY the following ")
               .append(wordCount).append(" target words as fill-in-the-blank tokens. ");
        builder.append("STRICT RULES: ");
        builder.append("(1) The story field must contain EXACTLY these tokens in order: ");
        for (int i = 1; i <= wordCount; i++) {
            builder.append("[BLANK_").append(i).append("]");
            if (i < wordCount) builder.append(", ");
        }
        builder.append(". ");
        builder.append("(2) Do NOT add extra [BLANK_N] tokens beyond the ones listed above. ");
        builder.append("(3) The blanks array must have EXACTLY ").append(wordCount).append(" items. ");
        builder.append("(4) Return ONLY valid JSON with keys: title (string), story (string), blanks (array). ");
        builder.append("(5) Each blank item: { \"index\": N, \"word\": \"...\", \"meaning\": \"...\" }. ");
        builder.append("Target words: ");
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

