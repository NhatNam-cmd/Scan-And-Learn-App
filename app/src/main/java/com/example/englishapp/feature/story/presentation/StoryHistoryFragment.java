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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.databinding.FragmentStoryHistoryBinding;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryHistoryFragment extends Fragment {
    private FragmentStoryHistoryBinding binding;
    private StoryHistoryAdapter adapter;
    private final Gson gson = new Gson();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentStoryHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        adapter = new StoryHistoryAdapter(this::showStoryDetail);

        binding.recyclerStoryHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerStoryHistory.setAdapter(adapter);
        binding.btnStoryHistoryBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        viewModel.getCompletedStories().observe(getViewLifecycleOwner(), stories -> {
            adapter.submitList(stories);
            boolean isEmpty = stories == null || stories.isEmpty();
            binding.layoutStoryHistoryEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.recyclerStoryHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showStoryDetail(StoryEntity story) {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_story_history_detail, null, false);

        TextView tvType = content.findViewById(R.id.tv_story_detail_type);
        TextView tvTitle = content.findViewById(R.id.tv_story_detail_title);
        TextView tvMeta = content.findViewById(R.id.tv_story_detail_meta);
        TextView tvBody = content.findViewById(R.id.tv_story_detail_body);

        StoryGameData data = parseStory(story);
        tvType.setText(formatDifficulty(story.getDifficulty()));
        tvTitle.setText(story.getTitle());
        tvMeta.setText("🕐 " + dateFormat.format(new Date(story.getCreatedAt())));
        tvBody.setText(data != null && data.getStory() != null ? data.getStory() : story.getContent());

        new MaterialAlertDialogBuilder(requireContext())
                .setView(content)
                .setPositiveButton("Đóng", null)
                .show();
    }

    private StoryGameData parseStory(StoryEntity story) {
        try {
            return gson.fromJson(story.getContent(), StoryGameData.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String formatDifficulty(String difficulty) {
        if ("AI".equalsIgnoreCase(difficulty)) {
            return "✨ AI Story";
        }
        if ("OFFLINE".equalsIgnoreCase(difficulty)) {
            return "📖 Offline Story";
        }
        return difficulty == null || difficulty.trim().isEmpty() ? "Story" : difficulty;
    }
}
