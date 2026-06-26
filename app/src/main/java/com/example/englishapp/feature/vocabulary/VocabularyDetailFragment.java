package com.example.englishapp.feature.vocabulary;

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

@AndroidEntryPoint
public class VocabularyDetailFragment extends Fragment {

    private VocabularyViewModel viewModel;
    private VocabularyEntity currentVocabulary;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

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

        MaterialButton btnEdit = view.findViewById(R.id.btn_edit);
        MaterialButton btnDelete = view.findViewById(R.id.btn_delete);
        MaterialButton btnReviewCorrect = view.findViewById(R.id.btn_review_correct);
        MaterialButton btnReviewWrong = view.findViewById(R.id.btn_review_wrong);

        btnEdit.setOnClickListener(v -> showEditDialog());
        btnDelete.setOnClickListener(v -> confirmDelete());
        btnReviewCorrect.setOnClickListener(v -> recordQuickReview(true));
        btnReviewWrong.setOnClickListener(v -> recordQuickReview(false));

        viewModel.observeVocabulary(vocabularyId).observe(getViewLifecycleOwner(), vocabulary -> {
            currentVocabulary = vocabulary;
            if (vocabulary == null) {
                Toast.makeText(requireContext(), "Không tìm thấy từ vựng", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
                return;
            }
            bindVocabulary(view, vocabulary);
        });
    }

    private void bindVocabulary(View view, VocabularyEntity vocabulary) {
        ((TextView) view.findViewById(R.id.tv_word)).setText(vocabulary.getWord());
        ((TextView) view.findViewById(R.id.tv_source)).setText(formatSource(vocabulary.getSourceType()));
        ((TextView) view.findViewById(R.id.tv_phonetic)).setText(
                isBlank(vocabulary.getPhonetic()) ? "/.../" : vocabulary.getPhonetic());
        ((TextView) view.findViewById(R.id.tv_meaning)).setText(vocabulary.getMeaning());
        ((ProgressBar) view.findViewById(R.id.progress_mastery)).setProgress(
                vocabulary.isMastered() ? 100 : Math.min(100, vocabulary.getMasteryLevel() * 20));

        bindOptionalSection(view.findViewById(R.id.layout_example),
                view.findViewById(R.id.tv_example), vocabulary.getExampleSentence());
        bindOptionalSection(view.findViewById(R.id.layout_note),
                view.findViewById(R.id.tv_note), vocabulary.getNote());

        ((TextView) view.findViewById(R.id.tv_srs_status)).setText(buildSrsStatus(vocabulary));
        ((TextView) view.findViewById(R.id.tv_meta)).setText(
                "Tạo: " + dateFormat.format(new Date(vocabulary.getCreatedAt()))
                        + "\nCập nhật: " + dateFormat.format(new Date(vocabulary.getUpdatedAt())));
    }

    private void bindOptionalSection(View section, TextView textView, String value) {
        boolean visible = !isBlank(value);
        section.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) textView.setText(value);
    }

    private String buildSrsStatus(VocabularyEntity vocabulary) {
        if (vocabulary.isMastered()) {
            return "Đã thuộc • Level " + vocabulary.getMasteryLevel();
        }
        long nextReviewDate = vocabulary.getNextReviewDate();
        String reviewText = nextReviewDate <= 0L || nextReviewDate <= System.currentTimeMillis()
                ? "Cần ôn hôm nay"
                : "Ôn tiếp: " + dateFormat.format(new Date(nextReviewDate));
        return reviewText + " • Level " + vocabulary.getMasteryLevel();
    }

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

    private void recordQuickReview(boolean isCorrect) {
        if (currentVocabulary == null) return;
        viewModel.recordReview(currentVocabulary, isCorrect);
        Toast.makeText(requireContext(),
                isCorrect ? "Đã tăng tiến độ ôn tập" : "Đã lùi lịch ôn để luyện lại",
                Toast.LENGTH_SHORT).show();
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
