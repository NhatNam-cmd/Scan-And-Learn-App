package com.example.englishapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.englishapp.core.database.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewHistory(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history WHERE vocabularyId = :vocabId ORDER BY reviewedAt DESC")
    fun getHistoryByVocabularyId(vocabId: Long): Flow<List<ReviewHistoryEntity>>
}