package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyEntity::class,
            parentColumns = ["vocabularyId"],
            childColumns = ["vocabularyId"],
            onDelete = ForeignKey.CASCADE // Xóa từ thì xóa luôn lịch sử ôn tập
        )
    ],
    indices = [Index("vocabularyId")]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val reviewId: Long = 0,
    val vocabularyId: Long,
    val isCorrect: Boolean,
    val oldLevel: Int,
    val newLevel: Int,
    val reviewedAt: Long = System.currentTimeMillis()
)