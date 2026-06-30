package com.example.englishapp.feature.vocabulary.presentation;

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
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.TopicEntity;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyFragment extends Fragment {

    private VocabularyViewModel viewModel;
    private VocabularyAdapter adapter;
    private final List<VocabularyEntity> allWords = new ArrayList<>();
    private final List<VocabularyEntity> visibleBaseWords = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private List<TopicEntity> topics = new ArrayList<>();
    private String query = "";
    private int selectedFilterId = R.id.chip_all;
    private LiveData<List<VocabularyEntity>> searchSource;
    private Observer<List<VocabularyEntity>> searchObserver;

    private View rootView;
    private View emptyView;
    private View batchActions;
    private View reviewBanner;
    private TextView summary;
    private TextView totalCount;
    private TextView newCount;
    private TextView dueCount;
    private TextView masteredCount;
    private TextView selectedCount;
    private TextView bannerSubtitle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rootView = view;
        viewModel = new ViewModelProvider(this).get(VocabularyViewModel.class);

        RecyclerView recyclerView = view.findViewById(R.id.rv_vocabulary);
        emptyView = view.findViewById(R.id.layout_empty);
        batchActions = view.findViewById(R.id.layout_batch_actions);
        reviewBanner = view.findViewById(R.id.layout_review_banner);
        summary = view.findViewById(R.id.tv_summary);
        totalCount = view.findViewById(R.id.tv_total_count);
        newCount = view.findViewById(R.id.tv_new_count);
        dueCount = view.findViewById(R.id.tv_due_count);
        masteredCount = view.findViewById(R.id.tv_mastered_count);
        selectedCount = view.findViewById(R.id.tv_selected_count);
        bannerSubtitle = view.findViewById(R.id.tv_banner_subtitle);
        SearchView searchView = view.findViewById(R.id.search_view);
        ChipGroup chipGroup = view.findViewById(R.id.chip_group_filter);

        adapter = new VocabularyAdapter(new VocabularyAdapter.OnVocabularyInteractionListener() {
            @Override
            public void onVocabularyClicked(VocabularyEntity vocabulary) {
                if (isSelectionMode()) {
                    toggleSelection(vocabulary);
                    return;
                }
                Bundle args = new Bundle();
                args.putLong("vocabularyId", vocabulary.getVocabularyId());
                NavHostFragment.findNavController(VocabularyFragment.this)
                        .navigate(R.id.nav_vocabulary_detail, args);
            }

            @Override
            public void onVocabularyLongClicked(VocabularyEntity vocabulary) {
                toggleSelection(vocabulary);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        attachSwipeToDelete(recyclerView);

        view.findViewById(R.id.btn_add_word).setOnClickListener(v -> showAddWordDialog());
        view.findViewById(R.id.btn_batch_clear).setOnClickListener(v -> clearSelection());
        view.findViewById(R.id.btn_batch_topic).setOnClickListener(v -> showTopicDialog());
        view.findViewById(R.id.btn_batch_quiz).setOnClickListener(v -> startForcedQuiz());
        view.findViewById(R.id.btn_start_review).setOnClickListener(v -> startFlashcardSession(null));
        view.findViewById(R.id.btn_start_flashcard).setOnClickListener(v -> startFlashcardSession(null));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                updateSearch(text);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                updateSearch(text);
                return true;
            }
        });

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            selectedFilterId = checkedIds.isEmpty() ? R.id.chip_all : checkedIds.get(0);
            applyFilters();
        });

        viewModel.getAllVocabularies().observe(getViewLifecycleOwner(), words -> {
            allWords.clear();
            if (words != null) allWords.addAll(words);
            if (query.trim().isEmpty()) {
                visibleBaseWords.clear();
                visibleBaseWords.addAll(allWords);
            }
            pruneSelection();
            applyFilters();
        });

        viewModel.getTopics().observe(getViewLifecycleOwner(), topicEntities -> {
            topics = topicEntities == null ? new ArrayList<>() : topicEntities;
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
            public int getSwipeDirs(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
                return isSelectionMode() ? 0 : super.getSwipeDirs(recyclerView, viewHolder);
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

    private void updateSearch(String text) {
        query = text == null ? "" : text.trim();
        clearSelection();
        if (searchSource != null && searchObserver != null) {
            searchSource.removeObserver(searchObserver);
            searchSource = null;
            searchObserver = null;
        }
        if (query.isEmpty()) {
            visibleBaseWords.clear();
            visibleBaseWords.addAll(allWords);
            applyFilters();
            return;
        }
        searchSource = viewModel.searchVocabularies(query);
        searchObserver = words -> {
            visibleBaseWords.clear();
            if (words != null) visibleBaseWords.addAll(words);
            applyFilters();
        };
        searchSource.observe(getViewLifecycleOwner(), searchObserver);
    }

    private void applyFilters() {
        long now = System.currentTimeMillis();
        List<VocabularyEntity> filtered = new ArrayList<>();
        List<VocabularyEntity> source = query.trim().isEmpty() ? allWords : visibleBaseWords;

        for (VocabularyEntity word : source) {
            if (matchesFilter(word, now)) filtered.add(word);
        }

        adapter.submitList(filtered);
        adapter.setSelectedIds(selectedIds);
        emptyView.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);

        // Tính toán thống kê từ toàn bộ danh sách
        int mastered = 0;
        int due = 0;
        int newWords = 0; // chưa học lần nào: masteryLevel == 0 && nextReviewDate == 0
        int inProgress = 0;
        for (VocabularyEntity word : allWords) {
            if (word.isMastered()) {
                mastered++;
            } else if (isNewWord(word)) {
                newWords++;
            } else {
                // đang học (masteryLevel > 0 hoặc đã review ít nhất 1 lần)
                inProgress++;
                if (word.getNextReviewDate() <= now) due++;
            }
        }
        // Từ mới cũng cần được "học" hôm nay nếu còn nhiều
        int totalDueAndNew = due; // banner chỉ đếm từ đến hạn ôn, không tính từ mới (khác loại)

        totalCount.setText(String.valueOf(allWords.size()));
        newCount.setText(String.valueOf(newWords));
        dueCount.setText(String.valueOf(due));
        masteredCount.setText(String.valueOf(mastered));

        String prefix = query.trim().isEmpty() ? "" : "FTS: \"" + query + "\" • ";
        summary.setText(prefix + filtered.size() + " đang hiển thị • " + due + " cần ôn hôm nay");

        // Cập nhật text chip
        ((TextView) rootView.findViewById(R.id.chip_all)).setText("Tất cả " + allWords.size());
        ((TextView) rootView.findViewById(R.id.chip_new)).setText("Từ mới " + newWords);
        ((TextView) rootView.findViewById(R.id.chip_due)).setText("Cần ôn " + due);
        ((TextView) rootView.findViewById(R.id.chip_unmastered)).setText("Đang học " + inProgress);
        ((TextView) rootView.findViewById(R.id.chip_mastered)).setText("Đã thuộc " + mastered);

        // Hiện/ẩn banner ôn tập
        updateReviewBanner(due, newWords);
        updateBatchActions();
    }

    /**
     * Từ mới = chưa học lần nào: masteryLevel == 0 và nextReviewDate == 0
     */
    private boolean isNewWord(VocabularyEntity word) {
        return !word.isMastered() && word.getMasteryLevel() == 0 && word.getNextReviewDate() == 0L;
    }

    private void updateReviewBanner(int due, int newWords) {
        // Chỉ hiện banner khi có từ đến hạn ôn tập (không tính từ mới — khác luồng)
        if (due > 0 && !isSelectionMode()) {
            reviewBanner.setVisibility(View.VISIBLE);
            bannerSubtitle.setText("Bạn có " + due + " từ cần ôn lại hôm nay. Đừng để quên mất!");
        } else {
            reviewBanner.setVisibility(View.GONE);
        }
    }

    private boolean matchesFilter(VocabularyEntity word, long now) {
        if (selectedFilterId == R.id.chip_new) {
            return isNewWord(word);
        }
        if (selectedFilterId == R.id.chip_mastered) return word.isMastered();
        if (selectedFilterId == R.id.chip_unmastered) {
            // "Đang học": đã học ít nhất 1 lần nhưng chưa mastered
            return !word.isMastered() && !isNewWord(word);
        }
        if (selectedFilterId == R.id.chip_due) {
            return !word.isMastered() && word.getNextReviewDate() > 0 && word.getNextReviewDate() <= now;
        }
        return true; // chip_all
    }

    private void toggleSelection(VocabularyEntity vocabulary) {
        long id = vocabulary.getVocabularyId();
        if (selectedIds.contains(id)) {
            selectedIds.remove(id);
        } else {
            selectedIds.add(id);
        }
        adapter.setSelectionMode(isSelectionMode());
        adapter.setSelectedIds(selectedIds);
        updateBatchActions();
    }

    private void clearSelection() {
        selectedIds.clear();
        adapter.setSelectionMode(false);
        updateBatchActions();
    }

    private boolean isSelectionMode() {
        return !selectedIds.isEmpty();
    }

    private void updateBatchActions() {
        boolean selecting = isSelectionMode();
        batchActions.setVisibility(selecting ? View.VISIBLE : View.GONE);
        // Ẩn banner khi đang chọn để tránh rối giao diện
        if (selecting) reviewBanner.setVisibility(View.GONE);
        adapter.setSelectionMode(selecting);
        adapter.setSelectedIds(selectedIds);
        if (selecting) {
            selectedCount.setText(selectedIds.size() + " từ đã chọn");
        }
    }

    private void pruneSelection() {
        Set<Long> existingIds = new HashSet<>();
        for (VocabularyEntity word : allWords) existingIds.add(word.getVocabularyId());
        selectedIds.retainAll(existingIds);
    }

    /** Bắt đầu phiên ôn tập flashcard.
     * @param ids null = ôn daily review, non-null = ôn từ danh sách cụ thể
     */
    private void startFlashcardSession(long[] ids) {
        Bundle args = new Bundle();
        if (ids != null && ids.length > 0) {
            args.putLongArray("vocabularyIds", ids);
        } else {
            args.putString("mode", "DAILY");
        }
        NavHostFragment.findNavController(this).navigate(R.id.nav_flashcard_session, args);
    }

    /** Bắt đầu phiên ôn tập hàng ngày: lấy tất cả từ đến hạn ôn (legacy, dùng cho quiz) */
    private void startDailyReview() {
        startFlashcardSession(null);
    }

    private void showTopicDialog() {
        if (!isSelectionMode()) return;
        List<String> labels = new ArrayList<>();
        labels.add("Tạo chủ đề mới");
        labels.add("Bỏ chủ đề");
        for (TopicEntity topic : topics) labels.add(topic.getName());

        new AlertDialog.Builder(requireContext())
                .setTitle("Gắn chủ đề")
                .setItems(labels.toArray(new CharSequence[0]), (dialog, which) -> {
                    List<Long> ids = new ArrayList<>(selectedIds);
                    if (which == 0) {
                        showCreateTopicDialog(ids);
                    } else if (which == 1) {
                        viewModel.moveToTopic(ids, null);
                        clearSelection();
                        Toast.makeText(requireContext(), "Đã bỏ chủ đề", Toast.LENGTH_SHORT).show();
                    } else {
                        TopicEntity topic = topics.get(which - 2);
                        viewModel.moveToTopic(ids, topic.getTopicId());
                        clearSelection();
                        Toast.makeText(requireContext(), "Đã chuyển vào " + topic.getName(), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showCreateTopicDialog(List<Long> ids) {
        EditText input = createInput("Tên chủ đề", "Từ vựng ôn thi TOEIC");
        new AlertDialog.Builder(requireContext())
                .setTitle("Tạo chủ đề mới")
                .setView(input)
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    viewModel.createTopicAndMove(ids, input.getText().toString());
                    clearSelection();
                    Toast.makeText(requireContext(), "Đã tạo và gắn chủ đề", Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void startForcedQuiz() {
        if (!isSelectionMode()) return;
        long[] ids = new long[selectedIds.size()];
        int index = 0;
        for (Long id : selectedIds) ids[index++] = id;
        Bundle args = new Bundle();
        args.putLongArray("vocabularyIds", ids);
        args.putString("quizMode", "FORCED_VOCABULARY");
        clearSelection();
        NavHostFragment.findNavController(this).navigate(R.id.nav_quiz_session, args);
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
            if (item.getWord().equalsIgnoreCase(word.trim())) return item;
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
