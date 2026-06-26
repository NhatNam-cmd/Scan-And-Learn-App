package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DictionaryWordDto {

    @SerializedName("word")
    private String word;

    @SerializedName("phonetic")
    private String phonetic;

    @SerializedName("meanings")
    private List<Meaning> meanings;

    public String getWord() { return word; }
    public String getPhonetic() { return phonetic; }
    public List<Meaning> getMeanings() { return meanings; }

    public static class Meaning {
        @SerializedName("definitions")
        private List<Definition> definitions;

        public List<Definition> getDefinitions() { return definitions; }
    }

    public static class Definition {
        @SerializedName("definition")
        private String definition;

        public String getDefinition() { return definition; }
    }
}