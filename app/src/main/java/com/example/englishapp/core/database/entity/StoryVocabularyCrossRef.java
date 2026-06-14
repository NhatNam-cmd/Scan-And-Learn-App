package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Objects;

// Intermediate N-N table: which vocabulary appears in which Story
// Bảng trung gian N-N: Từ vựng nào xuất hiện trong Story nào
@Entity(
        tableName = "story_vocabulary_cross_ref",
        primaryKeys = {"storyId", "vocabularyId"},
        indices = {@Index("vocabularyId")}
)
public class StoryVocabularyCrossRef {

    private long storyId;
    private long vocabularyId;

    public StoryVocabularyCrossRef(long storyId, long vocabularyId) {
        this.storyId = storyId;
        this.vocabularyId = vocabularyId;
    }

    // Getters
    public long getStoryId() { return storyId; }
    public long getVocabularyId() { return vocabularyId; }

    // Setters
    public void setStoryId(long storyId) { this.storyId = storyId; }
    public void setVocabularyId(long vocabularyId) { this.vocabularyId = vocabularyId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoryVocabularyCrossRef that = (StoryVocabularyCrossRef) o;
        return storyId == that.storyId
                && vocabularyId == that.vocabularyId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storyId, vocabularyId);
    }

    @NonNull
    @Override
    public String toString() {
        return "StoryVocabularyCrossRef{" +
                "storyId=" + storyId +
                ", vocabularyId=" + vocabularyId +
                '}';
    }
}
