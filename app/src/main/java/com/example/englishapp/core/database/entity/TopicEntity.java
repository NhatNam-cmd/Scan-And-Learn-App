package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "vocabulary_topic")
public class TopicEntity {

    @PrimaryKey(autoGenerate = true)
    private long topicId;

    @NonNull
    private String name;

    @Nullable
    private String description;

    private long createdAt;

    public TopicEntity(long topicId, @NonNull String name, @Nullable String description, long createdAt) {
        this.topicId = topicId;
        this.name = name;
        this.description = description;
        this.createdAt = createdAt;
    }

    // Convenience constructor with defaults
    @Ignore
    public TopicEntity(@NonNull String name) {
        this(0, name, null, System.currentTimeMillis());
    }

    // Getters
    public long getTopicId() { return topicId; }
    @NonNull public String getName() { return name; }
    @Nullable public String getDescription() { return description; }
    public long getCreatedAt() { return createdAt; }

    // Setters
    public void setTopicId(long topicId) { this.topicId = topicId; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setDescription(@Nullable String description) { this.description = description; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicEntity that = (TopicEntity) o;
        return topicId == that.topicId
                && createdAt == that.createdAt
                && Objects.equals(name, that.name)
                && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicId, name, description, createdAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "TopicEntity{" +
                "topicId=" + topicId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
