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
        animateEnter(view.findViewById(R.id.img_story), 0);
        animateEnter(view.findViewById(R.id.tv_story_intro_title), 70);
        animateEnter(view.findViewById(R.id.tv_story_intro), 120);
        animateEnter(view.findViewById(R.id.layout_story_badges), 170);
        animateEnter(view.findViewById(R.id.btn_create_story), 220);
        animateEnter(view.findViewById(R.id.btn_story_history), 270);
        view.findViewById(R.id.btn_create_story).setOnClickListener(v -> {
            viewModel.prepareNewStory();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
        });
        view.findViewById(R.id.btn_story_history).setOnClickListener(v ->
                NavHostFragment.findNavController(this).navigate(R.id.nav_story_history));
    }

    private void animateEnter(View view, long delay) {
        view.setAlpha(0f);
        view.setTranslationY(18f);
        view.animate().alpha(1f).translationY(0f).setStartDelay(delay).setDuration(240L).start();
    }
}
