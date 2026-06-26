package com.example.englishapp.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.englishapp.core.database.entity.TopicEntity;

import java.util.List;

@Dao
public interface TopicDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTopic(TopicEntity topic);

    @Query("SELECT * FROM vocabulary_topic ORDER BY createdAt DESC")
    LiveData<List<TopicEntity>> getAllTopics();

    @Query("SELECT * FROM vocabulary_topic ORDER BY name ASC")
    List<TopicEntity> getAllTopicsSync();
}
