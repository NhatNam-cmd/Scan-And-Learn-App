package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "story")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val storyId: Long = 0,
    val title: String,
    val content: String, // Chuỗi JSON sinh ra từ AI
    val difficulty: String,
    val createdAt: Long = System.currentTimeMillis()
)