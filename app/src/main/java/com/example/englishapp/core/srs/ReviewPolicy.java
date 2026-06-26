package com.example.englishapp.core.srs;

public final class ReviewPolicy {
    public static final int MASTERED_LEVEL = 5;
    private static final long DAY_MILLIS = 24L * 60L * 60L * 1000L;
    private static final int[] INTERVAL_DAYS = {0, 1, 3, 7, 14};

    private ReviewPolicy() {
    }

    public static long intervalMillisForLevel(int level) {
        if (level <= 0) return 0L;
        int cappedLevel = Math.min(level, INTERVAL_DAYS.length - 1);
        return INTERVAL_DAYS[cappedLevel] * DAY_MILLIS;
    }
}
