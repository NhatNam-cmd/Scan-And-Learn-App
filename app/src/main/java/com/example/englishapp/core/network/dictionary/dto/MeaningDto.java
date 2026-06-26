package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Collections;

public class MeaningDto {
    @SerializedName("partOfSpeech")
    private String partOfSpeech;
    
    @SerializedName("definitions")
    private List<DefinitionDto> definitions;

    public MeaningDto() {
        this.definitions = Collections.emptyList();
    }

    public String getPartOfSpeech() { return partOfSpeech; }
    public void setPartOfSpeech(String partOfSpeech) { this.partOfSpeech = partOfSpeech; }

    public List<DefinitionDto> getDefinitions() { return definitions; }
    public void setDefinitions(List<DefinitionDto> definitions) { this.definitions = definitions; }
}
