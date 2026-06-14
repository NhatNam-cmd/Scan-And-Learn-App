package com.example.englishapp.core.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import java.util.Objects;

// Intermediate N-N table: which vocabulary appears in which Quiz and the Correct/Incorrect result
// Bảng trung gian N-N: Từ vựng nào xuất hiện trong Quiz nào và kết quả Đúng/Sai
@Entity(
        tableName = "quiz_question_cross_ref",
        primaryKeys = {"quizId", "vocabularyId"},
        indices = {@Index("vocabularyId")}
)
public class QuizQuestionCrossRef {

    private long quizId;
    private long vocabularyId;
    private boolean isCorrect;

    public QuizQuestionCrossRef(long quizId, long vocabularyId, boolean isCorrect) {
        this.quizId = quizId;
        this.vocabularyId = vocabularyId;
        this.isCorrect = isCorrect;
    }

    // Getters
    public long getQuizId() { return quizId; }
    public long getVocabularyId() { return vocabularyId; }
    public boolean isCorrect() { return isCorrect; }

    // Setters
    public void setQuizId(long quizId) { this.quizId = quizId; }
    public void setVocabularyId(long vocabularyId) { this.vocabularyId = vocabularyId; }
    public void setCorrect(boolean correct) { isCorrect = correct; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QuizQuestionCrossRef that = (QuizQuestionCrossRef) o;
        return quizId == that.quizId
                && vocabularyId == that.vocabularyId
                && isCorrect == that.isCorrect;
    }

    @Override
    public int hashCode() {
        return Objects.hash(quizId, vocabularyId, isCorrect);
    }

    @NonNull
    @Override
    public String toString() {
        return "QuizQuestionCrossRef{" +
                "quizId=" + quizId +
                ", vocabularyId=" + vocabularyId +
                ", isCorrect=" + isCorrect +
                '}';
    }
}
