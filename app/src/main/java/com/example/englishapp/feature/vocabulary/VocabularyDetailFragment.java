package com.example.englishapp.feature.vocabulary;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.englishapp.R;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VocabularyDetailFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tvMessage = view.findViewById(R.id.tv_message);
        tvMessage.setText("Bạn đang đứng tại: Vocabulary Detail\nHạ tầng Navigation và Theme hoạt động 100%!");
    }
}
