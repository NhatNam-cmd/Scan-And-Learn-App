package com.example.englishapp.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.util.List;

@Dao
public interface VocabularyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertVocabulary(VocabularyEntity vocabulary);

    @Update
    void updateVocabulary(VocabularyEntity vocabulary);

    @Query("SELECT * FROM vocabulary WHERE vocabularyId = :id")
    @Nullable
    VocabularyEntity getVocabularyById(long id);

    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    LiveData<List<VocabularyEntity>> getAllVocabularies();

    @Query("SELECT * FROM vocabulary WHERE nextReviewDate <= :currentDate AND isMastered = 0 ORDER BY nextReviewDate ASC LIMIT :limit")
    List<VocabularyEntity> getDueWords(long currentDate, int limit);

    @Query("SELECT * FROM vocabulary WHERE isMastered = 0 ORDER BY masteryLevel ASC, createdAt DESC")
    LiveData<List<VocabularyEntity>> getUnmasteredWords();

    @Query("SELECT * FROM vocabulary WHERE vocabularyId IN (:ids)")
    List<VocabularyEntity> getVocabularyByIds(List<Long> ids);

    @Query("SELECT COUNT(*) FROM vocabulary WHERE isMastered = 0")
    int countUnmasteredWords();
}
