package com.example.englishapp.feature.vocabulary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    public interface OnVocabularyInteractionListener {
        void onVocabularyClicked(VocabularyEntity vocabulary);
        void onVocabularyLongClicked(VocabularyEntity vocabulary);
    }

    private final OnVocabularyInteractionListener listener;
    private final List<VocabularyEntity> words = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private boolean selectionMode;

    public VocabularyAdapter(OnVocabularyInteractionListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return words.get(position).getVocabularyId();
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        VocabularyEntity item = words.get(position);
        String word = item.getWord();
        String initial = word != null && !word.isEmpty()
                ? String.valueOf(word.charAt(0)).toUpperCase()
                : "?";
        holder.tvInitial.setText(initial);
        holder.tvWord.setText(item.getWord());
        holder.tvMeaning.setText(item.getMeaning());
        holder.tvPhonetic.setText(isBlank(item.getPhonetic()) ? "/.../" : item.getPhonetic());
        holder.tvSource.setText(formatSource(item.getSourceType()));
        holder.tvMastery.setText(item.isMastered()
                ? "Level 5"
                : "Level " + Math.min(5, item.getMasteryLevel()));
        bindMasterySegments(holder.layoutMasterySegments, item);
        holder.tvReview.setText(buildReviewText(item));
        boolean selected = selectedIds.contains(item.getVocabularyId());
        holder.tvSelectedCheck.setVisibility(selected ? View.VISIBLE : View.GONE);
        holder.itemView.setAlpha(selectionMode && !selected ? 0.78f : 1f);
        holder.itemView.setOnClickListener(v -> listener.onVocabularyClicked(item));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onVocabularyLongClicked(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void submitList(List<VocabularyEntity> newWords) {
        words.clear();
        if (newWords != null) {
            words.addAll(newWords);
        }
        notifyDataSetChanged();
    }

    public VocabularyEntity getItem(int position) {
        if (position < 0 || position >= words.size()) return null;
        return words.get(position);
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyDataSetChanged();
    }

    public void setSelectedIds(Set<Long> ids) {
        selectedIds.clear();
        if (ids != null) selectedIds.addAll(ids);
        notifyDataSetChanged();
    }

    public List<Long> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    private String buildReviewText(VocabularyEntity item) {
        if (item.isMastered()) return "Không cần ôn";
        long nextReviewDate = item.getNextReviewDate();
        if (nextReviewDate <= 0L || nextReviewDate <= System.currentTimeMillis()) {
            return "Cần ôn hôm nay";
        }
        return "Ôn tiếp: " + dateFormat.format(new Date(nextReviewDate));
    }

    private void bindMasterySegments(LinearLayout container, VocabularyEntity item) {
        container.removeAllViews();
        int level = item.isMastered() ? 5 : Math.max(0, Math.min(5, item.getMasteryLevel()));
        int activeColor = getMasteryColor(container, level);
        int inactiveColor = container.getContext().getResources().getColor(android.R.color.darker_gray, null);
        float density = container.getContext().getResources().getDisplayMetrics().density;
        for (int i = 1; i <= 5; i++) {
            View segment = new View(container.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) (8 * density), 1f);
            if (i < 5) params.setMarginEnd((int) (4 * density));
            segment.setLayoutParams(params);
            segment.setBackgroundColor(i <= level ? activeColor : inactiveColor);
            segment.setAlpha(i <= level ? 1f : 0.22f);
            container.addView(segment);
        }
    }

    private int getMasteryColor(LinearLayout container, int level) {
        int colorRes;
        if (level <= 1) {
            colorRes = R.color.light_error;
        } else if (level <= 3) {
            colorRes = R.color.light_tertiary;
        } else {
            colorRes = R.color.result_correct;
        }
        return container.getContext().getResources().getColor(colorRes, null);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String formatSource(String sourceType) {
        if ("SCAN".equalsIgnoreCase(sourceType)) return "Quét";
        if ("MANUAL".equalsIgnoreCase(sourceType)) return "Thủ công";
        if ("TOPIC".equalsIgnoreCase(sourceType)) return "Chủ đề";
        return sourceType == null ? "Khác" : sourceType;
    }

    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvInitial;
        private final TextView tvWord;
        private final TextView tvPhonetic;
        private final TextView tvMeaning;
        private final TextView tvSource;
        private final TextView tvMastery;
        private final TextView tvReview;
        private final TextView tvSelectedCheck;
        private final LinearLayout layoutMasterySegments;

        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_word_initial);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvPhonetic = itemView.findViewById(R.id.tv_phonetic);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvMastery = itemView.findViewById(R.id.tv_mastery);
            tvReview = itemView.findViewById(R.id.tv_review);
            tvSelectedCheck = itemView.findViewById(R.id.tv_selected_check);
            layoutMasterySegments = itemView.findViewById(R.id.layout_mastery_segments);
        }
    }
}
