package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_session")
data class QuizSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val quizId: Long = 0,
    val score: Int,
    val totalQuestions: Int,
    val startedAt: Long,
    val completedAt: Long
)