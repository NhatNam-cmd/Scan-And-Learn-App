package com.example.englishapp.feature.quiz.presentation.state;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.englishapp.feature.quiz.domain.repository.model.QuizQuestion;
import com.example.englishapp.feature.quiz.usecase.GenerateQuizUseCase;
import com.example.englishapp.feature.quiz.usecase.SubmitQuizResultUseCase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class QuizSessionViewModel extends ViewModel {

    private final GenerateQuizUseCase generateQuizUseCase;
    private final SubmitQuizResultUseCase submitQuizResultUseCase;
    private final ExecutorService executorService;

    private final MutableLiveData<List<QuizQuestion>> _questions = new MutableLiveData<>();
    public LiveData<List<QuizQuestion>> getQuestions() { return _questions; }

    private final MutableLiveData<Integer> _currentQuestionIndex = new MutableLiveData<>(0);
    public LiveData<Integer> getCurrentQuestionIndex() { return _currentQuestionIndex; }

    private final MutableLiveData<Boolean> _quizFinished = new MutableLiveData<>(false);
    public LiveData<Boolean> getQuizFinished() { return _quizFinished; }

    private long startedAt;

    @Inject
    public QuizSessionViewModel(GenerateQuizUseCase generateQuizUseCase, SubmitQuizResultUseCase submitQuizResultUseCase) {
        this.generateQuizUseCase = generateQuizUseCase;
        this.submitQuizResultUseCase = submitQuizResultUseCase;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void startQuiz(int numQuestions) {
        startedAt = System.currentTimeMillis();
        executorService.execute(() -> {
            List<QuizQuestion> generated = generateQuizUseCase.invoke(numQuestions);
            _questions.postValue(generated);
            _currentQuestionIndex.postValue(0);
            _quizFinished.postValue(false);
        });
    }

    public void submitAnswer(String answer) {
        List<QuizQuestion> currentQuestions = _questions.getValue();
        Integer currentIndex = _currentQuestionIndex.getValue();

        if (currentQuestions != null && currentIndex != null && currentIndex < currentQuestions.size()) {
            QuizQuestion currentQuestion = currentQuestions.get(currentIndex);
            currentQuestion.setSelectedAnswer(answer);
        }
    }

    public void nextQuestion() {
        List<QuizQuestion> currentQuestions = _questions.getValue();
        Integer currentIndex = _currentQuestionIndex.getValue();

        if (currentQuestions != null && currentIndex != null) {
            if (currentIndex + 1 < currentQuestions.size()) {
                _currentQuestionIndex.setValue(currentIndex + 1);
            } else {
                finishQuiz();
            }
        }
    }

    private void finishQuiz() {
        List<QuizQuestion> currentQuestions = _questions.getValue();
        if (currentQuestions != null) {
            executorService.execute(() -> {
                submitQuizResultUseCase.invoke(currentQuestions, startedAt);
                _quizFinished.postValue(true);
            });
        }
    }

    public int getScore() {
        List<QuizQuestion> currentQuestions = _questions.getValue();
        if (currentQuestions == null) return 0;
        int score = 0;
        for (QuizQuestion q : currentQuestions) {
            if (q.isCorrect()) score++;
        }
        return score;
    }

    public int getTotalQuestions() {
        List<QuizQuestion> currentQuestions = _questions.getValue();
        return currentQuestions == null ? 0 : currentQuestions.size();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
