package com.example.englishapp.feature.scan.presentation;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishapp.R;
import com.example.englishapp.core.service.AudioCacheManager;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.model.VocabularyLookup;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import javax.inject.Inject;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordPreviewDialog extends DialogFragment {

    private static final String ARG_WORD = "arg_word";
    private static final String ARG_MEANING = "arg_meaning";
    private static final String ARG_PHONETIC = "arg_phonetic";

    private EditText etWord;
    private TextView tvPhonetic;
    private EditText etMeaning;
    private ImageButton btnPlayAudio;
    private ImageButton btnSave, btnCancel, btnRefreshWord;

    private String currentWord;
    private String currentMeaning;
    private String currentPhonetic;

    @Inject
    public AudioCacheManager audioCacheManager;

    private ScanViewModel scanViewModel;

    public static WordPreviewDialog newInstance(VocabularyLookup lookup) {
        WordPreviewDialog fragment = new WordPreviewDialog();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, lookup.getWord());
        args.putString(ARG_MEANING, lookup.getMeaning());
        args.putString(ARG_PHONETIC, lookup.getPhonetic());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentWord = getArguments().getString(ARG_WORD, "");
            currentMeaning = getArguments().getString(ARG_MEANING, "");
            currentPhonetic = getArguments().getString(ARG_PHONETIC, "");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        return inflater.inflate(R.layout.dialog_word_preview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getParentFragment() != null) {
            scanViewModel = new ViewModelProvider(getParentFragment()).get(ScanViewModel.class);
        }

        etWord = view.findViewById(R.id.et_word);
        tvPhonetic = view.findViewById(R.id.tv_phonetic);
        etMeaning = view.findViewById(R.id.et_meaning);
        btnPlayAudio = view.findViewById(R.id.btn_play_audio);
        btnSave = view.findViewById(R.id.btn_save_vocab);
        btnCancel = view.findViewById(R.id.btn_cancel_vocab);
        btnRefreshWord = view.findViewById(R.id.btn_refresh_word);

        etWord.setText(currentWord);
        tvPhonetic.setText(currentPhonetic == null || currentPhonetic.isEmpty() ? "/.../" : currentPhonetic);
        etMeaning.setText(currentMeaning);

        btnRefreshWord.setOnClickListener(v -> {
            String correctedWord = etWord.getText().toString().trim();
            if (!correctedWord.isEmpty() && scanViewModel != null) {
                scanViewModel.lookupScannedWord(correctedWord);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập từ cần kiểm tra", Toast.LENGTH_SHORT).show();
            }
        });

        btnPlayAudio.setOnClickListener(v -> {
            String queryWord = etWord.getText().toString().trim();
            if (!queryWord.isEmpty()) {
                String audioUrl = "https://api.dictionaryapi.dev/media/pronunciations/en/" + queryWord.toLowerCase() + "-us.mp3";

                audioCacheManager.getAudioFile(queryWord, audioUrl).thenAccept(file -> {
                    if (file != null) {
                        audioCacheManager.playAudio(file);
                    }
                }).exceptionally(throwable -> {
                    getActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Không có file âm thanh cho từ này", Toast.LENGTH_SHORT).show()
                    );
                    return null;
                });
            }
        });

        btnSave.setOnClickListener(v -> {
            String finalWord = etWord.getText().toString().trim();
            String finalMeaning = etMeaning.getText().toString().trim();

            if (finalWord.isEmpty() || finalMeaning.isEmpty()) {
                Toast.makeText(getContext(), "Từ vựng và ý nghĩa không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            long currentTime = System.currentTimeMillis();
            VocabularyEntity newEntity = new VocabularyEntity(
                    0L,
                    null,
                    finalWord,
                    finalMeaning,
                    currentPhonetic,
                    "",
                    "",
                    "",
                    null,
                    "SCAN",
                    0,
                    false,
                    currentTime,
                    currentTime,
                    currentTime
            );

            if (scanViewModel != null) {
                scanViewModel.saveScannedVocabulary(newEntity, false);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }
}