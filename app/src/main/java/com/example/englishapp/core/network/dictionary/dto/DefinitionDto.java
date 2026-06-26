package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;

public class DefinitionDto {
    @SerializedName("definition")
    private String definition;
    
    @SerializedName("example")
    private String example;

    public DefinitionDto() {}

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
}
