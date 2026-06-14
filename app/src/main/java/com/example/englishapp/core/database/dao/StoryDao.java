package com.example.englishapp.core.database.dao;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.core.database.entity.StoryVocabularyCrossRef;

import java.util.List;

@Dao
public interface StoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertStory(StoryEntity story);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertStoryVocabularyCrossRef(StoryVocabularyCrossRef crossRef);

    @Query("SELECT * FROM story WHERE storyId = :id")
    @Nullable
    StoryEntity getStoryById(long id);

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    LiveData<List<StoryEntity>> getAllStories();
}
