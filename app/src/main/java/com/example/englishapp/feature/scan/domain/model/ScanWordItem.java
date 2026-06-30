package com.example.englishapp.feature.scan.domain.model;

import androidx.annotation.NonNull;

public class ScanWordItem {

    @NonNull
    private final String word;

    private final boolean selected;

    private final boolean stopWord;

    private final int frequency;

    public ScanWordItem(
            @NonNull String word,
            boolean selected,
            boolean stopWord,
            int frequency
    ) {
        this.word = word;
        this.selected = selected;
        this.stopWord = stopWord;
        this.frequency = frequency;
    }

    @NonNull
    public String getWord() {
        return word;
    }

    public boolean isSelected() {
        return selected;
    }

    public boolean isStopWord() {
        return stopWord;
    }

    public int getFrequency() {
        return frequency;
    }

    public ScanWordItem copy(
            boolean selected
    ) {
        return new ScanWordItem(
                word,
                selected,
                stopWord,
                frequency
        );
    }
}