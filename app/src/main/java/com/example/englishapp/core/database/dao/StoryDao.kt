package com.example.englishapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.englishapp.core.database.entity.StoryEntity
import com.example.englishapp.core.database.entity.StoryVocabularyCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStoryVocabularyCrossRef(crossRef: StoryVocabularyCrossRef)

    @Query("SELECT * FROM story WHERE storyId = :id")
    suspend fun getStoryById(id: Long): StoryEntity?

    @Query("SELECT * FROM story ORDER BY createdAt DESC")
    fun getAllStories(): Flow<List<StoryEntity>>
}