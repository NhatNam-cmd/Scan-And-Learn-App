package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vocabulary",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["topicId"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("word"), Index("topicId")]
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
    val note: String? = null,
    val sourceType: String, // "SCAN", "MANUAL", "TOPIC"

    // SRS Fields
    val masteryLevel: Int = 0,
    val isMastered: Boolean = false,
    val nextReviewDate: Long = 0L,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)