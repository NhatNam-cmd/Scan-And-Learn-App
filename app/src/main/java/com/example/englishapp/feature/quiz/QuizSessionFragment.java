package com.example.englishapp.feature.quiz;

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
public class QuizSessionFragment extends Fragment {
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
        long[] vocabularyIds = getArguments() == null ? null : getArguments().getLongArray("vocabularyIds");
        String quizMode = getArguments() == null ? null : getArguments().getString("quizMode");
        if ("FORCED_VOCABULARY".equals(quizMode) && vocabularyIds != null) {
            tvMessage.setText("Custom Quiz Session\nĐã nhận " + vocabularyIds.length
                    + " từ vựng từ Vocabulary.\nSẵn sàng tạo bài kiểm tra cục bộ từ bộ từ đã chọn.");
        } else {
            tvMessage.setText("Bạn đang đứng tại: Quiz Session\nHạ tầng Navigation và Theme hoạt động 100%!");
        }
    }
}
