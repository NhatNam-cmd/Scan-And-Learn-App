package com.example.englishapp.feature.vocabulary;

import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewConfiguration;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.google.android.material.button.MaterialButton;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Màn hình phiên ôn tập Flashcard.
 *
 * Luồng:
 *  1. Load từ theo nguồn (IDs cụ thể hoặc daily review)
 *  2. Hiện mặt trước thẻ (từ + phiên âm)
 *  3. User nhấn "Xem nghĩa" → flip animation → hiện mặt sau
 *  4. User chọn "Đã nhớ" / "Chưa nhớ" → SRS cập nhật → thẻ tiếp theo
 *  5. Hết thẻ → navigate đến màn kết quả
 *
 * Args nhận vào:
 *  - "vocabularyIds" (long[]) nếu từ danh sách cụ thể
 *  - "mode" ("DAILY" | "ALL") nếu là daily review hoặc ôn tổng
 */
@AndroidEntryPoint
public class FlashcardSessionFragment extends Fragment {

    // Mode constants
    public static final String MODE_DAILY = "DAILY";
    public static final String MODE_ALL = "ALL";
    public static final String MODE_IDS = "IDS";

    private FlashcardSessionViewModel viewModel;

    // Card views
    private View cardFront;
    private View cardBack;

    // Front card views
    private TextView tvCardWord;
    private TextView tvCardPhonetic;
    private TextView tvCardSource;

    // Back card views
    private TextView tvBackWord;
    private TextView tvBackMeaning;
    private TextView tvBackExample;
    private TextView tvBackExampleLabel;
    private TextView tvBackNote;
    private TextView tvBackNoteLabel;
    private TextView tvBackMastery;

    // Controls
    private MaterialButton btnFlip;
    private MaterialButton btnRemembered;
    private MaterialButton btnForgot;
    private View layoutActionButtons;

    // Progress
    private ProgressBar progressSession;
    private TextView tvProgressLabel;
    private TextView tvScoreLabel;
    private TextView tvTotalBadge;

    private boolean isShowingBack = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_flashcard_session, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(FlashcardSessionViewModel.class);

        bindViews(view);
        setupListeners(view);
        initSession();
        observeViewModel();
    }

    // ===== INIT =====

    private void bindViews(View view) {
        cardFront = view.findViewById(R.id.card_front);
        cardBack = view.findViewById(R.id.card_back);
        tvCardWord = view.findViewById(R.id.tv_card_word);
        tvCardPhonetic = view.findViewById(R.id.tv_card_phonetic);
        tvCardSource = view.findViewById(R.id.tv_card_source);
        tvBackWord = view.findViewById(R.id.tv_back_word);
        tvBackMeaning = view.findViewById(R.id.tv_back_meaning);
        tvBackExample = view.findViewById(R.id.tv_back_example);
        tvBackExampleLabel = view.findViewById(R.id.tv_back_example_label);
        tvBackNote = view.findViewById(R.id.tv_back_note);
        tvBackNoteLabel = view.findViewById(R.id.tv_back_note_label);
        tvBackMastery = view.findViewById(R.id.tv_back_mastery);
        btnFlip = view.findViewById(R.id.btn_flip);
        btnRemembered = view.findViewById(R.id.btn_remembered);
        btnForgot = view.findViewById(R.id.btn_forgot);
        layoutActionButtons = view.findViewById(R.id.layout_action_buttons);
        progressSession = view.findViewById(R.id.progress_session);
        tvProgressLabel = view.findViewById(R.id.tv_progress_label);
        tvScoreLabel = view.findViewById(R.id.tv_score_label);
        tvTotalBadge = view.findViewById(R.id.tv_total_badge);
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.btn_close).setOnClickListener(v -> confirmExit());

        // Flip sang mặt sau: nhấn nút HOẶC click trực tiếp vào thẻ trước
        btnFlip.setOnClickListener(v -> flipToBack());
        cardFront.setOnClickListener(v -> flipToBack());

        // Flip ngược lại về mặt trước: click vào thẻ sau
        cardBack.setOnClickListener(v -> flipToFront());
        setupBackCardTapToFlip();

        btnRemembered.setOnClickListener(v -> submitAnswer(true));
        btnForgot.setOnClickListener(v -> submitAnswer(false));
    }

    private void setupBackCardTapToFlip() {
        final int touchSlop = ViewConfiguration.get(requireContext()).getScaledTouchSlop();
        cardBack.setOnTouchListener(new View.OnTouchListener() {
            private float downX;
            private float downY;
            private boolean moved;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        moved = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getX() - downX) > touchSlop
                                || Math.abs(event.getY() - downY) > touchSlop) {
                            moved = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!moved) {
                            v.performClick();
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        moved = true;
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void initSession() {
        Bundle args = getArguments();
        if (args == null) {
            viewModel.initDailyReview();
            return;
        }
        long[] ids = args.getLongArray("vocabularyIds");
        String mode = args.getString("mode", MODE_DAILY);

        if (ids != null && ids.length > 0) {
            viewModel.initWithIds(ids);
        } else if (MODE_ALL.equals(mode)) {
            viewModel.initAllUnmastered();
        } else {
            viewModel.initDailyReview();
        }
    }

    // ===== OBSERVE =====

    private void observeViewModel() {
        viewModel.getSessionState().observe(getViewLifecycleOwner(), state -> {
            if (state == FlashcardSessionViewModel.SessionState.FINISHED) {
                navigateToResult();
            } else if (state == FlashcardSessionViewModel.SessionState.ACTIVE) {
                // BUG FIX: Khi data load xong và state chuyển sang ACTIVE,
                // currentIndex observer đã fire trước nhưng state lúc đó còn LOADING
                // → phải trigger showCurrentCard() ở đây mới hiện được thẻ đầu tiên.
                showCurrentCard();
            }
        });

        viewModel.getCurrentIndex().observe(getViewLifecycleOwner(), index -> {
            // Chỉ xử lý khi đang trong phiên ACTIVE (để hiện thẻ tiếp theo khi advance).
            // Thẻ đầu tiên (index=0) được handle bởi sessionState observer ở trên.
            FlashcardSessionViewModel.SessionState state = viewModel.getSessionState().getValue();
            if (state == FlashcardSessionViewModel.SessionState.ACTIVE
                    && index != null && index > 0) {
                showCurrentCard();
            }
        });
    }

    // ===== CARD DISPLAY =====

    private void showCurrentCard() {
        VocabularyEntity word = viewModel.getCurrentWord();
        if (word == null) return;

        isShowingBack = false;

        // Cập nhật mặt trước
        tvCardWord.setText(word.getWord());
        tvCardPhonetic.setText(isBlank(word.getPhonetic()) ? "/.../": word.getPhonetic());
        tvCardSource.setText(formatSource(word.getSourceType()));

        // Reset về mặt trước với animation slide-in
        cardBack.setVisibility(View.GONE);
        cardFront.setVisibility(View.VISIBLE);
        cardFront.setAlpha(0f);
        cardFront.setTranslationX(80f);
        cardFront.animate().alpha(1f).translationX(0f).setDuration(250).start();

        btnFlip.setVisibility(View.VISIBLE);
        layoutActionButtons.setVisibility(View.GONE);

        // Cập nhật progress
        updateProgress();
    }

    private void updateProgress() {
        int total = viewModel.getTotalCount();
        Integer idx = viewModel.getCurrentIndex().getValue();
        int current = (idx == null ? 0 : idx) + 1;
        int remembered = viewModel.getRememberedCount();
        int forgot = viewModel.getForgotCount();

        tvProgressLabel.setText(current + " / " + total);
        tvTotalBadge.setText(total + " từ");
        tvScoreLabel.setText("✅ " + remembered + "  😕 " + forgot);
        progressSession.setProgress(viewModel.getProgressPercent());
    }

    // ===== FLIP ANIMATION =====

    /**
     * Lật sang mặt SAU (hiện nghĩa).
     * Bị chặn nếu đã đang ở mặt sau.
     */
    private void flipToBack() {
        if (isShowingBack) return;
        isShowingBack = true;

        VocabularyEntity word = viewModel.getCurrentWord();
        if (word == null) return;

        // Điền nội dung mặt sau trước khi animate
        tvBackWord.setText(word.getWord());
        tvBackMeaning.setText(word.getMeaning());
        setOptionalField(tvBackExampleLabel, tvBackExample, word.getExampleSentence());
        setOptionalField(tvBackNoteLabel, tvBackNote, word.getNote());
        int level = word.isMastered() ? 5 : Math.min(5, word.getMasteryLevel());
        tvBackMastery.setText("Level " + level + " / 5");

        // Animate: front quay ra 90° → ẩn → back quay vào từ -90°
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardFront, "rotationY", 0f, 90f);
        flipOut.setDuration(200);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Reset rotation của front để lần sau flip về lại đúng
                cardFront.setRotationY(0f);
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.VISIBLE);
                cardBack.setRotationY(-90f);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardBack, "rotationY", -90f, 0f);
                flipIn.setDuration(220);
                flipIn.start();
            }
        });
        flipOut.start();

        // Ẩn nút flip, hiện nút chấm điểm
        btnFlip.setVisibility(View.GONE);
        layoutActionButtons.setVisibility(View.VISIBLE);
        layoutActionButtons.setAlpha(0f);
        layoutActionButtons.animate().alpha(1f).setStartDelay(300).setDuration(200).start();
    }

    /**
     * Lật ngược về mặt TRƯỚC (để xem lại từ).
     * Khi ở mặt sau, user có thể click vào thẻ để xem lại từ trước khi chấm điểm.
     */
    private void flipToFront() {
        if (!isShowingBack) return;
        isShowingBack = false;

        // Animate: back quay ra 90° → ẩn → front quay vào từ -90°
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(cardBack, "rotationY", 0f, -90f);
        flipOut.setDuration(200);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Reset rotation của back để lần sau flip sang lại đúng
                cardBack.setRotationY(0f);
                cardBack.setVisibility(View.GONE);
                cardFront.setVisibility(View.VISIBLE);
                cardFront.setRotationY(90f);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(cardFront, "rotationY", 90f, 0f);
                flipIn.setDuration(220);
                flipIn.start();
            }
        });
        flipOut.start();

        // Hiện lại nút flip, ẩn nút chấm điểm
        layoutActionButtons.setVisibility(View.GONE);
        btnFlip.setVisibility(View.VISIBLE);
        btnFlip.setAlpha(0f);
        btnFlip.animate().alpha(1f).setStartDelay(300).setDuration(200).start();
    }

    // ===== SUBMIT ANSWER =====

    private void submitAnswer(boolean remembered) {
        // Disable nút ngay để tránh double-tap
        btnRemembered.setEnabled(false);
        btnForgot.setEnabled(false);

        // Visual feedback ngắn rồi chuyển thẻ
        View feedbackCard = remembered ? btnRemembered : btnForgot;
        feedbackCard.animate().scaleX(1.08f).scaleY(1.08f).setDuration(80)
                .withEndAction(() -> feedbackCard.animate().scaleX(1f).scaleY(1f).setDuration(80)
                        .withEndAction(() -> {
                            btnRemembered.setEnabled(true);
                            btnForgot.setEnabled(true);
                            viewModel.submitAnswer(remembered);
                        })
                        .start())
                .start();
    }

    // ===== RESULT =====

    private void navigateToResult() {
        int total = viewModel.getTotalCount();
        int remembered = viewModel.getRememberedCount();
        int forgot = viewModel.getForgotCount();
        long[] forgotIds = toLongArray(viewModel.getForgotIds());

        Bundle args = new Bundle();
        args.putInt("total", total);
        args.putInt("remembered", remembered);
        args.putInt("forgot", forgot);
        args.putLongArray("forgotIds", forgotIds);
        NavHostFragment.findNavController(this)
                .navigate(R.id.nav_flashcard_result, args);
    }

    // ===== EXIT CONFIRM =====

    private void confirmExit() {
        Integer idx = viewModel.getCurrentIndex().getValue();
        int done = idx == null ? 0 : idx;
        if (done == 0) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Thoát phiên ôn tập?")
                .setMessage("Bạn đã ôn " + done + " từ. Thoát sẽ không lưu kết quả các từ chưa ôn.")
                .setPositiveButton("Thoát", (d, w) ->
                        NavHostFragment.findNavController(this).navigateUp())
                .setNegativeButton("Tiếp tục ôn", null)
                .show();
    }

    // ===== HELPERS =====

    private void setOptionalField(TextView label, TextView value, String text) {
        boolean visible = !isBlank(text);
        int vis = visible ? View.VISIBLE : View.GONE;
        label.setVisibility(vis);
        value.setVisibility(vis);
        if (visible) value.setText(text);
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private String formatSource(String sourceType) {
        if ("SCAN".equalsIgnoreCase(sourceType)) return "📷 Quét";
        if ("MANUAL".equalsIgnoreCase(sourceType)) return "✏️ Thủ công";
        if ("TOPIC".equalsIgnoreCase(sourceType)) return "📚 Chủ đề";
        return sourceType == null ? "Khác" : sourceType;
    }

    private long[] toLongArray(java.util.List<Long> list) {
        if (list == null) return new long[0];
        long[] arr = new long[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }
}
