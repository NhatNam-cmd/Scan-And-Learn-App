package com.example.englishapp.feature.story.presentation;

import android.animation.ObjectAnimator;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        TextView tvScore   = view.findViewById(R.id.tv_result_score);
        TextView tvExp     = view.findViewById(R.id.tv_result_exp);
        TextView tvMessage = view.findViewById(R.id.tv_result_message);
        TextView tvResultStory = view.findViewById(R.id.tv_result_story);

        StoryGameData story = viewModel.getCurrentStory().getValue();
        List<String> userAnswers = viewModel.getAnswers().getValue();
        int total   = (story == null || story.getBlanks() == null) ? 0 : story.getBlanks().size();
        Integer score = viewModel.getScore().getValue();
        int correct = score == null ? 0 : score;
        int exp = correct * 10;

        tvScore.setText(correct + " / " + total + " câu đúng");
        tvExp.setText("EXP +" + exp);

        // Dynamic motivational message
        float ratio = total > 0 ? (float) correct / total : 0f;
        String message;
        if (ratio >= 1f)      message = "🎉 Hoàn hảo! Bạn đã điền đúng tất cả! Tuyệt vời!";
        else if (ratio >= 0.7f) message = "💪 Rất tốt! Luyện tập thêm để đạt điểm hoàn hảo nhé!";
        else if (ratio >= 0.5f) message = "📖 Tiếp tục cố gắng! Ôn thêm những từ chưa thuộc nhé.";
        else                    message = "🌱 Đừng nản! Mỗi lần luyện tập là một bước tiến bộ.";
        tvMessage.setText(message);

        // Build colored story review text
        if (story != null && userAnswers != null) {
            tvResultStory.setText(buildColoredStoryText(story, userAnswers));

            // Shake animation for all wrong answers
            scheduleShakeForWrongAnswers(view, story, userAnswers);
        }

        // Animate score pop-in
        tvScore.setAlpha(0f);
        tvScore.setScaleX(0.4f);
        tvScore.setScaleY(0.4f);
        tvScore.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(300L).setDuration(450L).start();

        // Slide-in the story review panel
        View storyPanel = view.findViewWithTag("story_panel");
        // We animate the TextView itself for simplicity
        tvResultStory.setAlpha(0f);
        tvResultStory.setTranslationY(16f);
        tvResultStory.animate().alpha(1f).translationY(0f)
                .setStartDelay(500L).setDuration(350L).start();

        view.findViewById(R.id.btn_story_home).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.nav_story, false));
        view.findViewById(R.id.btn_play_again).setOnClickListener(v -> {
            viewModel.prepareNewStory();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
        });
    }

    // ─── Colored story text ──────────────────────────────────────
    /**
     * Builds a SpannableString where:
     *  - Correctly answered blanks are tinted GREEN with bold
     *  - Wrongly answered blanks are tinted RED with strikethrough on the wrong word
     *    followed by the correct word in green italics
     */
    private SpannableStringBuilder buildColoredStoryText(StoryGameData story, List<String> userAnswers) {
        SpannableStringBuilder sb = new SpannableStringBuilder();

        int colorCorrect   = ContextCompat.getColor(requireContext(), R.color.result_correct);
        int colorWrong     = ContextCompat.getColor(requireContext(), R.color.result_wrong);
        int bgCorrect      = ContextCompat.getColor(requireContext(), R.color.result_correct_bg);
        int bgWrong        = ContextCompat.getColor(requireContext(), R.color.result_wrong_bg);

        Pattern blankPattern = Pattern.compile("\\[BLANK_(\\d+)\\]");
        Matcher matcher = blankPattern.matcher(story.getStory());

        int lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                sb.append(story.getStory(), lastEnd, matcher.start());
            }

            int blankNumber = Integer.parseInt(matcher.group(1));
            int idx = blankNumber - 1;

            if (idx >= 0 && idx < story.getBlanks().size()) {
                StoryBlank blank = story.getBlanks().get(idx);
                String correctWord = blank.getWord();
                String userWord = (userAnswers != null && idx < userAnswers.size())
                        ? userAnswers.get(idx) : "";
                boolean isCorrect = correctWord.equalsIgnoreCase(userWord);

                int spanStart = sb.length();
                if (isCorrect) {
                    // ✅ Green + bold + underline
                    sb.append(" ").append(correctWord).append(" ");
                    int spanEnd = sb.length();
                    sb.setSpan(new ForegroundColorSpan(colorCorrect),
                            spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new BackgroundColorSpan(bgCorrect),
                            spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new StyleSpan(Typeface.BOLD),
                            spanStart, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    // ❌ Show user's wrong word in red with strikethrough
                    String display = userWord.isEmpty() ? "(trống)" : userWord;
                    sb.append(" ").append(display);
                    int wrongEnd = sb.length();
                    sb.setSpan(new ForegroundColorSpan(colorWrong),
                            spanStart, wrongEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new BackgroundColorSpan(bgWrong),
                            spanStart, wrongEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new StrikethroughSpan(),
                            spanStart + 1, wrongEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Show correct word right after in green italics
                    int correctStart = sb.length();
                    sb.append("→").append(correctWord).append(" ");
                    int correctEnd = sb.length();
                    sb.setSpan(new ForegroundColorSpan(colorCorrect),
                            correctStart, correctEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new StyleSpan(Typeface.ITALIC),
                            correctStart, correctEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    sb.setSpan(new UnderlineSpan(),
                            correctStart, correctEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd < story.getStory().length()) {
            sb.append(story.getStory(), lastEnd, story.getStory().length());
        }
        return sb;
    }

    // ─── Shake animation for wrong answers ───────────────────────
    /**
     * After a short delay, shake the story text view for each wrong answer detected.
     * A single shake on the containing TextView is enough as a cue.
     */
    private void scheduleShakeForWrongAnswers(View rootView,
            StoryGameData story, List<String> userAnswers) {
        boolean hasWrong = false;
        if (story.getBlanks() != null) {
            for (int i = 0; i < story.getBlanks().size() && i < userAnswers.size(); i++) {
                if (!story.getBlanks().get(i).getWord().equalsIgnoreCase(userAnswers.get(i))) {
                    hasWrong = true;
                    break;
                }
            }
        }
        if (!hasWrong) return;

        TextView tvResultStory = rootView.findViewById(R.id.tv_result_story);
        tvResultStory.postDelayed(() -> shakeView(tvResultStory), 750L);
    }

    /**
     * Shake animation: translates view horizontally through keyframes.
     * Mimics iOS-style error shake.
     */
    private void shakeView(View view) {
        ObjectAnimator shaker = ObjectAnimator.ofFloat(view, "translationX",
                0f, -14f, 14f, -10f, 10f, -6f, 6f, -3f, 3f, 0f);
        shaker.setDuration(550L);
        shaker.start();
    }

    // ─── Enter animation ─────────────────────────────────────────
    private void animateEnter(View view) {
        view.setAlpha(0f);
        view.setScaleX(0.96f);
        view.setScaleY(0.96f);
        view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300L).start();
    }
}
