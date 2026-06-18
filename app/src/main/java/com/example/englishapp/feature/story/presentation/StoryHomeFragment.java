package com.example.englishapp.feature.story.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StoryHomeFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_story_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        StoryViewModel viewModel = new ViewModelProvider(requireActivity()).get(StoryViewModel.class);
        view.findViewById(R.id.btn_create_story).setOnClickListener(v -> {
            viewModel.prepareNewStory();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
        });
    }
}
