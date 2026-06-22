package com.example.englishapp.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.englishapp.core.database.entity.ReviewHistoryEntity;

import java.util.List;

@Dao
public interface ReviewHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReviewHistory(ReviewHistoryEntity history);

    @Query("SELECT * FROM review_history WHERE vocabularyId = :vocabId ORDER BY reviewedAt DESC")
    LiveData<List<ReviewHistoryEntity>> getHistoryByVocabularyId(long vocabId);

    @Query("SELECT DISTINCT reviewedAt / 86400000 FROM review_history ORDER BY reviewedAt / 86400000 DESC")
    List<Long> getReviewedDaysDesc();
}
