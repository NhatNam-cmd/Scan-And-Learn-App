package com.example.englishapp.core.database.seed;

import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.entity.TopicEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;

public class VocabularySeeder {
    private static final int MIN_TEST_WORDS = 20;

    private VocabularySeeder() {
    }

    public static void seedIfNeeded(AppDatabase database) {
        if (database.vocabularyDao().countUnmasteredWords() >= MIN_TEST_WORDS) {
            return;
        }

        long topicId = database.topicDao().insertTopic(
                new TopicEntity(0, "Story Practice", "Từ mẫu để test tính năng Story", System.currentTimeMillis()));

        insert(database, topicId, "adventure", "cuộc phiêu lưu",
                "The adventure began when Mia opened the old map.");
        insert(database, topicId, "brave", "dũng cảm",
                "The brave student answered the difficult question.");
        insert(database, topicId, "discover", "khám phá",
                "They discover a hidden garden behind the library.");
        insert(database, topicId, "forest", "khu rừng",
                "The forest was quiet after the rain stopped.");
        insert(database, topicId, "journey", "hành trình",
                "Her journey to the mountain took three days.");
        insert(database, topicId, "mystery", "điều bí ẩn",
                "The mystery of the locked box made everyone curious.");
        insert(database, topicId, "promise", "lời hứa",
                "He kept his promise to help his friend.");
        insert(database, topicId, "secret", "bí mật",
                "The secret message was written under the desk.");
        insert(database, topicId, "treasure", "kho báu",
                "The treasure was hidden near the river.");
        insert(database, topicId, "whisper", "thì thầm",
                "She could hear a whisper from the empty hallway.");
        insert(database, topicId, "curious", "tò mò",
                "The curious boy looked inside the small box.");
        insert(database, topicId, "danger", "nguy hiểm",
                "The sign warned travelers about danger on the road.");
        insert(database, topicId, "escape", "trốn thoát",
                "They planned to escape before sunset.");
        insert(database, topicId, "friendship", "tình bạn",
                "Their friendship became stronger after the challenge.");
        insert(database, topicId, "magic", "phép thuật",
                "A little magic changed the dark room into a bright hall.");
        insert(database, topicId, "memory", "ký ức",
                "This photo brought back a happy memory.");
        insert(database, topicId, "rescue", "giải cứu",
                "The team worked together to rescue the lost child.");
        insert(database, topicId, "shadow", "cái bóng",
                "A tall shadow moved across the wall.");
        insert(database, topicId, "village", "ngôi làng",
                "The village was famous for its colorful lanterns.");
        insert(database, topicId, "wonder", "sự kỳ diệu",
                "The children looked at the stars with wonder.");
        insert(database, topicId, "ancient", "cổ xưa",
                "They found an ancient coin near the temple.");
        insert(database, topicId, "courage", "lòng can đảm",
                "Courage helped her speak in front of the class.");
        insert(database, topicId, "lantern", "đèn lồng",
                "The lantern guided them through the dark street.");
        insert(database, topicId, "river", "dòng sông",
                "The river flowed beside the quiet village.");
        insert(database, topicId, "storm", "cơn bão",
                "The storm arrived suddenly in the afternoon.");
    }

    private static void insert(AppDatabase database, long topicId, String word, String meaning, String example) {
        long now = System.currentTimeMillis();
        VocabularyEntity entity = new VocabularyEntity(
                0,
                topicId,
                word,
                meaning,
                null,
                example,
                null,
                null,
                "Seed data for Story testing",
                "TOPIC",
                0,
                false,
                0L,
                now,
                now);
        database.vocabularyDao().insertVocabulary(entity);
    }
}
