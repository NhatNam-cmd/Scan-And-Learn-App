package com.example.englishapp.feature.scan.presentation;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishapp.R;
import com.example.englishapp.feature.scan.presentation.state.ScanUiEvent;
import com.example.englishapp.feature.scan.presentation.state.ScanUiState;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanFragment extends Fragment {

    private ScanViewModel viewModel;

    // Views
    private PreviewView previewView;
    private ImageView capturedImageView;
    private TextView resultTextView;
    private TextView wordDisplayTextView;
    private TextView meaningTextView;
    private TextView phoneticTextView;
    private ProgressBar progressBar;
    private Button captureButton;
    private Button lookupButton;
    private Button saveButton;
    private Button clearButton;

    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Bitmap capturedBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Init ViewModel
        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        // Init Views
        initViews(view);

        // Setup Camera
        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();

        // Setup Listeners
        setupListeners();

        // Observe State
        observeState();
    }

    private void initViews(View view) {
        // ⚠️ Đổi từ previewView -> preview_view (khớp với ID trong XML)
        previewView = view.findViewById(R.id.preview_view);
        capturedImageView = view.findViewById(R.id.capturedImageView);
        resultTextView = view.findViewById(R.id.resultTextView);
        wordDisplayTextView = view.findViewById(R.id.wordDisplayTextView);
        meaningTextView = view.findViewById(R.id.meaningTextView);
        phoneticTextView = view.findViewById(R.id.phoneticTextView);
        progressBar = view.findViewById(R.id.progressBar);
        captureButton = view.findViewById(R.id.captureButton);
        lookupButton = view.findViewById(R.id.lookupButton);
        saveButton = view.findViewById(R.id.saveButton);
        clearButton = view.findViewById(R.id.clearButton);

        // Mặc định ẩn các button
        lookupButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
        capturedImageView.setVisibility(View.GONE);
        resultTextView.setVisibility(View.GONE);
        wordDisplayTextView.setVisibility(View.GONE);
        meaningTextView.setVisibility(View.GONE);
        phoneticTextView.setVisibility(View.GONE);
    }

    private void setupListeners() {
        captureButton.setOnClickListener(v -> capturePhoto());

        lookupButton.setOnClickListener(v -> {
            String scannedText = resultTextView.getText().toString();
            if (!scannedText.isEmpty()) {
                viewModel.handleEvent(new ScanUiEvent.LookupWord(scannedText));
            }
        });

        saveButton.setOnClickListener(v -> {
            viewModel.handleEvent(ScanUiEvent.SaveWord.INSTANCE);
        });

        clearButton.setOnClickListener(v -> {
            viewModel.handleEvent(ScanUiEvent.ClearResult.INSTANCE);
            resetUI();
        });
    }

    private void observeState() {
        viewModel.getState().observe(getViewLifecycleOwner(), this::updateUI);
    }

    private void updateUI(ScanUiState state) {
        if (state instanceof ScanUiState.Loading) {
            progressBar.setVisibility(View.VISIBLE);
            captureButton.setEnabled(false);
            return;
        }

        progressBar.setVisibility(View.GONE);
        captureButton.setEnabled(true);

        if (state instanceof ScanUiState.Idle) {
            resetUI();
        } else if (state instanceof ScanUiState.ScannedText) {
            String text = ((ScanUiState.ScannedText) state).getText();
            resultTextView.setVisibility(View.VISIBLE);
            resultTextView.setText("Scanned: " + text);
            lookupButton.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
            wordDisplayTextView.setVisibility(View.GONE);
            meaningTextView.setVisibility(View.GONE);
            phoneticTextView.setVisibility(View.GONE);
        } else if (state instanceof ScanUiState.WordFound) {
            com.example.englishapp.core.model.Vocabulary vocab =
                    ((ScanUiState.WordFound) state).getVocabulary();

            wordDisplayTextView.setVisibility(View.VISIBLE);
            wordDisplayTextView.setText(vocab.getWord());

            phoneticTextView.setVisibility(View.VISIBLE);
            phoneticTextView.setText("Phonetic: " + vocab.getPhonetic());

            meaningTextView.setVisibility(View.VISIBLE);
            meaningTextView.setText("Meaning: " + vocab.getMeaning());

            lookupButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.VISIBLE);

        } else if (state instanceof ScanUiState.WordSaved) {
            String word = ((ScanUiState.WordSaved) state).getWord();
            Toast.makeText(requireContext(), "Saved: " + word, Toast.LENGTH_SHORT).show();
            resetUI();
        } else if (state instanceof ScanUiState.Error) {
            String message = ((ScanUiState.Error) state).getMessage();
            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_LONG).show();
            resetUI();
        }
    }

    private void resetUI() {
        capturedImageView.setVisibility(View.GONE);
        resultTextView.setVisibility(View.GONE);
        wordDisplayTextView.setVisibility(View.GONE);
        meaningTextView.setVisibility(View.GONE);
        phoneticTextView.setVisibility(View.GONE);
        lookupButton.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
        clearButton.setVisibility(View.GONE);
        capturedBitmap = null;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(requireContext(), "Camera error: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()),
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
                        super.onCaptureSuccess(image);

                        // Convert ImageProxy to Bitmap
                        androidx.camera.core.ImageProxy imageProxy = image;
                        Bitmap bitmap = imageProxyToBitmap(imageProxy);
                        image.close();

                        if (bitmap != null) {
                            capturedBitmap = bitmap;
                            capturedImageView.setVisibility(View.VISIBLE);
                            capturedImageView.setImageBitmap(bitmap);

                            // Process OCR
                            viewModel.handleEvent(new ScanUiEvent.ScanImage(bitmap));
                        } else {
                            Toast.makeText(requireContext(), "Failed to capture image",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        super.onError(exception);
                        Toast.makeText(requireContext(), "Capture error: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Bitmap imageProxyToBitmap(androidx.camera.core.ImageProxy image) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            // Lấy dữ liệu YUV từ ImageProxy
            android.media.Image mediaImage = image.getImage();
            if (mediaImage == null) return null;

            // Chuyển đổi YUV sang JPEG
            java.nio.ByteBuffer buffer = mediaImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            // Decode thành bitmap
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}