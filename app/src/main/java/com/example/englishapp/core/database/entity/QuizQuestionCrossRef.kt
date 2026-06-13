package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index

// Bảng trung gian N-N: Từ vựng nào xuất hiện trong Quiz nào và kết quả Đúng/Sai
@Entity(
    tableName = "quiz_question_cross_ref",
    primaryKeys = ["quizId", "vocabularyId"],
    indices = [Index("vocabularyId")]
)
data class QuizQuestionCrossRef(
    val quizId: Long,
    val vocabularyId: Long,
    val isCorrect: Boolean
)