package com.example.englishapp.feature.story.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordSelectionAdapter extends RecyclerView.Adapter<WordSelectionAdapter.WordViewHolder> {
    public interface OnWordClickListener {
        void onWordClicked(VocabularyEntity word);
    }

    private final OnWordClickListener listener;
    private final List<VocabularyEntity> words = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();

    public WordSelectionAdapter(OnWordClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_selection, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        VocabularyEntity word = words.get(position);
        holder.tvWord.setText(word.getWord());
        holder.tvMeaning.setText(word.getMeaning());
        boolean selected = selectedIds.contains(word.getVocabularyId());
        holder.itemView.setSelected(selected);
        holder.checkBox.setChecked(selected);
        holder.itemView.setOnClickListener(v -> listener.onWordClicked(word));
        holder.checkBox.setOnClickListener(v -> listener.onWordClicked(word));
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(16f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(Math.min(position * 25L, 220L))
                .setDuration(180L)
                .start();
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    public void submitWords(List<VocabularyEntity> newWords) {
        words.clear();
        if (newWords != null) {
            words.addAll(newWords);
        }
        notifyDataSetChanged();
    }

    public void submitSelectedIds(List<Long> ids) {
        selectedIds.clear();
        if (ids != null) {
            selectedIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvWord;
        private final TextView tvMeaning;
        private final CheckBox checkBox;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            checkBox = itemView.findViewById(R.id.cb_selected);
        }
    }
}
