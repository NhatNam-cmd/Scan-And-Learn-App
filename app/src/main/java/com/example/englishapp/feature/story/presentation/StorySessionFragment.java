package com.example.englishapp.feature.story.presentation;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.core.service.TtsManager;
import com.example.englishapp.feature.story.domain.StoryBlank;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class StorySessionFragment extends Fragment {

    // --- Views ---
    private StoryViewModel viewModel;
    private View layoutContent;
    private View shimmerLayout;
    private View shimmerTvHint;
    private TextView tvTitle;
    private TextView tvStory;
    private ChipGroup chipGroupAnswers;
    private ChipGroup chipGroupWords;
    private Button btnSubmit;
    private ProgressBar pbFillProgress;
    private TextView tvFillCounter;

    // TTS
    private View fabTtsContainer;
    private View btnTts;
    private ImageView ivTtsIcon;
    private AudioWaveView audioWaveView;
    private TtsManager ttsManager;
    private boolean isSpeaking = false;

    // Shimmer animation
    private ValueAnimator shimmerAnimator;

    private final List<String> shuffledWords = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
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

        // Bind views
        layoutContent    = view.findViewById(R.id.layout_session_content);
        shimmerLayout    = view.findViewById(R.id.shimmer_layout);
        tvTitle          = view.findViewById(R.id.tv_story_title);
        tvStory          = view.findViewById(R.id.tv_story_content);
        chipGroupAnswers = view.findViewById(R.id.chip_group_answers);
        chipGroupWords   = view.findViewById(R.id.chip_group_words);
        btnSubmit        = view.findViewById(R.id.btn_submit_story);
        pbFillProgress   = view.findViewById(R.id.pb_fill_progress);
        tvFillCounter    = view.findViewById(R.id.tv_fill_counter);

        // TTS views
        fabTtsContainer = view.findViewById(R.id.fab_tts_container);
        btnTts          = view.findViewById(R.id.btn_tts);
        ivTtsIcon       = view.findViewById(R.id.iv_tts_icon);
        audioWaveView   = view.findViewById(R.id.audio_wave_view);

        // Init TTS
        initTts();

        // Observe loading state → show/hide shimmer
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean isLoading = Boolean.TRUE.equals(loading);
            if (isLoading) {
                showShimmer();
            } else {
                hideShimmer();
            }
        });

        // Observe story data
        viewModel.getCurrentStory().observe(getViewLifecycleOwner(), story -> {
            if (story == null) {
                // No story yet – check if loading; if not, go back to selection
                if (!Boolean.TRUE.equals(viewModel.getLoading().getValue())) {
                    NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
                }
                return;
            }
            tvTitle.setText(story.getTitle());
            shuffledWords.clear();
            for (StoryBlank blank : story.getBlanks()) {
                shuffledWords.add(blank.getWord());
            }
            Collections.shuffle(shuffledWords);
            render(story, viewModel.getAnswers().getValue());
            // Show FAB and content once story is ready
            fabTtsContainer.setVisibility(View.VISIBLE);
            animateEnter(layoutContent);
        });

        viewModel.getAnswers().observe(getViewLifecycleOwner(),
                answers -> render(viewModel.getCurrentStory().getValue(), answers));

        btnSubmit.setOnClickListener(v -> {
            stopTts();
            viewModel.submitAnswers();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_result);
        });

        // TTS FAB click
        btnTts.setOnClickListener(v -> onTtsFabClick());
    }

    // ─── TTS ────────────────────────────────────────────────────
    private void initTts() {
        ttsManager = new TtsManager(requireContext(), ready -> {
            // Nothing to do on ready; FAB is shown when story loads
        });

        ttsManager.setSpeakListener(new TtsManager.OnSpeakListener() {
            @Override
            public void onSpeakStart(String utteranceId) {
                requireActivity().runOnUiThread(() -> setSpeakingState(true));
            }

            @Override
            public void onSpeakDone(String utteranceId) {
                requireActivity().runOnUiThread(() -> setSpeakingState(false));
            }

            @Override
            public void onSpeakError(String utteranceId) {
                requireActivity().runOnUiThread(() -> setSpeakingState(false));
            }
        });
    }

    private void onTtsFabClick() {
        // Scale pulse on the FAB
        animateScalePulse(btnTts);

        if (isSpeaking) {
            stopTts();
        } else {
            startTts();
        }
    }

    private void startTts() {
        StoryGameData story = viewModel.getCurrentStory().getValue();
        if (story == null) return;

        // Build clean plain text (strip [BLANK_N] tokens → use the actual words or blank hint)
        List<String> answers = viewModel.getAnswers().getValue();
        String plainText = buildPlainTextForTts(story, answers);
        ttsManager.speak(plainText);
    }

    private void stopTts() {
        ttsManager.stop();
        setSpeakingState(false);
    }

    /** Replace [BLANK_N] tokens with the filled word or "_____" */
    private String buildPlainTextForTts(StoryGameData story, List<String> answers) {
        if (story == null) return "";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("\\[BLANK_(\\d+)\\]");
        java.util.regex.Matcher m = p.matcher(story.getStory());
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;
        while (m.find()) {
            sb.append(story.getStory(), lastEnd, m.start());
            int idx = Integer.parseInt(m.group(1)) - 1;
            if (answers != null && idx >= 0 && idx < answers.size()
                    && answers.get(idx) != null && !answers.get(idx).isEmpty()) {
                sb.append(answers.get(idx));
            } else {
                sb.append("blank");
            }
            lastEnd = m.end();
        }
        if (lastEnd < story.getStory().length()) {
            sb.append(story.getStory(), lastEnd, story.getStory().length());
        }
        return sb.toString();
    }

    private void setSpeakingState(boolean speaking) {
        isSpeaking = speaking;
        if (speaking) {
            ivTtsIcon.setImageResource(R.drawable.ic_stop_circle);
            audioWaveView.setBarColor(ContextCompat.getColor(requireContext(), R.color.light_on_primary));
            audioWaveView.startWave();
            // Pulse the FAB gently while speaking
            animateFabPulseLoop(btnTts);
        } else {
            ivTtsIcon.setImageResource(R.drawable.ic_volume_up);
            audioWaveView.stopWave();
            btnTts.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
        }
    }

    // ─── Shimmer ────────────────────────────────────────────────
    private void showShimmer() {
        layoutContent.setVisibility(View.GONE);
        fabTtsContainer.setVisibility(View.GONE);
        shimmerLayout.setVisibility(View.VISIBLE);
        startShimmerAnimation();
    }

    private void hideShimmer() {
        stopShimmerAnimation();
        shimmerLayout.setVisibility(View.GONE);
        // Content is shown when story observable fires
    }

    private void startShimmerAnimation() {
        if (shimmerAnimator != null && shimmerAnimator.isRunning()) return;

        // Collect all shimmer block views inside the shimmer layout
        // We animate their alpha to create the "pulse" effect
        List<View> shimmerBlocks = collectShimmerBlocks(shimmerLayout);

        shimmerAnimator = ValueAnimator.ofFloat(0.4f, 1f);
        shimmerAnimator.setDuration(900);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shimmerAnimator.addUpdateListener(anim -> {
            float v = (float) anim.getAnimatedValue();
            for (View block : shimmerBlocks) {
                block.setAlpha(v);
            }
        });
        shimmerAnimator.start();
    }

    private void stopShimmerAnimation() {
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
    }

    /** Recursively collect all Views that use bg_shimmer_block (tag-based or by size heuristic). */
    private List<View> collectShimmerBlocks(View root) {
        List<View> result = new ArrayList<>();
        collectShimmerBlocksRec(root, result);
        return result;
    }

    private void collectShimmerBlocksRec(View view, List<View> result) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                collectShimmerBlocksRec(group.getChildAt(i), result);
            }
        } else {
            // Plain View with no children = block placeholder
            result.add(view);
        }
    }

    // ─── Render ─────────────────────────────────────────────────
    private void render(StoryGameData story, List<String> answers) {
        if (story == null || answers == null) return;
        tvStory.setText(buildStoryText(story, answers));
        renderAnswerChips(answers);
        renderWordChips(answers);

        int filled = 0;
        for (String a : answers) { if (a != null && !a.isEmpty()) filled++; }
        int total    = answers.size();
        int progress = total > 0 ? (filled * 100 / total) : 0;
        pbFillProgress.setProgress(progress);
        tvFillCounter.setText(filled + "/" + total + " ô");

        boolean canSubmit = !answers.contains("");
        btnSubmit.setEnabled(canSubmit);
        btnSubmit.setAlpha(canSubmit ? 1f : 0.45f);
    }

    private SpannableStringBuilder buildStoryText(StoryGameData story, List<String> answers) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        java.util.regex.Pattern blankPattern =
                java.util.regex.Pattern.compile("\\[BLANK_(\\d+)\\]");
        java.util.regex.Matcher matcher = blankPattern.matcher(story.getStory());

        String[] circledNumbers = {"①", "②", "③", "④", "⑤", "⑥", "⑦", "⑧", "⑨", "⑩"};

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                builder.append(story.getStory(), lastEnd, matcher.start());
            }
            int blankNumber = Integer.parseInt(matcher.group(1));
            int answerIndex = blankNumber - 1;

            if (answerIndex >= 0 && answerIndex < answers.size()) {
                String answer = answers.get(answerIndex);
                boolean isFilled = answer != null && !answer.isEmpty();
                int start = builder.length();

                if (isFilled) {
                    builder.append(" ").append(answer).append(" ");
                    int end = builder.length();
                    builder.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(
                            new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.light_primary)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    String label = answerIndex < circledNumbers.length
                            ? circledNumbers[answerIndex] : ("(" + blankNumber + ")");
                    builder.append(" ").append(label).append(" ");
                    int end = builder.length();
                    builder.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(
                            new ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.light_secondary)),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    builder.setSpan(new BackgroundColorSpan(0x1A0D9488),
                            start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < story.getStory().length()) {
            builder.append(story.getStory(), lastEnd, story.getStory().length());
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
            if (used.contains(word)) continue;
            Chip chip = makeWordChip(word);
            chip.setOnClickListener(v -> {
                // ── Enhanced scale animation with OvershootInterpolator ──
                animateChipSelect(chip);
                viewModel.fillNextBlank(word);
            });
            chipGroupWords.addView(chip);
            animateChip(chip);
        }
    }

    // ─── Chip factories ─────────────────────────────────────────
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

    // ─── Animations ─────────────────────────────────────────────
    private void animateEnter(View view) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0f);
        view.setTranslationY(20f);
        view.animate().alpha(1f).translationY(0f).setDuration(280L).start();
    }

    private void animateChip(View view) {
        view.setScaleX(0.85f);
        view.setScaleY(0.85f);
        view.setAlpha(0f);
        view.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180L).start();
    }

    /** Enhanced tap-to-fill: scale DOWN quickly, then spring back UP with Overshoot */
    private void animateChipSelect(View view) {
        view.animate()
            .scaleX(0.80f).scaleY(0.80f)
            .setDuration(80L)
            .withEndAction(() ->
                view.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(220L)
                    .setInterpolator(new OvershootInterpolator(2.5f))
                    .start()
            )
            .start();
    }

    private void animateChipRemove(View view) {
        view.animate().scaleX(0.7f).scaleY(0.7f).alpha(0f).setDuration(120L).start();
    }

    /** Single bounce pulse on FAB when tapped */
    private void animateScalePulse(View view) {
        view.animate()
            .scaleX(0.88f).scaleY(0.88f)
            .setDuration(80L)
            .withEndAction(() ->
                view.animate()
                    .scaleX(1f).scaleY(1f)
                    .setDuration(200L)
                    .setInterpolator(new OvershootInterpolator(3f))
                    .start()
            )
            .start();
    }

    /** Gentle repeating pulse for FAB while TTS is active */
    private void animateFabPulseLoop(View view) {
        view.animate()
            .scaleX(1.08f).scaleY(1.08f)
            .setDuration(600L)
            .withEndAction(() -> {
                if (isSpeaking && view.isAttachedToWindow()) {
                    view.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(600L)
                        .withEndAction(() -> {
                            if (isSpeaking && view.isAttachedToWindow()) {
                                animateFabPulseLoop(view);
                            }
                        })
                        .start();
                }
            })
            .start();
    }

    private float dpToPx(float dp) {
        return dp * requireContext().getResources().getDisplayMetrics().density;
    }

    // ─── Lifecycle ───────────────────────────────────────────────
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopShimmerAnimation();
        if (ttsManager != null) {
            ttsManager.shutdown();
            ttsManager = null;
        }
        if (audioWaveView != null) {
            audioWaveView.stopWave();
        }
    }
}
