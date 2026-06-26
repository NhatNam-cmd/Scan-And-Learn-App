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
import androidx.appcompat.app.AlertDialog;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.R;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.example.englishapp.core.model.VocabularyLookup;
import com.example.englishapp.core.service.AudioCacheManager;
import com.google.android.material.button.MaterialButton;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WordPreviewDialog extends DialogFragment {

    private static final String ARG_WORD = "word";
    private static final String ARG_MEANING = "meaning";
    private static final String ARG_PHONETIC = "phonetic";
    private boolean forceUpdate = false;
    @Inject
    AudioCacheManager audioCacheManager;

    private ScanViewModel viewModel;

    private EditText etWord;
    private EditText etMeaning;
    private TextView tvPhonetic;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;

    private ImageButton btnRefresh;
    private ImageButton btnPlay;

    public static WordPreviewDialog newInstance(
            VocabularyLookup lookup
    ) {

        Bundle args = new Bundle();

        args.putString(
                ARG_WORD,
                lookup.getWord()
        );

        args.putString(
                ARG_MEANING,
                lookup.getMeaning()
        );

        args.putString(
                ARG_PHONETIC,
                lookup.getPhonetic()
        );

        WordPreviewDialog dialog =
                new WordPreviewDialog();

        dialog.setArguments(args);

        return dialog;

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        if (getDialog() != null &&
                getDialog().getWindow() != null) {

            getDialog()
                    .getWindow()
                    .requestFeature(Window.FEATURE_NO_TITLE);

            getDialog()
                    .getWindow()
                    .setBackgroundDrawable(
                            new ColorDrawable(Color.TRANSPARENT)
                    );

        }

        return inflater.inflate(
                R.layout.dialog_word_preview,
                container,
                false
        );

    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {

        super.onViewCreated(view, savedInstanceState);

        viewModel =
                new ViewModelProvider(
                        requireParentFragment()
                ).get(ScanViewModel.class);

        initViews(view);
        observeSaveResult();
        bindData();

        setupListeners();

    }

    private void initViews(View view){

        etWord=view.findViewById(R.id.et_word);

        etMeaning=view.findViewById(R.id.et_meaning);

        tvPhonetic=view.findViewById(R.id.tv_phonetic);

        btnSave=view.findViewById(R.id.btn_save_vocab);

        btnCancel=view.findViewById(R.id.btn_cancel_vocab);

        btnRefresh=view.findViewById(R.id.btn_refresh_word);

        btnPlay=view.findViewById(R.id.btn_play_audio);

    }

    private void bindData(){

        Bundle args=getArguments();

        if(args==null)return;

        etWord.setText(
                args.getString(ARG_WORD,"")
        );

        etMeaning.setText(
                args.getString(ARG_MEANING,"")
        );

        tvPhonetic.setText(
                args.getString(ARG_PHONETIC,"")
        );

    }

    private void setupListeners(){

        btnCancel.setOnClickListener(v->dismiss());

        btnRefresh.setOnClickListener(v->refreshWord());

        btnPlay.setOnClickListener(v->playAudio());

        btnSave.setOnClickListener(v->saveVocabulary());

    }

    private void refreshWord(){

        String word=
                etWord.getText()
                        .toString()
                        .trim();

        if(word.isEmpty()){

            Toast.makeText(
                    requireContext(),
                    "Nhập từ cần tra",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }

        dismiss();

        viewModel.lookupScannedWord(word);

    }

    private void playAudio(){

        String word=
                etWord.getText()
                        .toString()
                        .trim();

        if(word.isEmpty()){

            return;

        }

        String url=
                "https://api.dictionaryapi.dev/media/pronunciations/en/"
                        +word.toLowerCase()
                        +"-us.mp3";

        audioCacheManager
                .getAudioFile(word,url)
                .thenAccept(file->{

                    if(file!=null){

                        audioCacheManager.playAudio(file);

                    }

                });

    }

    private void saveVocabulary(){

        String word=
                etWord.getText()
                        .toString()
                        .trim();

        String meaning=
                etMeaning.getText()
                        .toString()
                        .trim();

        if(word.isEmpty()||meaning.isEmpty()){

            Toast.makeText(
                    requireContext(),
                    "Không được để trống",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }

        long now=
                System.currentTimeMillis();

        VocabularyEntity entity=
                new VocabularyEntity(

                        0,

                        null,

                        word,

                        meaning,

                        tvPhonetic.getText().toString(),

                        "",

                        "",

                        "",

                        null,

                        "SCAN",

                        0,

                        false,

                        now,

                        now,

                        now

                );

        viewModel.saveVocabulary(entity,forceUpdate);

    }
    private void observeSaveResult() {

        viewModel.getSaveResult().observe(

                getViewLifecycleOwner(),

                result -> {

                    if (result == null) {

                        return;

                    }

                    if (result instanceof ApiResult.Success) {

                        Toast.makeText(

                                requireContext(),

                                ((ApiResult.Success<String>) result)
                                        .getData(),

                                Toast.LENGTH_SHORT

                        ).show();

                        dismiss();

                    }

                    else if (result instanceof ApiResult.Error) {

                        Toast.makeText(

                                requireContext(),

                                ((ApiResult.Error<?>) result)
                                        .getMessage(),

                                Toast.LENGTH_SHORT

                        ).show();

                    }

                    else if (result instanceof ApiResult.Fallback) {

                        showUpdateDialog();

                    }

                    viewModel.clearSaveResult();

                });

    }
    private void showUpdateDialog() {

        new AlertDialog.Builder(requireContext())

                .setTitle("Duplicate vocabulary")

                .setMessage(
                        "Từ này đã tồn tại.\nBạn có muốn cập nhật nghĩa mới không?"
                )

                .setPositiveButton(

                        "Update",

                        (dialog, which) -> {

                            forceUpdate = true;

                            saveVocabulary();

                        })

                .setNegativeButton(

                        "Cancel",

                        null

                )

                .show();

    }
}