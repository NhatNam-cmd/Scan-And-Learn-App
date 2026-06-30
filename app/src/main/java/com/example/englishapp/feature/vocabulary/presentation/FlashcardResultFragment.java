package com.example.englishapp.feature.vocabulary.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Màn kết quả sau khi hoàn thành phiên ôn tập Flashcard.
 *
 * Nhận args:
 *  - total     (int)    tổng số từ
 *  - remembered(int)    số từ đã nhớ
 *  - forgot    (int)    số từ chưa nhớ
 *  - forgotIds (long[]) id các từ chưa nhớ (để ôn lại)
 */
@AndroidEntryPoint
public class FlashcardResultFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcard_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        int total      = args != null ? args.getInt("total", 0) : 0;
        int remembered = args != null ? args.getInt("remembered", 0) : 0;
        int forgot     = args != null ? args.getInt("forgot", 0) : 0;
        long[] forgotIds = args != null ? args.getLongArray("forgotIds") : null;

        TextView tvEmoji    = view.findViewById(R.id.tv_result_emoji);
        TextView tvTitle    = view.findViewById(R.id.tv_result_title);
        TextView tvSubtitle = view.findViewById(R.id.tv_result_subtitle);
        TextView tvStatRem  = view.findViewById(R.id.tv_stat_remembered);
        TextView tvStatForg = view.findViewById(R.id.tv_stat_forgot);
        TextView tvAccuracy = view.findViewById(R.id.tv_accuracy);
        ProgressBar progressAccuracy = view.findViewById(R.id.progress_accuracy);
        MaterialButton btnReviewAgain = view.findViewById(R.id.btn_review_again);
        MaterialButton btnBack        = view.findViewById(R.id.btn_back_to_vocabulary);

        // Tính % chính xác
        int accuracy = total > 0 ? (int) ((remembered / (float) total) * 100) : 0;

        // Chọn nội dung theo kết quả
        String emoji, title, subtitle;
        if (accuracy == 100) {
            emoji = "🏆"; title = "Xuất sắc!"; subtitle = "Bạn nhớ tất cả " + total + " từ trong phiên này!";
        } else if (accuracy >= 75) {
            emoji = "🎉"; title = "Tuyệt vời!"; subtitle = "Bạn nhớ được " + remembered + "/" + total + " từ!";
        } else if (accuracy >= 50) {
            emoji = "💪"; title = "Cố lên!"; subtitle = "Còn " + forgot + " từ cần ôn thêm. Đừng bỏ cuộc!";
        } else {
            emoji = "📖"; title = "Cần ôn thêm"; subtitle = "Hãy luyện lại " + forgot + " từ chưa nhớ nhé!";
        }

        tvEmoji.setText(emoji);
        tvTitle.setText(title);
        tvSubtitle.setText(subtitle);
        tvStatRem.setText(String.valueOf(remembered));
        tvStatForg.setText(String.valueOf(forgot));
        tvAccuracy.setText(accuracy + "%");

        // Animate progress bar
        progressAccuracy.setMax(100);
        progressAccuracy.setProgress(0);
        progressAccuracy.animate(); // trigger redraw
        view.post(() -> progressAccuracy.setProgress(accuracy));

        // Nút ôn lại từ chưa nhớ (chỉ hiện khi có từ chưa nhớ)
        if (forgot > 0 && forgotIds != null && forgotIds.length > 0) {
            btnReviewAgain.setVisibility(View.VISIBLE);
            btnReviewAgain.setText("🔁  Ôn lại " + forgot + " từ chưa nhớ");
            final long[] ids = forgotIds;
            btnReviewAgain.setOnClickListener(v -> {
                Bundle newArgs = new Bundle();
                newArgs.putLongArray("vocabularyIds", ids);
                NavHostFragment.findNavController(this)
                        .navigate(R.id.nav_flashcard_session, newArgs);
            });
        } else {
            btnReviewAgain.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.nav_vocabulary));
    }
}
