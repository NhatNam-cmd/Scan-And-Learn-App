package com.example.englishapp.feature.vocabulary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    public interface OnVocabularyClickListener {
        void onVocabularyClicked(VocabularyEntity vocabulary);
    }

    private final OnVocabularyClickListener listener;
    private final List<VocabularyEntity> words = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public VocabularyAdapter(OnVocabularyClickListener listener) {
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
                ? "100%"
                : Math.min(100, item.getMasteryLevel() * 20) + "%");
        holder.progressMastery.setProgress(item.isMastered()
                ? 100
                : Math.min(100, item.getMasteryLevel() * 20));
        holder.tvReview.setText(buildReviewText(item));
        holder.itemView.setOnClickListener(v -> listener.onVocabularyClicked(item));
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

    private String buildReviewText(VocabularyEntity item) {
        if (item.isMastered()) return "Không cần ôn";
        long nextReviewDate = item.getNextReviewDate();
        if (nextReviewDate <= 0L || nextReviewDate <= System.currentTimeMillis()) {
            return "Cần ôn hôm nay";
        }
        return "Ôn tiếp: " + dateFormat.format(new Date(nextReviewDate));
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
        private final ProgressBar progressMastery;

        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInitial = itemView.findViewById(R.id.tv_word_initial);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvPhonetic = itemView.findViewById(R.id.tv_phonetic);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            tvSource = itemView.findViewById(R.id.tv_source);
            tvMastery = itemView.findViewById(R.id.tv_mastery);
            tvReview = itemView.findViewById(R.id.tv_review);
            progressMastery = itemView.findViewById(R.id.progress_mastery);
        }
    }
}
