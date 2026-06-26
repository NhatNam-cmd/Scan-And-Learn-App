package com.example.englishapp.feature.scan.model;

import java.io.Serializable;

public class WordCandidate implements Serializable {

    private final String word;

    private final float confidence;

    public WordCandidate(String word,float confidence){

        this.word=word;
        this.confidence=confidence;

    }

    public String getWord(){

        return word;

    }

    public float getConfidence(){

        return confidence;

    }

}