package com.example.englishapp.feature.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.englishapp.R;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizConfigFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_config, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioGroup rgQuestionCount = view.findViewById(R.id.rg_question_count);
        MaterialButton btnStartQuiz = view.findViewById(R.id.btn_start_quiz);

        btnStartQuiz.setOnClickListener(v -> {
            int selectedId = rgQuestionCount.getCheckedRadioButtonId();
            int numQuestions = 5; // Default

            if (selectedId == R.id.rb_10) {
                numQuestions = 10;
            } else if (selectedId == R.id.rb_15) {
                numQuestions = 15;
            } else if (selectedId == R.id.rb_20) {
                numQuestions = 20;
            }

            Bundle args = new Bundle();
            args.putInt("numQuestions", numQuestions);
            Navigation.findNavController(view).navigate(R.id.nav_quiz_session, args);
        });
    }
}
