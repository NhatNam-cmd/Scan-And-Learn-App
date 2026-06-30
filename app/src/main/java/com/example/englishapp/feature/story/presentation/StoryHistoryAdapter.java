package com.example.englishapp.feature.story.presentation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.StoryEntity;
import com.example.englishapp.feature.story.domain.StoryGameData;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StoryHistoryAdapter extends RecyclerView.Adapter<StoryHistoryAdapter.StoryViewHolder> {
    public interface OnStoryClickedListener {
        void onStoryClicked(StoryEntity story);
    }

    private final List<StoryEntity> stories = new ArrayList<>();
    private final OnStoryClickedListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private final Gson gson = new Gson();

    public StoryHistoryAdapter(OnStoryClickedListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return stories.get(position).getStoryId();
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
        StoryEntity story = stories.get(position);
        StoryGameData data = parseStory(story);
        holder.tvTitle.setText(story.getTitle());
        holder.tvMeta.setText(formatDifficulty(story.getDifficulty()) + " • " + dateFormat.format(new Date(story.getCreatedAt())));
        holder.tvPreview.setText(buildPreview(data, story));
        holder.itemView.setOnClickListener(v -> listener.onStoryClicked(story));
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public void submitList(List<StoryEntity> newStories) {
        stories.clear();
        if (newStories != null) {
            stories.addAll(newStories);
        }
        notifyDataSetChanged();
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
            return "AI Story";
        }
        if ("OFFLINE".equalsIgnoreCase(difficulty)) {
            return "Offline Story";
        }
        return difficulty == null || difficulty.trim().isEmpty() ? "Story" : difficulty;
    }

    static class StoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final TextView tvMeta;
        private final TextView tvPreview;

        StoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_story_history_title);
            tvMeta = itemView.findViewById(R.id.tv_story_history_meta);
            tvPreview = itemView.findViewById(R.id.tv_story_history_preview);
        }
    }
}
