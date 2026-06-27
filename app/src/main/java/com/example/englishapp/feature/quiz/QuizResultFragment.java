package com.example.englishapp.feature.quiz;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.englishapp.R;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class QuizResultFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int score = getArguments() != null ? getArguments().getInt("score", 0) : 0;
        int totalQuestions = getArguments() != null ? getArguments().getInt("totalQuestions", 0) : 0;

        TextView tvScore = view.findViewById(R.id.tv_score);
        MaterialButton btnBackToHome = view.findViewById(R.id.btn_back_to_home);

        tvScore.setText("You scored " + score + "/" + totalQuestions);

        btnBackToHome.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack(R.id.nav_dashboard, false);
        });
    }
}
