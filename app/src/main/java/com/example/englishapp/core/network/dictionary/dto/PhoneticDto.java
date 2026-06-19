package com.example.englishapp.core.network.dictionary.dto;

import com.google.gson.annotations.SerializedName;

public class PhoneticDto {
    @SerializedName("text")
    private String text;

    @SerializedName("audio")
    private String audio;

    public PhoneticDto() {
        this.text = "";
        this.audio = "";
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getAudio() { return audio; }
    public void setAudio(String audio) { this.audio = audio; }
}
