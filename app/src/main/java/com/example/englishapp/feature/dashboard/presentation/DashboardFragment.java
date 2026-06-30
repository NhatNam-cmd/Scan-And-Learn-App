package com.example.englishapp.feature.dashboard.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.databinding.FragmentDashboardBinding;

import java.util.Calendar;


import dagger.hilt.android.AndroidEntryPoint;

/**
 * Dashboard — the main home screen of the app.
 *
 * Shows:
 *  - Greeting banner with time-of-day salutation
 *  - Daily goal progress bar
 *  - Streak counter
 *  - Key statistics (total words, mastered, due for review)
 *  - Quick-action shortcuts to Scan, Vocabulary, Quiz, Story
 *  - Mastery progress panel
 *  - Motivational footer
 *
 * All data is loaded asynchronously via {@link DashboardViewModel}.
 */
@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupGreeting();
        setupShortcuts();
        observeUiState();
        animateEntrance();

        viewModel.loadStats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh stats every time the user returns to the dashboard
        viewModel.loadStats();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ─────────────────────────────────────────────────────────────────────
    // Setup
    // ─────────────────────────────────────────────────────────────────────

    /** Sets time-appropriate greeting text. */
    private void setupGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour >= 5 && hour < 12) {
            greeting = "Chào buổi sáng! ☀️";
        } else if (hour >= 12 && hour < 18) {
            greeting = "Chào buổi chiều! 🌤️";
        } else {
            greeting = "Chào buổi tối! 🌙";
        }
        binding.tvGreeting.setText(greeting);
    }

    /** Wires click listeners on the four quick-action shortcut cards. */
    private void setupShortcuts() {
        binding.shortcutScan.setOnClickListener(v -> {
            animateClick(v);
            NavHostFragment.findNavController(this).navigate(R.id.nav_scan);
        });

        binding.shortcutVocabulary.setOnClickListener(v -> {
            animateClick(v);
            NavHostFragment.findNavController(this).navigate(R.id.nav_vocabulary);
        });

        binding.shortcutQuiz.setOnClickListener(v -> {
            animateClick(v);
            NavHostFragment.findNavController(this).navigate(R.id.nav_quiz_config);
        });

        binding.shortcutStory.setOnClickListener(v -> {
            animateClick(v);
            NavHostFragment.findNavController(this).navigate(R.id.nav_story);
        });
    }

    // ─────────────────────────────────────────────────────────────────────
    // State observation
    // ─────────────────────────────────────────────────────────────────────

    private void observeUiState() {
        viewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            if (binding == null) return;

            renderHeroCard(state);
            renderStatCards(state);
            renderMasteryPanel(state);
            renderMotivation(state);
        });
    }

    /** Updates the hero gradient card at the top. */
    private void renderHeroCard(DashboardUiState state) {
        // Streak
        binding.tvHeroStreak.setText(String.valueOf(state.getStreakDays()));

        // Daily goal
        String goalLabel = state.getWordsLearnedToday() + "/" + state.getDailyGoal() + " từ";
        binding.tvDailyGoalLabel.setText(goalLabel);

        int progressPercent = Math.round(state.getDailyGoalProgress() * 100f);
        animateProgressBar(binding.progressDailyGoal, progressPercent);

        // Hero motivational title
        if (state.getDailyGoalProgress() >= 1f) {
            binding.tvHeroTitle.setText("Xuất sắc! Hoàn thành mục tiêu! 🎉");
        } else if (state.getStreakDays() >= 7) {
            binding.tvHeroTitle.setText("Chuỗi " + state.getStreakDays() + " ngày! 💪");
        } else if (state.getTotalWords() == 0) {
            binding.tvHeroTitle.setText("Bắt đầu hành trình học nhé!");
        } else {
            binding.tvHeroTitle.setText("Tiếp tục cố lên nào! 🚀");
        }
    }

    /** Updates the three statistics cards (total, mastered, due). */
    private void renderStatCards(DashboardUiState state) {
        animateCountTo(binding.tvStatTotalWords, state.getTotalWords());
        animateCountTo(binding.tvStatMastered, state.getMasteredWords());
        animateCountTo(binding.tvStatDueReview, state.getDueForReviewCount());
    }

    /** Updates the mastery panel with percentage and progress bar. */
    private void renderMasteryPanel(DashboardUiState state) {
        int masteryPercent = state.getMasteryPercent();
        binding.tvMasteryPercent.setText(masteryPercent + "%");
        binding.tvMasteryDetail.setText(
                state.getMasteredWords() + " / " + state.getTotalWords() + " từ đã thuộc");

        animateProgressBar(binding.progressMastery, masteryPercent);

        animateCountTo(binding.tvCompletedQuizzes, state.getCompletedQuizzes());
        animateCountTo(binding.tvCompletedStories, state.getCompletedStories());
    }

    /** Updates the motivational footer message based on current stats. */
    private void renderMotivation(DashboardUiState state) {
        String[] motivations;
        if (state.getTotalWords() == 0) {
            motivations = new String[]{"📷 Bắt đầu bằng cách quét một từ mới ngay hôm nay!"};
        } else if (state.getStreakDays() >= 30) {
            motivations = new String[]{"🏆 Thành tích 30 ngày liên tiếp! Bạn thật kiên định!"};
        } else if (state.getDueForReviewCount() > 10) {
            motivations = new String[]{"📚 Bạn có " + state.getDueForReviewCount() + " từ cần ôn tập — hãy làm quiz ngay!"};
        } else if (state.getMasteryPercent() >= 80) {
            motivations = new String[]{"⭐ Bạn đã thuộc " + state.getMasteryPercent() + "% từ vựng. Tuyệt vời!"};
        } else {
            motivations = new String[]{
                    "💪 Hãy giữ vững động lực và học mỗi ngày!",
                    "🌟 Mỗi từ mới là một bước tiến trên con đường chinh phục tiếng Anh!",
                    "📖 Đọc một truyện ngắn hôm nay để luyện từ vựng nhé!"
            };
        }
        int index = (int) (System.currentTimeMillis() % motivations.length);
        binding.tvMotivation.setText(motivations[index]);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Animations
    // ─────────────────────────────────────────────────────────────────────

    /** Fade-in + slide-up entrance animation for the whole screen. */
    private void animateEntrance() {
        animateFadeIn(binding.cardHero, 0);
        animateFadeIn(binding.tvSectionStats, 80);
        animateFadeIn(binding.layoutStatsRow, 130);
        animateFadeIn(binding.tvSectionShortcuts, 200);
        animateFadeIn(binding.shortcutScan, 250);
        animateFadeIn(binding.shortcutVocabulary, 280);
        animateFadeIn(binding.shortcutQuiz, 310);
        animateFadeIn(binding.shortcutStory, 340);
        animateFadeIn(binding.tvSectionMastery, 400);
        animateFadeIn(binding.layoutMasteryPanel, 440);
        animateFadeIn(binding.tvMotivation, 500);
    }

    private void animateFadeIn(@Nullable View view, long delayMs) {
        if (view == null) return;
        view.setAlpha(0f);
        view.setTranslationY(20f);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delayMs)
                .setDuration(280L)
                .start();
    }

    /** Quick press-scale animation on shortcut cards. */
    private void animateClick(View view) {
        view.animate()
                .scaleX(0.93f)
                .scaleY(0.93f)
                .setDuration(80)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(120)
                        .start())
                .start();
    }

    /**
     * Animates a ProgressBar from its current value to the target using a simple
     * post-delayed step animation (avoids ObjectAnimator dependency issues).
     */
    private void animateProgressBar(ProgressBar bar, int targetProgress) {
        if (bar == null) return;
        int current = bar.getProgress();
        int steps = Math.abs(targetProgress - current);
        if (steps == 0) return;
        int increment = targetProgress > current ? 1 : -1;
        Runnable stepper = new Runnable() {
            int step = 0;
            @Override
            public void run() {
                if (binding == null) return;
                if (step >= steps) return;
                bar.setProgress(current + increment * (step + 1));
                step++;
                bar.postDelayed(this, 8);
            }
        };
        bar.postDelayed(stepper, 300);
    }

    /**
     * Simple animated number count-up for stat TextViews.
     * Counts from 0 to target over ~400ms.
     */
    private void animateCountTo(TextView tv, int target) {
        if (tv == null || target <= 0) {
            if (tv != null) tv.setText("0");
            return;
        }
        int steps = Math.min(target, 30);
        long intervalMs = 400L / steps;
        tv.post(new Runnable() {
            int step = 0;
            @Override
            public void run() {
                if (binding == null || !isAdded()) return;
                step++;
                int value = (int) Math.round((double) target * step / steps);
                tv.setText(String.valueOf(value));
                if (step < steps) {
                    tv.postDelayed(this, intervalMs);
                } else {
                    tv.setText(String.valueOf(target));
                }
            }
        });
    }
}
