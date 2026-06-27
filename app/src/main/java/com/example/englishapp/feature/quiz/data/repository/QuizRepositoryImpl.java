package com.example.englishapp.feature.quiz.data.repository;

import androidx.lifecycle.LiveData;
import com.example.englishapp.core.database.AppDatabase;
import com.example.englishapp.core.database.dao.QuizDao;
import com.example.englishapp.core.database.dao.ReviewHistoryDao;
import com.example.englishapp.core.database.dao.VocabularyDao;
import com.example.englishapp.core.database.entity.QuizQuestionCrossRef;
import com.example.englishapp.core.database.entity.QuizSessionEntity;
import com.example.englishapp.core.database.entity.ReviewHistoryEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.feature.quiz.domain.repository.repository.QuizRepository;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class QuizRepositoryImpl implements QuizRepository {

    private final VocabularyDao vocabularyDao;
    private final QuizDao quizDao;
    private final AppDatabase appDatabase;
    private final ReviewHistoryDao reviewHistoryDao;

    @Inject
    public QuizRepositoryImpl(VocabularyDao vocabularyDao, QuizDao quizDao, AppDatabase appDatabase, ReviewHistoryDao reviewHistoryDao) {
        this.vocabularyDao = vocabularyDao;
        this.quizDao = quizDao;
        this.appDatabase = appDatabase;
        this.reviewHistoryDao = reviewHistoryDao;
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
    public List<VocabularyEntity> getDueWords(long currentDate) {
        return vocabularyDao.getDueWords(currentDate, Integer.MAX_VALUE);
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

    @Override
    public void submitQuizSessionTransaction(
            QuizSessionEntity session,
            List<QuizQuestionCrossRef> pendingCrossRefs,
            List<VocabularyEntity> updatedVocabs,
            List<ReviewHistoryEntity> reviewHistories
    ) {
        appDatabase.runInTransaction(() -> {
            // 1. Insert session to get its ID
            long quizId = quizDao.insertQuizSession(session);

            // 2. Set quizId and insert crossRefs
            if (pendingCrossRefs != null) {
                for (QuizQuestionCrossRef crossRef : pendingCrossRefs) {
                    crossRef.setQuizId(quizId);
                    quizDao.insertQuizQuestionCrossRef(crossRef);
                }
            }

            // 3. Update vocabularies (SRS logic applied)
            if (updatedVocabs != null) {
                for (VocabularyEntity vocab : updatedVocabs) {
                    vocabularyDao.updateVocabulary(vocab);
                }
            }

            // 4. Insert review histories
            if (reviewHistories != null) {
                for (ReviewHistoryEntity history : reviewHistories) {
                    reviewHistoryDao.insertReviewHistory(history);
                }
            }
        });
    }
}
