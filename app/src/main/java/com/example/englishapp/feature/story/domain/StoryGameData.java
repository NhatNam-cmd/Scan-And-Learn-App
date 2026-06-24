package com.example.englishapp.feature.story.domain;

import java.util.ArrayList;
import java.util.List;

public class StoryGameData {
    private String title;
    private String story;
    private List<StoryBlank> blanks;
    private boolean offline;

    public StoryGameData() {
        blanks = new ArrayList<>();
    }

    public StoryGameData(String title, String story, List<StoryBlank> blanks, boolean offline) {
        this.title = title;
        this.story = story;
        this.blanks = blanks;
        this.offline = offline;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public List<StoryBlank> getBlanks() {
        return blanks;
    }

    public void setBlanks(List<StoryBlank> blanks) {
        this.blanks = blanks;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
