package com.example.englishapp.core.database.entity

import androidx.room.Entity
import androidx.room.Index

// Bảng trung gian N-N: Từ vựng nào xuất hiện trong Story nào
@Entity(
    tableName = "story_vocabulary_cross_ref",
    primaryKeys = ["storyId", "vocabularyId"],
    indices = [Index("vocabularyId")]
)
data class StoryVocabularyCrossRef(
    val storyId: Long,
    val vocabularyId: Long
)