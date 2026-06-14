package com.example.englishapp.core.database.dao;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;

import java.util.List;

@Dao
public interface QuizDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertQuizSession(QuizSessionEntity session);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuizQuestionCrossRef(QuizQuestionCrossRef crossRef);

    @Query("SELECT * FROM quiz_session WHERE quizId = :id")
    @Nullable
    QuizSessionEntity getQuizSessionById(long id);

    @Query("SELECT * FROM quiz_session ORDER BY completedAt DESC")
    LiveData<List<QuizSessionEntity>> getAllQuizSessions();
}
