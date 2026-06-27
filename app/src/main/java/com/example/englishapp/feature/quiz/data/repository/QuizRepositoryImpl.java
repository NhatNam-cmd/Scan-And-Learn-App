package com.example.englishapp.feature.quiz.data.repository;

import androidx.lifecycle.LiveData;
import com.example.englishapp.core.database.dao.QuizDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuizRepositoryImpl implements QuizRepository {

    private final VocabularyDao vocabularyDao;
    private final QuizDao quizDao;

    @Inject
    public QuizRepositoryImpl(VocabularyDao vocabularyDao, QuizDao quizDao) {
        this.vocabularyDao = vocabularyDao;
        this.quizDao = quizDao;
    }

    @Override
    public LiveData<List<VocabularyEntity>> getAllVocabularies() {
        return vocabularyDao.getAllVocabularies();
    }

    @Override
    public List<VocabularyEntity> getAllVocabulariesSync() {
        return vocabularyDao.getAllVocabulariesSync();
    }

    @Override
    public long saveQuizSession(int score, int totalQuestions, long startedAt, long completedAt) {
        QuizSessionEntity session = new QuizSessionEntity(score, totalQuestions, startedAt, completedAt);
        return quizDao.insertQuizSession(session);
    }

    @Override
    public void saveQuizQuestionResult(long quizId, long vocabularyId, boolean isCorrect) {
        QuizQuestionCrossRef crossRef = new QuizQuestionCrossRef(quizId, vocabularyId, isCorrect);
        quizDao.insertQuizQuestionCrossRef(crossRef);
    }
}
