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

import com.example.englishapp.R;
import com.example.englishapp.feature.story.domain.StoryGameData;

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
        TextView tvScore = view.findViewById(R.id.tv_result_score);
        TextView tvExp = view.findViewById(R.id.tv_result_exp);
        TextView tvMessage = view.findViewById(R.id.tv_result_message);

        StoryGameData story = viewModel.getCurrentStory().getValue();
        int total = story == null || story.getBlanks() == null ? 0 : story.getBlanks().size();
        Integer score = viewModel.getScore().getValue();
        int correct = score == null ? 0 : score;
        int exp = correct * 10;

        tvScore.setText(correct + " / " + total + " câu đúng");
        tvExp.setText("EXP +" + exp);

        // Dynamic motivational message based on score
        float ratio = total > 0 ? (float) correct / total : 0f;
        String message;
        if (ratio >= 1f) {
            message = "🎉 Hoàn hảo! Bạn đã điền đúng tất cả! Tuyệt vời!";
        } else if (ratio >= 0.7f) {
            message = "💪 Rất tốt! Luyện tập thêm để đạt điểm hoàn hảo nhé!";
        } else if (ratio >= 0.5f) {
            message = "📖 Tiếp tục cố gắng! Ôn thêm những từ chưa thuộc nhé.";
        } else {
            message = "🌱 Đừng nản! Mỗi lần luyện tập là một bước tiến bộ.";
        }
        tvMessage.setText(message);

        // Animate score text in with delay
        tvScore.setAlpha(0f);
        tvScore.setScaleX(0.5f);
        tvScore.setScaleY(0.5f);
        tvScore.animate().alpha(1f).scaleX(1f).scaleY(1f).setStartDelay(300L).setDuration(400L).start();

        view.findViewById(R.id.btn_story_home).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack(R.id.nav_story, false));
        view.findViewById(R.id.btn_play_again).setOnClickListener(v -> {
            viewModel.prepareNewStory();
            NavHostFragment.findNavController(this).navigate(R.id.nav_story_word_selection);
        });
    }

    private void animateEnter(View view) {
        view.setAlpha(0f);
        view.setScaleX(0.96f);
        view.setScaleY(0.96f);
        view.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(300L).start();
    }
}
