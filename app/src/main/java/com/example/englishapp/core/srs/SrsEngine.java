package com.example.englishapp.core.srs;

import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;

import javax.inject.Inject;

public class SrsEngine {
    private final SrsCalculator calculator;

    @Inject
    public SrsEngine(SrsCalculator calculator) {
        this.calculator = calculator;
    }

    public void applyReview(VocabularyEntity entity, boolean isCorrect,
                            VocabularyDao vocabularyDao, ReviewHistoryDao reviewHistoryDao) {
        if (entity == null) return;

        int oldLevel = entity.getMasteryLevel();
        long now = System.currentTimeMillis();
        SrsCalculator.ReviewResult result = calculator.calculate(oldLevel, isCorrect, now);

        entity.setMasteryLevel(result.getNewLevel());
        entity.setMastered(result.isMastered());
        entity.setNextReviewDate(result.getNextReviewDate());
        entity.setUpdatedAt(now);

        vocabularyDao.updateVocabulary(entity);
        reviewHistoryDao.insertReviewHistory(new ReviewHistoryEntity(
                0L,
                entity.getVocabularyId(),
                isCorrect,
                oldLevel,
                result.getNewLevel(),
                now
        ));
    }
}
