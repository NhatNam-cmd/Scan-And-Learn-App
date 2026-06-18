package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryWordSelectionFragment extends Fragment {
    private StoryViewModel viewModel;
    private WordSelectionAdapter adapter;
    private Button btnStart;
    private TextView tvCounter;
    private TextView tvEmptyWords;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_word_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        btnStart = view.findViewById(R.id.btn_start_story);
        tvCounter = view.findViewById(R.id.tv_selection_counter);
        tvEmptyWords = view.findViewById(R.id.tv_empty_words);
        progressBar = view.findViewById(R.id.progress_story_generation);

        recyclerView = view.findViewById(R.id.rv_words);
        adapter = new WordSelectionAdapter(word -> viewModel.toggleSelection(word.getVocabularyId()));
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getWords().observe(getViewLifecycleOwner(), words -> {
            adapter.submitWords(words);
            boolean isEmpty = words == null || words.isEmpty();
            recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
            tvEmptyWords.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        });
        viewModel.getSelectedIds().observe(getViewLifecycleOwner(), ids -> {
            adapter.submitSelectedIds(ids);
            int count = ids == null ? 0 : ids.size();
            tvCounter.setText(count + "/10 từ đã chọn");
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

    private void updateStartButton(int count, boolean isLoading) {
        boolean canStart = !isLoading && count >= 5 && count <= 10;
        btnStart.setEnabled(canStart);
        btnStart.setAlpha(canStart ? 1f : 0.45f);
        if (isLoading) {
            btnStart.setText("Đang tạo truyện...");
        } else if (count < 5) {
            btnStart.setText("Chọn ít nhất 5 từ");
        } else {
            btnStart.setText("Bắt đầu tạo truyện");
        }
    }
}
