package com.example.englishapp.feature.story.presentation;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
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
        boolean selected = selectedIds.contains(word.getVocabularyId());

        holder.tvWord.setText(word.getWord());
        holder.tvMeaning.setText(word.getMeaning());

        // Show first letter as avatar
        String initial = word.getWord() != null && !word.getWord().isEmpty()
                ? String.valueOf(word.getWord().charAt(0)).toUpperCase()
                : "?";
        holder.tvInitial.setText(initial);

        // Update card visual state
        holder.checkBox.setChecked(selected);
        if (selected) {
            holder.card.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.light_primary_container, null));
            holder.card.setCardElevation(6f * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        } else {
            holder.card.setCardBackgroundColor(
                    holder.itemView.getContext().getResources().getColor(R.color.light_surface, null));
            holder.card.setCardElevation(2f * holder.itemView.getContext().getResources().getDisplayMetrics().density);
        }

        holder.card.setOnClickListener(v -> {
            animateCardPress(holder.card);
            listener.onWordClicked(word);
        });
        holder.checkBox.setOnClickListener(v -> listener.onWordClicked(word));

        // Staggered entrance animation
        holder.itemView.setAlpha(0f);
        holder.itemView.setTranslationY(20f);
        holder.itemView.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(Math.min(position * 30L, 240L))
                .setDuration(200L)
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

    private void animateCardPress(View view) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80L)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(120L).start())
                .start();
    }

    static class WordViewHolder extends RecyclerView.ViewHolder {
        private final CardView card;
        private final TextView tvWord;
        private final TextView tvMeaning;
        private final TextView tvInitial;
        private final CheckBox checkBox;

        WordViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_word_item);
            tvWord = itemView.findViewById(R.id.tv_word);
            tvMeaning = itemView.findViewById(R.id.tv_meaning);
            tvInitial = itemView.findViewById(R.id.tv_word_initial);
            checkBox = itemView.findViewById(R.id.cb_selected);
        }
    }
}
