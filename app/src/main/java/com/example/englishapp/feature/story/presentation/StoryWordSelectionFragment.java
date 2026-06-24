package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryWordSelectionFragment extends Fragment {
    private StoryViewModel viewModel;
    private WordSelectionAdapter adapter;
    private Button btnStart;
    private TextView tvCounter;
    private LinearLayout layoutEmptyWords;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private final List<View> progressDots = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_word_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        animateEnter(view);
        viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        btnStart = view.findViewById(R.id.btn_start_story);
        tvCounter = view.findViewById(R.id.tv_selection_counter);
        layoutEmptyWords = view.findViewById(R.id.layout_empty_words);
        progressBar = view.findViewById(R.id.progress_story_generation);

        // Collect progress dots
        int[] dotIds = {
            R.id.dot_1, R.id.dot_2, R.id.dot_3, R.id.dot_4, R.id.dot_5,
            R.id.dot_6, R.id.dot_7, R.id.dot_8, R.id.dot_9, R.id.dot_10
        };
        for (int id : dotIds) {
            progressDots.add(view.findViewById(id));
        }

        recyclerView = view.findViewById(R.id.rv_words);
        adapter = new WordSelectionAdapter(word -> viewModel.toggleSelection(word.getVocabularyId()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getWords().observe(getViewLifecycleOwner(), words -> {
            adapter.submitWords(words);
            boolean isEmpty = words == null || words.isEmpty();
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            layoutEmptyWords.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });

        viewModel.getSelectedIds().observe(getViewLifecycleOwner(), ids -> {
            adapter.submitSelectedIds(ids);
            int count = ids == null ? 0 : ids.size();
            tvCounter.setText(count + " / 10 từ đã chọn");
            updateProgressDots(count);
            updateStartButton(count, Boolean.TRUE.equals(viewModel.getLoading().getValue()));
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            int count = viewModel.getSelectedIds().getValue() == null
                    ? 0 : viewModel.getSelectedIds().getValue().size();
            updateStartButton(count, isLoading);
        });

        viewModel.getCurrentStory().observe(getViewLifecycleOwner(), story -> {
            if (story != null && progressBar.getVisibility() == View.GONE) {
                if (story.isOffline()) {
                    Toast.makeText(requireContext(),
                            "Mạng yếu, đang sử dụng chế độ luyện tập Offline",
                            Toast.LENGTH_SHORT).show();
                }
                NavHostFragment.findNavController(this).navigate(R.id.nav_story_session);
            }
        });

        btnStart.setOnClickListener(v -> viewModel.generateStory());
    }

    private void updateProgressDots(int count) {
        for (int i = 0; i < progressDots.size(); i++) {
            View dot = progressDots.get(i);
            boolean active = i < count;
            dot.setBackgroundResource(active
                    ? R.drawable.bg_progress_dot_active
                    : R.drawable.bg_progress_dot_inactive);
            dot.animate()
                    .scaleX(active ? 1.3f : 1f)
                    .scaleY(active ? 1.3f : 1f)
                    .setDuration(150L)
                    .start();
        }
    }

    private void updateStartButton(int count, boolean isLoading) {
        boolean canStart = !isLoading && count >= 5 && count <= 10;
        btnStart.setEnabled(canStart);
        btnStart.setAlpha(canStart ? 1f : 0.45f);
        if (isLoading) {
            btnStart.setText("⏳ Đang tạo truyện...");
        } else if (count < 5) {
            btnStart.setText("Chọn ít nhất 5 từ (" + count + "/5)");
        } else {
            btnStart.setText("✨ Bắt đầu tạo truyện →");
            btnStart.animate().scaleX(1.03f).scaleY(1.03f).setDuration(220L)
                    .withEndAction(() -> btnStart.animate().scaleX(1f).scaleY(1f).setDuration(220L).start())
                    .start();
        }
    }

    private void animateEnter(View view) {
        view.setAlpha(0f);
        view.setTranslationY(18f);
        view.animate().alpha(1f).translationY(0f).setDuration(240L).start();
    }
}
