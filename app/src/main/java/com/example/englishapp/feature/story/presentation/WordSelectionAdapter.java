package com.example.englishapp.feature.story.presentation;

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

    // Payload constant — dùng để chỉ update selection state, không trigger animation lại
    private static final String PAYLOAD_SELECTION_CHANGED = "SELECTION_CHANGED";

    public interface OnWordClickListener {
        void onWordClicked(VocabularyEntity word);
    }

    private final OnWordClickListener listener;
    private final List<VocabularyEntity> words = new ArrayList<>();
    private final Set<Long> selectedIds = new HashSet<>();
    // Track IDs đã chạy entrance animation để không chạy lại khi rebind
    private final Set<Long> animatedIds = new HashSet<>();

    public WordSelectionAdapter(OnWordClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return words.get(position).getVocabularyId();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_word_selection, parent, false);
        return new WordViewHolder(view);
    }

    /** Full bind — chỉ chạy khi item lần đầu xuất hiện, hoặc list thay đổi */
    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        VocabularyEntity word = words.get(position);
        boolean selected = selectedIds.contains(word.getVocabularyId());

        holder.tvWord.setText(word.getWord());
        holder.tvMeaning.setText(word.getMeaning());

        String initial = word.getWord() != null && !word.getWord().isEmpty()
                ? String.valueOf(word.getWord().charAt(0)).toUpperCase()
                : "?";
        holder.tvInitial.setText(initial);

        applySelectionVisual(holder, selected);

        holder.card.setOnClickListener(v -> {
            animateCardPress(holder.card);
            listener.onWordClicked(word);
        });
        holder.checkBox.setOnClickListener(v -> listener.onWordClicked(word));

        // Entrance animation chỉ chạy 1 lần cho mỗi item
        long id = word.getVocabularyId();
        if (!animatedIds.contains(id)) {
            animatedIds.add(id);
            holder.itemView.setAlpha(0f);
            holder.itemView.setTranslationY(24f);
            holder.itemView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(Math.min(position * 30L, 250L))
                    .setDuration(200L)
                    .start();
        } else {
            // Đảm bảo view ở trạng thái bình thường nếu đã animate rồi
            holder.itemView.setAlpha(1f);
            holder.itemView.setTranslationY(0f);
        }
    }

    /** Partial bind — chỉ cập nhật visual selection, KHÔNG reset animation */
    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position,
                                 @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && PAYLOAD_SELECTION_CHANGED.equals(payloads.get(0))) {
            VocabularyEntity word = words.get(position);
            boolean selected = selectedIds.contains(word.getVocabularyId());
            applySelectionVisual(holder, selected);
            return;
        }
        // Fallback: full bind
        onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return words.size();
    }

    /** Cập nhật danh sách từ — reset animation tracker vì list hoàn toàn mới */
    public void submitWords(List<VocabularyEntity> newWords) {
        words.clear();
        animatedIds.clear();
        if (newWords != null) {
            words.addAll(newWords);
        }
        notifyDataSetChanged();
    }

    /**
     * Cập nhật trạng thái chọn — chỉ gửi payload tới items thay đổi,
     * KHÔNG gọi notifyDataSetChanged() để tránh reset animation toàn list.
     */
    public void submitSelectedIds(List<Long> ids) {
        Set<Long> newSelected = new HashSet<>();
        if (ids != null) newSelected.addAll(ids);

        for (int i = 0; i < words.size(); i++) {
            long wordId = words.get(i).getVocabularyId();
            boolean wasSelected = selectedIds.contains(wordId);
            boolean isSelected = newSelected.contains(wordId);
            if (wasSelected != isSelected) {
                // Chỉ notify item cụ thể này với payload — không trigger full rebind
                notifyItemChanged(i, PAYLOAD_SELECTION_CHANGED);
            }
        }

        selectedIds.clear();
        selectedIds.addAll(newSelected);
    }

    /** Cập nhật visual card theo trạng thái chọn với animation mượt */
    private void applySelectionVisual(WordViewHolder holder, boolean selected) {
        holder.checkBox.setChecked(selected);

        int targetBg = selected
                ? holder.itemView.getContext().getResources().getColor(R.color.light_primary_container, null)
                : holder.itemView.getContext().getResources().getColor(R.color.light_surface, null);
        float targetElevationDp = selected ? 6f : 2f;
        float density = holder.itemView.getContext().getResources().getDisplayMetrics().density;

        holder.card.setCardBackgroundColor(targetBg);
        holder.card.setCardElevation(targetElevationDp * density);

        // Micro-animation nhẹ khi toggle
        holder.card.animate()
                .scaleX(selected ? 1.01f : 1f)
                .scaleY(selected ? 1.01f : 1f)
                .setDuration(120L)
                .withEndAction(() -> holder.card.animate()
                        .scaleX(1f).scaleY(1f).setDuration(80L).start())
                .start();
    }

    private void animateCardPress(View view) {
        view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L)
                .withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(100L).start())
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
