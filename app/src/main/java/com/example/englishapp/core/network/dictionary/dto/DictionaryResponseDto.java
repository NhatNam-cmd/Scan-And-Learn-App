package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Collections;

public class DictionaryResponseDto {
    @SerializedName("word")
    private String word;
    
    @SerializedName("phonetic")
    private String phonetic;
    
    @SerializedName("meanings")
    private List<MeaningDto> meanings;

    public DictionaryResponseDto() {
        this.meanings = Collections.emptyList();
    }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }

    public List<MeaningDto> getMeanings() { return meanings; }
    public void setMeanings(List<MeaningDto> meanings) { this.meanings = meanings; }
}
