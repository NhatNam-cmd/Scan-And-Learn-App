package com.example.englishapp.core.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.englishapp.core.database.dao.QuizDao;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.StoryDao;
import com.example.englishapp.core.database.dao.TopicDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.core.database.entity.StoryVocabularyCrossRef;
import com.example.englishapp.core.database.entity.TopicEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.database.entity.VocabularyFtsEntity;

@Database(
        entities = {
                TopicEntity.class,
                VocabularyEntity.class,
                ReviewHistoryEntity.class,
                StoryEntity.class,
                StoryVocabularyCrossRef.class,
                QuizSessionEntity.class,
                QuizQuestionCrossRef.class,
                VocabularyFtsEntity.class
        },
        version = 2,
        exportSchema = true
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract TopicDao topicDao();

    public abstract VocabularyDao vocabularyDao();

    public abstract ReviewHistoryDao reviewHistoryDao();

    public abstract StoryDao storyDao();

    public abstract QuizDao quizDao();
}
