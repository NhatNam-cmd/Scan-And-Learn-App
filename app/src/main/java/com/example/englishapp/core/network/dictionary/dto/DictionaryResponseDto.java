package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class DictionaryResponseDto {
    @SerializedName("word")
    private String word;

    @SerializedName("phonetic")
    private String phonetic;

    @SerializedName("phonetics")
    private List<PhoneticDto> phonetics;

    @SerializedName("meanings")
    private List<MeaningDto> meanings;

    public DictionaryResponseDto() {
        this.word = "";
        this.phonetic = "";
        this.phonetics = Collections.emptyList();
        this.meanings = Collections.emptyList();
    }

    // Getters
    public String getWord() { return word; }
    public String getPhonetic() { return phonetic; }
    public List<PhoneticDto> getPhonetics() { return phonetics; }
    public List<MeaningDto> getMeanings() { return meanings; }

    // Setters
    public void setWord(String word) { this.word = word; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public void setPhonetics(List<PhoneticDto> phonetics) { this.phonetics = phonetics; }
    public void setMeanings(List<MeaningDto> meanings) {

        this.meanings = meanings;

    }
}
