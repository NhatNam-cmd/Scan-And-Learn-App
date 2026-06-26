package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Fts4;
import androidx.room.FtsOptions;
import androidx.room.PrimaryKey;

@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "vocabulary_fts")
public class VocabularyFtsEntity {
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    private long rowId;

    @NonNull
    private String word;

    @NonNull
    private String meaning;

    @Nullable
    private String phonetic;

    @Nullable
    private String exampleSentence;

    @Nullable
    private String note;

    public VocabularyFtsEntity(long rowId, @NonNull String word, @NonNull String meaning,
                               @Nullable String phonetic, @Nullable String exampleSentence,
                               @Nullable String note) {
        this.rowId = rowId;
        this.word = word;
        this.meaning = meaning;
        this.phonetic = phonetic;
        this.exampleSentence = exampleSentence;
        this.note = note;
    }

    public long getRowId() { return rowId; }
    @NonNull public String getWord() { return word; }
    @NonNull public String getMeaning() { return meaning; }
    @Nullable public String getPhonetic() { return phonetic; }
    @Nullable public String getExampleSentence() { return exampleSentence; }
    @Nullable public String getNote() { return note; }

    public void setRowId(long rowId) { this.rowId = rowId; }
    public void setWord(@NonNull String word) { this.word = word; }
    public void setMeaning(@NonNull String meaning) { this.meaning = meaning; }
    public void setPhonetic(@Nullable String phonetic) { this.phonetic = phonetic; }
    public void setExampleSentence(@Nullable String exampleSentence) { this.exampleSentence = exampleSentence; }
    public void setNote(@Nullable String note) { this.note = note; }
}
