package com.example.englishapp.core.ai;

import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StoryValidator {
    private static final Pattern BLANK_TOKEN_PATTERN = Pattern.compile("\\[BLANK_(\\d+)\\]");

    private final Gson gson = new Gson();

    public StoryGameData parseAndValidate(String json) {
        try {
            StoryGameData data = gson.fromJson(json, StoryGameData.class);
            if (data == null || isBlank(data.getTitle()) || isBlank(data.getStory())) {
                return null;
            }

            List<StoryBlank> blanks = data.getBlanks();
            if (blanks == null || blanks.isEmpty()) {
                return null;
            }

            List<Integer> tokenIndexes = extractTokenIndexes(data.getStory());
            if (tokenIndexes.size() != blanks.size()) {
                return null;
            }
            for (int i = 0; i < tokenIndexes.size(); i++) {
                if (tokenIndexes.get(i) != i + 1) {
                    return null;
                }
            }

            Map<Integer, StoryBlank> blanksByIndex = new HashMap<>();
            for (int i = 0; i < blanks.size(); i++) {
                StoryBlank blank = blanks.get(i);
                if (blank == null || isBlank(blank.getWord())) {
                    return null;
                }
                if (blank.getIndex() <= 0) {
                    blank.setIndex(i + 1);
                }
                if (blank.getIndex() < 1 || blank.getIndex() > blanks.size()
                        || blanksByIndex.containsKey(blank.getIndex())) {
                    return null;
                }
                blanksByIndex.put(blank.getIndex(), blank);
            }

            List<StoryBlank> orderedBlanks = new ArrayList<>();
            for (int i = 1; i <= blanks.size(); i++) {
                StoryBlank blank = blanksByIndex.get(i);
                if (blank == null) {
                    return null;
                }
                orderedBlanks.add(blank);
            }

            data.setBlanks(orderedBlanks);
            return data;
        } catch (JsonSyntaxException | NumberFormatException exception) {
            return null;
        }
    }

    private List<Integer> extractTokenIndexes(String story) {
        List<Integer> indexes = new ArrayList<>();
        Matcher matcher = BLANK_TOKEN_PATTERN.matcher(story);
        while (matcher.find()) {
            indexes.add(Integer.parseInt(matcher.group(1)));
        }
        return indexes;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
