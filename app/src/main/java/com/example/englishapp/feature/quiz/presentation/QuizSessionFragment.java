package com.example.englishapp.feature.quiz.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.englishapp.R;
import com.example.englishapp.feature.quiz.domain.repository.model.QuizQuestion;
import com.example.englishapp.feature.quiz.presentation.QuizSessionViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizSessionFragment extends Fragment {

    private QuizSessionViewModel viewModel;
    private TextView tvProgress, tvQuestionLabel, tvQuestionText;
    private ProgressBar pbQuizProgress;
    private MaterialButton btnOption1, btnOption2, btnOption3, btnOption4, btnNext;
    private MaterialButton[] optionButtons;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(QuizSessionViewModel.class);

        tvProgress = view.findViewById(R.id.tv_progress);
        tvQuestionLabel = view.findViewById(R.id.tv_question_label);
        tvQuestionText = view.findViewById(R.id.tv_question_text);
        pbQuizProgress = view.findViewById(R.id.pb_quiz_progress);
        btnOption1 = view.findViewById(R.id.btn_option_1);
        btnOption2 = view.findViewById(R.id.btn_option_2);
        btnOption3 = view.findViewById(R.id.btn_option_3);
        btnOption4 = view.findViewById(R.id.btn_option_4);
        btnNext = view.findViewById(R.id.btn_next);

        optionButtons = new MaterialButton[]{btnOption1, btnOption2, btnOption3, btnOption4};

        int numQuestions = getArguments() != null ? getArguments().getInt("numQuestions", 5) : 5;

        // Ensure we only start once
        if (viewModel.getQuestions().getValue() == null) {
            viewModel.startQuiz(numQuestions);
        }

        viewModel.getQuestions().observe(getViewLifecycleOwner(), questions -> {
            if (questions != null && !questions.isEmpty()) {
                pbQuizProgress.setMax(questions.size());
                updateUI(questions.get(viewModel.getCurrentQuestionIndex().getValue() != null ? viewModel.getCurrentQuestionIndex().getValue() : 0));
            } else if (questions != null && questions.isEmpty()) {
                Toast.makeText(requireContext(), "Not enough vocabulary to start a quiz", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(view).popBackStack();
            }
        });

        viewModel.getCurrentQuestionIndex().observe(getViewLifecycleOwner(), index -> {
            List<QuizQuestion> questions = viewModel.getQuestions().getValue();
            if (questions != null && index < questions.size()) {
                updateUI(questions.get(index));
                tvProgress.setText("Question " + (index + 1) + "/" + questions.size());
                pbQuizProgress.setProgress(index + 1);
            }
        });

        viewModel.getQuizFinished().observe(getViewLifecycleOwner(), finished -> {
            if (finished) {
                Bundle args = new Bundle();
                args.putInt("score", viewModel.getScore());
                args.putInt("totalQuestions", viewModel.getTotalQuestions());
                Navigation.findNavController(view).navigate(R.id.nav_quiz_result, args);
            }
        });

        for (MaterialButton btn : optionButtons) {
            btn.setOnClickListener(v -> onOptionSelected(btn));
        }

        btnNext.setOnClickListener(v -> viewModel.nextQuestion());
    }

    private void updateUI(QuizQuestion question) {
        if (question == null) return;

        resetButtons();

        if (question.isMeaningToWord()) {
            tvQuestionLabel.setText("Which word means:");
            tvQuestionText.setText(question.getTargetVocabulary().getMeaning());
        } else {
            tvQuestionLabel.setText("What is the meaning of:");
            tvQuestionText.setText(question.getTargetVocabulary().getWord());
        }

        List<String> options = question.getOptions();
        for (int i = 0; i < optionButtons.length; i++) {
            if (i < options.size()) {
                optionButtons[i].setVisibility(View.VISIBLE);
                optionButtons[i].setText(options.get(i));
            } else {
                optionButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private void onOptionSelected(MaterialButton selectedBtn) {
        String answer = selectedBtn.getText().toString();
        viewModel.submitAnswer(answer);

        List<QuizQuestion> questions = viewModel.getQuestions().getValue();
        Integer currentIndex = viewModel.getCurrentQuestionIndex().getValue();
        if (questions == null || currentIndex == null) return;

        QuizQuestion currentQuestion = questions.get(currentIndex);

        for (MaterialButton btn : optionButtons) {
            btn.setEnabled(false);
            if (btn.getText().toString().equals(currentQuestion.getCorrectAnswer())) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light));
            } else if (btn == selectedBtn && !currentQuestion.isCorrect()) {
                btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light));
            }
        }

        btnNext.setEnabled(true);
    }

    private void resetButtons() {
        for (MaterialButton btn : optionButtons) {
            btn.setEnabled(true);
            btn.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent));
        }
        btnNext.setEnabled(false);
    }
}
