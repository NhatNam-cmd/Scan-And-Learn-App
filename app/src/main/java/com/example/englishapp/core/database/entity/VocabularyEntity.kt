package com.project.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary",
    indices = [Index(value = ["word", "sourceType"], unique = true)]
)
data class VocabularyEntity(
    @PrimaryKey(autoGenerate = true)
    val vocabularyId: Long = 0,

    val topicId: Long? = null,
    val word: String,
    val meaning: String,
    val phonetic: String? = null,
    val exampleSentence: String? = null,
    val imagePath: String? = null,
    val audioPath: String? = null,
    val sourceType: String,

    val masteryLevel: Int = 0,
    val isMastered: Boolean = false,
    val nextReviewDate: Long = 0L,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)