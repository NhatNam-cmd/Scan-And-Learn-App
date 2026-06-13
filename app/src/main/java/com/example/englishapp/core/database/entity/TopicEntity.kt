package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_topic")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true)
    val topicId: Long = 0,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)