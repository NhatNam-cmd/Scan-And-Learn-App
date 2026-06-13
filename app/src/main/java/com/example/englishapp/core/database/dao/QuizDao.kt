package com.example.englishapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.englishapp.core.database.entity.QuizQuestionCrossRef
import com.example.englishapp.core.database.entity.QuizSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizSession(session: QuizSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizQuestionCrossRef(crossRef: QuizQuestionCrossRef)

    @Query("SELECT * FROM quiz_session WHERE quizId = :id")
    suspend fun getQuizSessionById(id: Long): QuizSessionEntity?

    @Query("SELECT * FROM quiz_session ORDER BY completedAt DESC")
    fun getAllQuizSessions(): Flow<List<QuizSessionEntity>>
}