package com.example.englishapp.core.model;

public class VocabularyLookup {

    private String word;

    private String meaning;

    private String phonetic;

    /*
     * New fields
     */

    private String partOfSpeech;

    private String example;

    private String audioUrl;

    public VocabularyLookup(
            String word,
            String meaning,
            String phonetic,
            String partOfSpeech,
            String example,
            String audioUrl
    ) {

        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.partOfSpeech = partOfSpeech;
        this.example = example;
        this.audioUrl = audioUrl;

    }

    //================================================
    // Getter
    //================================================

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getExample() {
        return example;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    //================================================
    // Setter
    //================================================

    public void setWord(String word) {
        this.word = word;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public void setPartOfSpeech(String partOfSpeech) {
        this.partOfSpeech = partOfSpeech;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

}