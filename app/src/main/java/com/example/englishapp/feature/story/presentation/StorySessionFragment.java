package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import android.widget.Button;

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
    private ChipGroup chipGroupAnswers;
    private ChipGroup chipGroupWords;
    private Button btnSubmit;
    private ProgressBar pbFillProgress;
    private TextView tvFillCounter;
    private final List<String> shuffledWords = new ArrayList<>();

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
        chipGroupAnswers = view.findViewById(R.id.chip_group_answers);
        chipGroupWords = view.findViewById(R.id.chip_group_words);
        btnSubmit = view.findViewById(R.id.btn_submit_story);
        pbFillProgress = view.findViewById(R.id.pb_fill_progress);
        tvFillCounter = view.findViewById(R.id.tv_fill_counter);

        viewModel.getCurrentStory().observe(getViewLifecycleOwner(), story -> {
            if (story == null) {
                NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
                return;
            }
            tvTitle.setText(story.getTitle());
            shuffledWords.clear();
            for (StoryBlank blank : story.getBlanks()) {
                shuffledWords.add(blank.getWord());
            }
            Collections.shuffle(shuffledWords);
            render(story, viewModel.getAnswers().getValue());
            animateEnter(view);
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
        renderAnswerChips(answers);
        renderWordChips(answers);

        int filled = 0;
        for (String a : answers) {
            if (a != null && !a.isEmpty()) filled++;
        }
        int total = answers.size();
        int progress = total > 0 ? (filled * 100 / total) : 0;
        pbFillProgress.setProgress(progress);
        tvFillCounter.setText(filled + "/" + total + " ô");

        boolean canSubmit = !answers.contains("");
        btnSubmit.setEnabled(canSubmit);
        btnSubmit.setAlpha(canSubmit ? 1f : 0.45f);
    }

    private SpannableStringBuilder buildStoryText(StoryGameData story, List<String> answers) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String text = story.getStory();
        int searchStart = 0;
        for (int i = 0; i < answers.size(); i++) {
            String token = "[BLANK_" + (i + 1) + "]";
            int tokenIndex = text.indexOf(token, searchStart);
            if (tokenIndex < 0) {
                continue;
            }
            builder.append(text, searchStart, tokenIndex);
            String answer = answers.get(i);
            boolean isFilled = answer != null && !answer.isEmpty();
            String value = isFilled ? answer : "________";
            int start = builder.length();
            builder.append(value);
            int end = builder.length();
            builder.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (isFilled) {
                builder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.light_primary)),
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            searchStart = tokenIndex + token.length();
        }
        if (searchStart < text.length()) {
            builder.append(text.substring(searchStart));
        }
        return builder;
    }

    private void renderAnswerChips(List<String> answers) {
        chipGroupAnswers.removeAllViews();
        for (int i = 0; i < answers.size(); i++) {
            String answer = answers.get(i);
            boolean isFilled = answer != null && !answer.isEmpty();
            Chip chip = makeAnswerChip(isFilled ? answer : "Ô " + (i + 1), isFilled);
            final int index = i;
            if (isFilled) {
                chip.setOnClickListener(v -> {
                    animateChipRemove(chip);
                    viewModel.clearBlank(index);
                });
            }
            chipGroupAnswers.addView(chip);
            animateChip(chip);
        }
    }

    private void renderWordChips(List<String> answers) {
        chipGroupWords.removeAllViews();
        Set<String> used = new HashSet<>(answers);
        for (String word : shuffledWords) {
            if (used.contains(word)) {
                continue;
            }
            Chip chip = makeWordChip(word);
            chip.setOnClickListener(v -> {
                animateChipSelect(chip);
                viewModel.fillNextBlank(word);
            });
            chipGroupWords.addView(chip);
            animateChip(chip);
        }
    }

    private Chip makeWordChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setChipBackgroundColorResource(R.color.light_primary_container);
        chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_primary));
        chip.setChipStrokeColorResource(R.color.light_primary);
        chip.setChipStrokeWidth(dpToPx(1.5f));
        chip.setTextSize(14f);
        chip.setTypeface(chip.getTypeface(), Typeface.BOLD);
        chip.setClickable(true);
        chip.setFocusable(true);
        chip.setChipCornerRadius(dpToPx(20f));
        return chip;
    }

    private Chip makeAnswerChip(String text, boolean isFilled) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        if (isFilled) {
            chip.setChipBackgroundColorResource(R.color.light_secondary_container);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_on_secondary_container));
            chip.setChipStrokeColorResource(R.color.light_secondary);
            chip.setChipStrokeWidth(dpToPx(1.5f));
            chip.setCloseIconVisible(true);
            chip.setCloseIconTint(ContextCompat.getColorStateList(requireContext(), R.color.light_secondary));
        } else {
            chip.setChipBackgroundColorResource(android.R.color.transparent);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.light_secondary));
            chip.setChipStrokeWidth(dpToPx(1.5f));
            chip.setChipStrokeColor(ContextCompat.getColorStateList(requireContext(), R.color.light_secondary));
            chip.setEnabled(false);
            chip.setAlpha(0.5f);
        }
        chip.setTextSize(13f);
        chip.setChipCornerRadius(dpToPx(20f));
        return chip;
    }

    private float dpToPx(float dp) {
        return dp * requireContext().getResources().getDisplayMetrics().density;
    }

    private void animateEnter(View view) {
        view.setAlpha(0f);
        view.setTranslationY(20f);
        view.animate().alpha(1f).translationY(0f).setDuration(260L).start();
    }

    private void animateChip(View view) {
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180L).start();
    }

    private void animateChipSelect(View view) {
        view.animate().scaleX(0.88f).scaleY(0.88f).setDuration(80L)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(80L).start())
                .start();
    }

    private void animateChipRemove(View view) {
        view.animate().scaleX(0.7f).scaleY(0.7f).alpha(0f).setDuration(120L).start();
    }
}
