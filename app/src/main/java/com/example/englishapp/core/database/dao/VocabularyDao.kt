package com.project.englishapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVocabulary(vocabulary: VocabularyEntity): Long

    @Update
    suspend fun updateVocabulary(vocabulary: VocabularyEntity)

    @Query("SELECT * FROM vocabulary WHERE vocabularyId = :id")
    suspend fun getVocabularyById(id: Long): VocabularyEntity?
    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    fun getAllVocabularies(): Flow<List<VocabularyEntity>>
    @Query("SELECT * FROM vocabulary WHERE nextReviewDate <= :currentDate AND isMastered = 0 ORDER BY nextReviewDate ASC LIMIT :limit")
    suspend fun getDueWords(currentDate: Long, limit: Int): List<VocabularyEntity>
}