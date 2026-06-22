package com.example.englishapp.core.ai;

import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.List;

public class StoryValidator {
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
            List<StoryBlank> validBlanks = new ArrayList<>();
            for (int i = 0; i < blanks.size(); i++) {
                StoryBlank blank = blanks.get(i);
                if (blank == null || isBlank(blank.getWord())) {
                    // Bỏ qua blank không hợp lệ thay vì reject cả story
                    continue;
                }
                int expectedIndex = i + 1;
                if (!data.getStory().contains("[BLANK_" + expectedIndex + "]")) {
                    // Token không có trong story — bỏ qua blank này,
                    // renderer sẽ tự dọn sạch token thừa trong text.
                    continue;
                }
                if (blank.getIndex() <= 0) {
                    blank.setIndex(expectedIndex);
                }
                validBlanks.add(blank);
            }
            if (validBlanks.isEmpty()) {
                return null;
            }
            data.setBlanks(validBlanks);
            return data;
        } catch (JsonSyntaxException exception) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

