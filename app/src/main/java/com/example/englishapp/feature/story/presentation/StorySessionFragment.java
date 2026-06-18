package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StorySessionFragment extends Fragment {
    private StoryViewModel viewModel;
    private TextView tvTitle;
    private TextView tvStory;
    private GridLayout answerGrid;
    private GridLayout wordGrid;
    private Button btnSubmit;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        tvTitle = view.findViewById(R.id.tv_story_title);
        tvStory = view.findViewById(R.id.tv_story_content);
        answerGrid = view.findViewById(R.id.grid_answers);
        wordGrid = view.findViewById(R.id.grid_words);
        btnSubmit = view.findViewById(R.id.btn_submit_story);

        viewModel.getCurrentStory().observe(getViewLifecycleOwner(), story -> {
            if (story == null) {
                NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
                return;
            }
            tvTitle.setText(story.getTitle());
            render(story, viewModel.getAnswers().getValue());
        });
        viewModel.getAnswers().observe(getViewLifecycleOwner(),
                answers -> render(viewModel.getCurrentStory().getValue(), answers));
        btnSubmit.setOnClickListener(v -> {
            viewModel.submitAnswers();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_result);
        });
    }

    private void render(StoryGameData story, List<String> answers) {
        if (story == null || answers == null) {
            return;
        }
        tvStory.setText(buildStoryText(story, answers));
        renderAnswerButtons(answers);
        renderWordButtons(story, answers);
        btnSubmit.setEnabled(!answers.contains(""));
    }

    private String buildStoryText(StoryGameData story, List<String> answers) {
        String text = story.getStory();
        for (int i = 0; i < answers.size(); i++) {
            String value = answers.get(i).isEmpty() ? "____" : answers.get(i);
            text = text.replace("[BLANK_" + (i + 1) + "]", value);
        }
        return text;
    }

    private void renderAnswerButtons(List<String> answers) {
        answerGrid.removeAllViews();
        answerGrid.setColumnCount(2);
        for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            MaterialButton button = makeChip(answer.isEmpty() ? "Ô " + (i + 1) : answer);
            final int index = i;
            button.setEnabled(!answer.isEmpty());
            button.setOnClickListener(v -> viewModel.clearBlank(index));
            answerGrid.addView(button);
        }
    }

    private void renderWordButtons(StoryGameData story, List<String> answers) {
        wordGrid.removeAllViews();
        wordGrid.setColumnCount(2);
        Set<String> used = new HashSet<>(answers);
        List<String> words = new ArrayList<>();
        for (StoryBlank blank : story.getBlanks()) {
            words.add(blank.getWord());
        }
        Collections.shuffle(words);
        for (String word : words) {
            if (used.contains(word)) {
                continue;
            }
            MaterialButton button = makeChip(word);
            button.setOnClickListener(v -> viewModel.fillNextBlank(word));
            wordGrid.addView(button);
        }
    }

    private MaterialButton makeChip(String text) {
        MaterialButton button = new MaterialButton(requireContext());
        button.setText(text);
        button.setAllCaps(false);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(8, 8, 8, 8);
        button.setLayoutParams(params);
        return button;
    }
}
