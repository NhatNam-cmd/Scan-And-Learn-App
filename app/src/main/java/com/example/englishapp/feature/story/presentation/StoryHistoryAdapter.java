package com.example.englishapp.feature.story.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StoryHistoryAdapter extends ListAdapter<StoryEntity, StoryHistoryAdapter.StoryViewHolder> {

    public interface OnStoryClickedListener {
        void onStoryClicked(StoryEntity story);
    }

    private static final DiffUtil.ItemCallback<StoryEntity> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<StoryEntity>() {
                @Override
                public boolean areItemsTheSame(@NonNull StoryEntity oldItem, @NonNull StoryEntity newItem) {
                    return oldItem.getStoryId() == newItem.getStoryId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull StoryEntity oldItem, @NonNull StoryEntity newItem) {
                    return oldItem.getTitle().equals(newItem.getTitle())
                            && oldItem.getCreatedAt() == newItem.getCreatedAt()
                            && oldItem.getDifficulty() != null
                            && oldItem.getDifficulty().equals(newItem.getDifficulty());
                }
            };

    private final OnStoryClickedListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final Gson gson = new Gson();

    public StoryHistoryAdapter(OnStoryClickedListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_story_history, parent, false);
        return new StoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        StoryEntity story = getItem(position);
        StoryGameData data = parseStory(story);

        holder.tvTitle.setText(story.getTitle());
        holder.tvMeta.setText(dateFormat.format(new Date(story.getCreatedAt())));
        holder.tvPreview.setText(buildPreview(data, story));

        // Type badge
        holder.tvTypeBadge.setText(formatDifficulty(story.getDifficulty()));

        holder.itemView.setOnClickListener(v -> listener.onStoryClicked(story));
    }

    private StoryGameData parseStory(StoryEntity story) {
        try {
            return gson.fromJson(story.getContent(), StoryGameData.class);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private String buildPreview(StoryGameData data, StoryEntity story) {
        String text = data != null ? data.getStory() : story.getContent();
        if (text == null || text.trim().isEmpty()) {
            return "Không có nội dung story.";
        }
        String singleLine = text.replace('\n', ' ').trim();
        return singleLine.length() > 120 ? singleLine.substring(0, 120) + "..." : singleLine;
    }

    private String formatDifficulty(String difficulty) {
        if ("AI".equalsIgnoreCase(difficulty)) {
            return "✨ AI Story";
        }
        if ("OFFLINE".equalsIgnoreCase(difficulty)) {
            return "📖 Offline";
        }
        return difficulty == null || difficulty.trim().isEmpty() ? "Story" : difficulty;
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvTypeBadge;
        private final TextView tvMeta;
        private final TextView tvPreview;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_story_history_title);
            tvTypeBadge = itemView.findViewById(R.id.tv_story_type_badge);
            tvMeta = itemView.findViewById(R.id.tv_story_history_meta);
            tvPreview = itemView.findViewById(R.id.tv_story_history_preview);
        }
    }
}
