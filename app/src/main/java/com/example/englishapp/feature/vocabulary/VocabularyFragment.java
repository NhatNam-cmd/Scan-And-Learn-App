package com.example.englishapp.feature.vocabulary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyFragment extends Fragment {

    private VocabularyViewModel viewModel;
    private VocabularyAdapter adapter;
    private final List<VocabularyEntity> allWords = new ArrayList<>();
    private String query = "";
    private int selectedFilterId = R.id.chip_all;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_vocabulary);
        View emptyView = view.findViewById(R.id.layout_empty);
        TextView summary = view.findViewById(R.id.tv_summary);
        TextView totalCount = view.findViewById(R.id.tv_total_count);
        TextView dueCount = view.findViewById(R.id.tv_due_count);
        TextView masteredCount = view.findViewById(R.id.tv_mastered_count);
        SearchView searchView = view.findViewById(R.id.search_view);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filter);
        View addButton = view.findViewById(R.id.btn_add_word);

        adapter = new VocabularyAdapter(vocabulary -> {
            Bundle args = new Bundle();
            args.putLong("vocabularyId", vocabulary.getVocabularyId());
            NavHostFragment.findNavController(this).navigate(R.id.nav_vocabulary_detail, args);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        attachSwipeToDelete(recyclerView);
        addButton.setOnClickListener(v -> showAddWordDialog());

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                query = text == null ? "" : text;
                applyFilters(view, emptyView, summary, totalCount, dueCount, masteredCount);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                query = text == null ? "" : text;
                applyFilters(view, emptyView, summary, totalCount, dueCount, masteredCount);
                return true;
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedFilterId = checkedIds.isEmpty() ? R.id.chip_all : checkedIds.get(0);
            applyFilters(view, emptyView, summary, totalCount, dueCount, masteredCount);
        });

        viewModel.getAllVocabularies().observe(getViewLifecycleOwner(), words -> {
            allWords.clear();
            if (words != null) allWords.addAll(words);
            applyFilters(view, emptyView, summary, totalCount, dueCount, masteredCount);
        });
    }

    private void attachSwipeToDelete(RecyclerView recyclerView) {
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                VocabularyEntity item = adapter.getItem(viewHolder.getAdapterPosition());
                if (item == null) {
                    adapter.notifyDataSetChanged();
                    return;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Xóa từ vựng")
                        .setMessage("Xóa \"" + item.getWord() + "\" khỏi danh sách?")
                        .setNegativeButton("Hủy", (dialog, which) -> adapter.notifyDataSetChanged())
                        .setPositiveButton("Xóa", (dialog, which) -> {
                            viewModel.deleteVocabulary(item);
                            Toast.makeText(requireContext(), "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();
                        })
                        .setOnCancelListener(dialog -> adapter.notifyDataSetChanged())
                        .show();
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(recyclerView);
    }

    private void applyFilters(View root, View emptyView, TextView summary,
                              TextView totalCount, TextView dueCount, TextView masteredCount) {
        String normalizedQuery = query.trim().toLowerCase();
        long now = System.currentTimeMillis();
        List<VocabularyEntity> filtered = new ArrayList<>();

        for (VocabularyEntity word : allWords) {
            if (!matchesFilter(word, now)) continue;
            if (!normalizedQuery.isEmpty() && !matchesQuery(word, normalizedQuery)) continue;
            filtered.add(word);
        }

        adapter.submitList(filtered);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        int mastered = 0;
        int due = 0;
        int unmastered = 0;
        for (VocabularyEntity word : allWords) {
            if (word.isMastered()) mastered++;
            if (!word.isMastered()) unmastered++;
            if (!word.isMastered() && word.getNextReviewDate() <= now) due++;
        }
        totalCount.setText(String.valueOf(allWords.size()));
        dueCount.setText(String.valueOf(due));
        masteredCount.setText(String.valueOf(mastered));
        summary.setText(filtered.size() + " đang hiển thị • " + due + " cần ôn hôm nay");
        ((TextView) root.findViewById(R.id.chip_all)).setText("Tất cả " + allWords.size());
        ((TextView) root.findViewById(R.id.chip_due)).setText("Cần ôn " + due);
        ((TextView) root.findViewById(R.id.chip_unmastered)).setText("Chưa thuộc " + unmastered);
        ((TextView) root.findViewById(R.id.chip_mastered)).setText("Đã thuộc " + mastered);
    }

    private boolean matchesFilter(VocabularyEntity word, long now) {
        if (selectedFilterId == R.id.chip_mastered) {
            return word.isMastered();
        }
        if (selectedFilterId == R.id.chip_unmastered) {
            return !word.isMastered();
        }
        if (selectedFilterId == R.id.chip_due) {
            return !word.isMastered() && word.getNextReviewDate() <= now;
        }
        return true;
    }

    private boolean matchesQuery(VocabularyEntity word, String normalizedQuery) {
        return contains(word.getWord(), normalizedQuery)
                || contains(word.getMeaning(), normalizedQuery)
                || contains(word.getPhonetic(), normalizedQuery)
                || contains(word.getNote(), normalizedQuery);
    }

    private boolean contains(String value, String normalizedQuery) {
        return value != null && value.toLowerCase().contains(normalizedQuery);
    }

    private void showAddWordDialog() {
        LinearLayout container = new LinearLayout(requireContext());
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, 8, padding, 0);

        EditText wordInput = createInput("Từ mới", null);
        EditText phoneticInput = createInput("Phiên âm", null);
        EditText meaningInput = createInput("Nghĩa", null);
        EditText exampleInput = createInput("Ví dụ", null);
        EditText noteInput = createInput("Ghi chú cá nhân", null);
        meaningInput.setMinLines(3);
        noteInput.setMinLines(2);

        container.addView(wordInput);
        container.addView(phoneticInput);
        container.addView(meaningInput);
        container.addView(exampleInput);
        container.addView(noteInput);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Thêm từ thủ công")
                .setView(container)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Lưu", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String word = wordInput.getText().toString().trim();
            String meaning = meaningInput.getText().toString().trim();
            if (word.isEmpty() || meaning.isEmpty()) {
                Toast.makeText(requireContext(), "Từ và nghĩa không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            VocabularyEntity existing = findExistingWord(word);
            if (existing != null) {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Từ đã tồn tại")
                        .setMessage("\"" + existing.getWord() + "\" đã có trong danh sách.")
                        .setNegativeButton("Mở từ này", (duplicateDialog, which) -> {
                            dialog.dismiss();
                            Bundle args = new Bundle();
                            args.putLong("vocabularyId", existing.getVocabularyId());
                            NavHostFragment.findNavController(this)
                                    .navigate(R.id.nav_vocabulary_detail, args);
                        })
                        .setPositiveButton("Vẫn lưu mới", (duplicateDialog, which) -> {
                            saveManualWord(wordInput, meaningInput, phoneticInput, exampleInput, noteInput);
                            dialog.dismiss();
                        })
                        .show();
                return;
            }
            saveManualWord(wordInput, meaningInput, phoneticInput, exampleInput, noteInput);
            dialog.dismiss();
        }));
        dialog.show();
    }

    private VocabularyEntity findExistingWord(String word) {
        for (VocabularyEntity item : allWords) {
            if (item.getWord().equalsIgnoreCase(word.trim())) {
                return item;
            }
        }
        return null;
    }

    private void saveManualWord(EditText wordInput, EditText meaningInput, EditText phoneticInput,
                                EditText exampleInput, EditText noteInput) {
        viewModel.addManualVocabulary(
                wordInput.getText().toString(),
                meaningInput.getText().toString(),
                phoneticInput.getText().toString(),
                exampleInput.getText().toString(),
                noteInput.getText().toString()
        );
        Toast.makeText(requireContext(), "Đã thêm từ mới", Toast.LENGTH_SHORT).show();
    }

    private EditText createInput(String hint, String value) {
        EditText input = new EditText(requireContext());
        input.setHint(hint);
        input.setSingleLine(false);
        input.setText(value == null ? "" : value);
        return input;
    }
}
