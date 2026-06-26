package com.example.englishapp.core.srs;

import javax.inject.Inject;

public class SrsCalculator {

    @Inject
    public SrsCalculator() {
    }

    public ReviewResult calculate(int oldLevel, boolean isCorrect, long nowMillis) {
        int normalizedOldLevel = Math.max(0, oldLevel);
        int newLevel = isCorrect
                ? normalizedOldLevel + 1
                : Math.max(0, normalizedOldLevel - 1);
        boolean mastered = newLevel >= ReviewPolicy.MASTERED_LEVEL;
        long nextReviewDate = mastered
                ? nowMillis + ReviewPolicy.intervalMillisForLevel(ReviewPolicy.MASTERED_LEVEL - 1)
                : nowMillis + ReviewPolicy.intervalMillisForLevel(newLevel);
        return new ReviewResult(newLevel, mastered, nextReviewDate);
    }

    public static class ReviewResult {
        private final int newLevel;
        private final boolean mastered;
        private final long nextReviewDate;

        ReviewResult(int newLevel, boolean mastered, long nextReviewDate) {
            this.newLevel = newLevel;
            this.mastered = mastered;
            this.nextReviewDate = nextReviewDate;
        }

        public int getNewLevel() {
            return newLevel;
        }

        public boolean isMastered() {
            return mastered;
        }

        public long getNextReviewDate() {
            return nextReviewDate;
        }
    }
}
