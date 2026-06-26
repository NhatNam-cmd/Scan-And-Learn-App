package com.example.englishapp.feature.vocabulary;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Màn chi tiết từ vựng với chế độ Flashcard (SRS chuẩn).
 *
 * Luồng:
 *   QUESTION phase → user tự nhớ nghĩa → bấm "Lật thẻ"
 *   ANSWER phase   → hiện nghĩa + ví dụ → user chọn "Đã nhớ" / "Chưa nhớ"
 *   → SRS engine cập nhật lịch ôn
 */
@AndroidEntryPoint
public class VocabularyDetailFragment extends Fragment {

    /** Trạng thái flashcard */
    private enum FlashcardPhase { QUESTION, ANSWER }

    private VocabularyViewModel viewModel;
    private VocabularyEntity currentVocabulary;
    private FlashcardPhase currentPhase = FlashcardPhase.QUESTION;
    private boolean hasReviewedThisSession = false;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    // Views
    private View layoutFlashcardQuestion;
    private View layoutFlashcardAnswer;
    private MaterialButton btnReveal;
    private MaterialButton btnReviewCorrect;
    private MaterialButton btnReviewWrong;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);
        long vocabularyId = requireArguments().getLong("vocabularyId");

        // Bind views
        layoutFlashcardQuestion = view.findViewById(R.id.layout_flashcard_question);
        layoutFlashcardAnswer = view.findViewById(R.id.layout_flashcard_answer);
        btnReveal = view.findViewById(R.id.btn_reveal);
        btnReviewCorrect = view.findViewById(R.id.btn_review_correct);
        btnReviewWrong = view.findViewById(R.id.btn_review_wrong);
        MaterialButton btnEdit = view.findViewById(R.id.btn_edit);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);

        // Listeners
        btnReveal.setOnClickListener(v -> revealAnswer(view));
        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnReviewCorrect.setOnClickListener(v -> recordReview(true));
        btnReviewWrong.setOnClickListener(v -> recordReview(false));

        // Quan sát từ vựng
        viewModel.observeVocabulary(vocabularyId).observe(getViewLifecycleOwner(), vocabulary -> {
            currentVocabulary = vocabulary;
            if (vocabulary == null) {
                Toast.makeText(requireContext(), "Không tìm thấy từ vựng", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
                return;
            }
            // Chỉ reset về phase QUESTION khi lần đầu load (chưa lật thẻ)
            if (currentPhase == FlashcardPhase.QUESTION) {
                bindHeaderInfo(view, vocabulary);
                showQuestionPhase();
            } else {
                // Nếu đã lật thẻ, chỉ cập nhật header (SRS status có thể thay đổi)
                bindHeaderInfo(view, vocabulary);
                bindAnswerContent(view, vocabulary);
            }
        });
    }

    // ====================== FLASHCARD PHASES ======================

    /**
     * Phase 1 — Câu hỏi: chỉ hiện từ + phiên âm, ẩn toàn bộ nội dung đáp án
     */
    private void showQuestionPhase() {
        currentPhase = FlashcardPhase.QUESTION;
        layoutFlashcardQuestion.setVisibility(View.VISIBLE);
        layoutFlashcardAnswer.setVisibility(View.GONE);
    }

    /**
     * Phase 2 — Đáp án: animate lật thẻ, hiện nghĩa + ví dụ + nút chấm điểm
     */
    private void revealAnswer(View rootView) {
        currentPhase = FlashcardPhase.ANSWER;

        // Animate: flip out question card, flip in answer card
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(layoutFlashcardQuestion, "rotationY", 0f, 90f);
        flipOut.setDuration(180);
        flipOut.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                layoutFlashcardQuestion.setVisibility(View.GONE);
                layoutFlashcardAnswer.setVisibility(View.VISIBLE);
                layoutFlashcardAnswer.setRotationY(-90f);
                bindAnswerContent(rootView, currentVocabulary);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(layoutFlashcardAnswer, "rotationY", -90f, 0f);
                flipIn.setDuration(200);
                flipIn.start();
            }
        });
        flipOut.start();
    }

    // ====================== DATA BINDING ======================

    /** Bind header section (luôn hiện bất kể phase) */
    private void bindHeaderInfo(View view, VocabularyEntity vocabulary) {
        ((TextView) view.findViewById(R.id.tv_word)).setText(vocabulary.getWord());
        ((TextView) view.findViewById(R.id.tv_source)).setText(formatSource(vocabulary.getSourceType()));
        ((TextView) view.findViewById(R.id.tv_phonetic)).setText(
                isBlank(vocabulary.getPhonetic()) ? "/.../": vocabulary.getPhonetic());
        ((ProgressBar) view.findViewById(R.id.progress_mastery)).setProgress(
                vocabulary.isMastered() ? 100 : Math.min(100, vocabulary.getMasteryLevel() * 20));
        ((TextView) view.findViewById(R.id.tv_srs_status)).setText(buildSrsStatus(vocabulary));
        ((TextView) view.findViewById(R.id.tv_meta)).setText(
                "Tạo: " + dateFormat.format(new Date(vocabulary.getCreatedAt()))
                        + "\nCập nhật: " + dateFormat.format(new Date(vocabulary.getUpdatedAt())));
    }

    /** Bind nội dung phase 2 (nghĩa, ví dụ, ghi chú) */
    private void bindAnswerContent(View view, VocabularyEntity vocabulary) {
        ((TextView) view.findViewById(R.id.tv_meaning)).setText(vocabulary.getMeaning());
        bindOptionalSection(view.findViewById(R.id.layout_example),
                view.findViewById(R.id.tv_example), vocabulary.getExampleSentence());
        bindOptionalSection(view.findViewById(R.id.layout_note),
                view.findViewById(R.id.tv_note), vocabulary.getNote());

        // Cập nhật trạng thái nút sau khi đã review trong phiên này
        if (hasReviewedThisSession) {
            btnReviewCorrect.setText("✅  Đã nhớ! (đã ghi nhận)");
            btnReviewCorrect.setEnabled(false);
            btnReviewWrong.setEnabled(false);
        } else {
            btnReviewCorrect.setText("✅  Đã nhớ!");
            btnReviewCorrect.setEnabled(true);
            btnReviewWrong.setEnabled(true);
        }
    }

    private void bindOptionalSection(View section, TextView textView, String value) {
        boolean visible = !isBlank(value);
        section.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) textView.setText(value);
    }

    // ====================== SRS REVIEW ======================

    /**
     * Ghi nhận kết quả ôn tập và cập nhật SRS.
     * Sau khi user chấm điểm, disable nút để tránh chấm 2 lần trong cùng 1 phiên.
     */
    private void recordReview(boolean isCorrect) {
        if (currentVocabulary == null || hasReviewedThisSession) return;
        hasReviewedThisSession = true;
        viewModel.recordReview(currentVocabulary, isCorrect);

        String message = isCorrect
                ? "🎉 Tuyệt! Lịch ôn sẽ được lùi ra xa hơn."
                : "💪 Không sao! Từ này sẽ xuất hiện sớm hơn để ôn lại.";
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

        // Disable nút, đổi màu để phản hồi
        btnReviewCorrect.setEnabled(false);
        btnReviewWrong.setEnabled(false);
        if (isCorrect) {
            btnReviewCorrect.setText("✅  Đã ghi nhận — Nhớ tốt!");
        } else {
            btnReviewWrong.setText("💪  Đã ghi nhận — Sẽ ôn lại!");
        }
    }

    // ====================== EDIT / DELETE ======================

    private void showEditDialog() {
        if (currentVocabulary == null) return;

        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 8, padding, 0);

        EditText wordInput = createInput("Từ", currentVocabulary.getWord());
        EditText phoneticInput = createInput("Phiên âm", currentVocabulary.getPhonetic());
        EditText meaningInput = createInput("Nghĩa", currentVocabulary.getMeaning());
        EditText exampleInput = createInput("Ví dụ", currentVocabulary.getExampleSentence());
        EditText noteInput = createInput("Ghi chú", currentVocabulary.getNote());
        meaningInput.setMinLines(3);
        noteInput.setMinLines(2);

        container.addView(wordInput);
        container.addView(phoneticInput);
        container.addView(meaningInput);
        container.addView(exampleInput);
        container.addView(noteInput);

        new AlertDialog.Builder(requireContext())
                .setTitle("Sửa từ vựng")
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String word = wordInput.getText().toString().trim();
                    String meaning = meaningInput.getText().toString().trim();
                    if (word.isEmpty() || meaning.isEmpty()) {
                        Toast.makeText(requireContext(), "Từ và nghĩa không được để trống", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentVocabulary.setWord(word);
                    currentVocabulary.setPhonetic(blankToNull(phoneticInput.getText().toString()));
                    currentVocabulary.setMeaning(meaning);
                    currentVocabulary.setExampleSentence(blankToNull(exampleInput.getText().toString()));
                    currentVocabulary.setNote(blankToNull(noteInput.getText().toString()));
                    viewModel.updateVocabulary(currentVocabulary);
                    Toast.makeText(requireContext(), "Đã cập nhật từ vựng", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private EditText createInput(String hint, String value) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setSingleLine(false);
        input.setText(value == null ? "" : value);
        return input;
    }

    private void confirmDelete() {
        if (currentVocabulary == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa từ vựng")
                .setMessage("Xóa \"" + currentVocabulary.getWord() + "\" khỏi danh sách?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa", (dialog, which) -> {
                    viewModel.deleteVocabulary(currentVocabulary);
                    Toast.makeText(requireContext(), "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();
                    NavHostFragment.findNavController(this).navigateUp();
                })
                .show();
    }

    // ====================== HELPERS ======================

    private String buildSrsStatus(VocabularyEntity vocabulary) {
        if (vocabulary.isMastered()) {
            return "✅ Đã thuộc • Level " + vocabulary.getMasteryLevel();
        }
        // Từ mới: chưa học lần nào
        if (vocabulary.getMasteryLevel() == 0 && vocabulary.getNextReviewDate() == 0L) {
            return "🆕 Từ mới — chưa học lần nào";
        }
        long nextReviewDate = vocabulary.getNextReviewDate();
        String reviewText = nextReviewDate <= 0L || nextReviewDate <= System.currentTimeMillis()
                ? "🔥 Cần ôn hôm nay"
                : "📅 Ôn tiếp: " + dateFormat.format(new Date(nextReviewDate));
        return reviewText + " • Level " + vocabulary.getMasteryLevel();
    }

    private String formatSource(String sourceType) {
        if ("SCAN".equalsIgnoreCase(sourceType)) return "Quét";
        if ("MANUAL".equalsIgnoreCase(sourceType)) return "Thủ công";
        if ("TOPIC".equalsIgnoreCase(sourceType)) return "Chủ đề";
        return sourceType == null ? "Khác" : sourceType;
    }

    private String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
