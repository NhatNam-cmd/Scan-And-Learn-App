package com.example.englishapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.englishapp.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: TopicEntity): Long

    @Query("SELECT * FROM vocabulary_topic ORDER BY createdAt DESC")
    fun getAllTopics(): Flow<List<TopicEntity>>
}