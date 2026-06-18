package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.feature.story.domain.StoryGameData;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryResultFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        animateEnter(view);
        StoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        TextView tvScore = view.findViewById(R.id.tv_result_score);
        TextView tvExp = view.findViewById(R.id.tv_result_exp);
        StoryGameData story = viewModel.getCurrentStory().getValue();
        int total = story == null || story.getBlanks() == null ? 0 : story.getBlanks().size();
        Integer score = viewModel.getScore().getValue();
        int correct = score == null ? 0 : score;
        tvScore.setText(correct + "/" + total + " câu đúng");
        tvExp.setText("EXP +" + (correct * 10));

        view.findViewById(R.id.btn_story_home).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.nav_story, false));
        view.findViewById(R.id.btn_play_again).setOnClickListener(v -> {
            viewModel.prepareNewStory();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
        });
    }

    private void animateEnter(View view) {
        view.setAlpha(0f);
        view.setScaleX(0.98f);
        view.setScaleY(0.98f);
        view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260L).start();
    }
}
