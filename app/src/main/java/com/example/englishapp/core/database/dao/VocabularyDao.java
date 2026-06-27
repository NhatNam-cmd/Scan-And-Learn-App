package com.example.englishapp.core.database.dao;

import androidx.lifecycle.LiveData;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.database.entity.VocabularyFtsEntity;

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

    @Query("SELECT * FROM vocabulary WHERE vocabularyId = :id")
    LiveData<VocabularyEntity> observeVocabularyById(long id);

    @Query("SELECT * FROM vocabulary ORDER BY createdAt DESC")
    LiveData<List<VocabularyEntity>> getAllVocabularies();

    @Query("SELECT v.* FROM vocabulary v INNER JOIN vocabulary_fts f ON v.vocabularyId = f.rowid WHERE vocabulary_fts MATCH :query ORDER BY v.updatedAt DESC, v.createdAt DESC")
    LiveData<List<VocabularyEntity>> searchVocabularies(String query);

    @Query("SELECT * FROM vocabulary WHERE nextReviewDate <= :currentDate AND isMastered = 0 ORDER BY nextReviewDate ASC LIMIT :limit")
    List<VocabularyEntity> getDueWords(long currentDate, int limit);

    @Query("SELECT COUNT(*) FROM vocabulary WHERE nextReviewDate <= :currentDate AND isMastered = 0")
    int countDueWords(long currentDate);

    @Query("SELECT * FROM vocabulary WHERE isMastered = 0 ORDER BY masteryLevel ASC, createdAt DESC")
    LiveData<List<VocabularyEntity>> getUnmasteredWords();

    @Query("SELECT * FROM vocabulary WHERE vocabularyId IN (:ids)")
    List<VocabularyEntity> getVocabularyByIds(List<Long> ids);

    @Query("SELECT COUNT(*) FROM vocabulary WHERE isMastered = 0")
    int countUnmasteredWords();

    @Query("SELECT COUNT(*) FROM vocabulary")
    int countAllWords();

    @Query("SELECT COUNT(*) FROM vocabulary WHERE isMastered = 1")
    int countMasteredWords();

    @Query("SELECT COUNT(*) FROM vocabulary WHERE word = :word")
    int countWord(String word);

    @androidx.room.Query("SELECT * FROM vocabulary WHERE word = :word AND sourceType = :sourceType LIMIT 1")
    VocabularyEntity findByWord(String word, String sourceType);

    @androidx.room.Insert
    long insert(VocabularyEntity entity);

    @androidx.room.Update
    void update(VocabularyEntity entity);

    @Delete
    void delete(VocabularyEntity entity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertFts(VocabularyFtsEntity entity);

    @Query("DELETE FROM vocabulary_fts WHERE rowid = :vocabularyId")
    void deleteFts(long vocabularyId);

    @Query("DELETE FROM vocabulary_fts")
    void clearFts();

    @Query("INSERT INTO vocabulary_fts(rowid, word, meaning, phonetic, exampleSentence, note) SELECT vocabularyId, word, meaning, phonetic, exampleSentence, note FROM vocabulary")
    void rebuildFts();

    @Query("UPDATE vocabulary SET topicId = :topicId, updatedAt = :updatedAt WHERE vocabularyId IN (:ids)")
    void updateTopicForIds(List<Long> ids, Long topicId, long updatedAt);

    /** Counts words added since the given timestamp (epoch ms). Used for daily progress. */
    @Query("SELECT COUNT(*) FROM vocabulary WHERE createdAt >= :sinceMillis")
    int countWordsAddedSince(long sinceMillis);
    @Query("SELECT * FROM vocabulary ORDER BY updatedAt DESC")
    List<VocabularyEntity> getAllVocabulariesSync();
}
