package com.example.englishapp.feature.scan.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.feature.scan.model.WordCandidate;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ScanWordAdapter extends RecyclerView.Adapter<ScanWordAdapter.WordViewHolder> {

    public interface OnWordClickListener {
        void onClick(WordCandidate candidate);
    }

    private final List<WordCandidate> data = new ArrayList<>();

    private final OnWordClickListener listener;

    public ScanWordAdapter(OnWordClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<WordCandidate> list) {

        data.clear();

        if (list != null) {
            data.addAll(list);
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_scan_word,
                        parent,
                        false
                );

        return new WordViewHolder(view);

    }

    @Override
    public void onBindViewHolder(
            @NonNull WordViewHolder holder,
            int position
    ) {

        WordCandidate candidate = data.get(position);

        holder.bind(candidate);

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class WordViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvWord;
        private final TextView tvConfidence;

        WordViewHolder(@NonNull View itemView) {

            super(itemView);

            tvWord = itemView.findViewById(R.id.tvWord);

            tvConfidence = itemView.findViewById(R.id.tvConfidence);

        }

        void bind(WordCandidate candidate) {

            tvWord.setText(candidate.getWord());

            tvConfidence.setText(
                    String.format(
                            Locale.getDefault(),
                            "%.0f%%",
                            candidate.getConfidence() * 100f
                    )
            );

            itemView.setOnClickListener(v -> {

                if (listener != null) {

                    listener.onClick(candidate);

                }

            });

        }

    }

}