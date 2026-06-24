package com.example.englishapp.feature.scan.presentation;
import com.google.mlkit.vision.text.Text;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.englishapp.R;
import com.example.englishapp.core.model.VocabularyLookup;
import com.example.englishapp.core.service.OcrManager;
import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.database.entity.VocabularyEntity;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ScanFragment extends Fragment {

    private ScanViewModel viewModel;
    private PreviewView previewView;
    private MaterialButton captureButton;
    private ProgressBar progressBar;
    private View loadingOverlay;
    private ImageCapture imageCapture;

    private VocabularyEntity pendingVocabulary;

    @Inject
    public OcrManager ocrManager;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Toast.makeText(requireContext(), "Cần cấp quyền Camera để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ScanViewModel.class);

        previewView = view.findViewById(R.id.preview_view);
        captureButton = view.findViewById(R.id.captureButton);
        progressBar = view.findViewById(R.id.progressBar);
        loadingOverlay = view.findViewById(R.id.loading_overlay);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        captureButton.setOnClickListener(v -> takePhoto());

        setupObservers();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(requireContext(), "Lỗi khởi tạo Camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        loadingOverlay.setVisibility(View.VISIBLE);
        captureButton.setEnabled(false);

        imageCapture.takePicture(ContextCompat.getMainExecutor(requireContext()), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                Bitmap bitmap = imageProxyToBitmap(image);
                image.close();
                processImageWithOcr(bitmap);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                loadingOverlay.setVisibility(View.GONE);
                captureButton.setEnabled(true);
                Toast.makeText(requireContext(), "Lỗi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Bitmap imageProxyToBitmap(ImageProxy image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Matrix matrix = new Matrix();
        matrix.postRotate(image.getImageInfo().getRotationDegrees());
        // FIX LỖI: Sửa bitmap.Height() thành bitmap.getHeight()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private void processImageWithOcr(Bitmap bitmap) {
        ocrManager.recognizeText(bitmap, new OcrManager.OcrCallback() {
            @Override
            public void onSuccess(Text visionText) {
                loadingOverlay.setVisibility(View.GONE);
                captureButton.setEnabled(true);

                showTextSelectionDialog(visionText.getText());
            }

            @Override
            public void onSuccess(String text) {
                if (text != null && !text.trim().isEmpty()) {
                    viewModel.lookupScannedWord(text.trim());
                } else {
                    loadingOverlay.setVisibility(View.GONE);
                    captureButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Không tìm thấy văn bản trong ảnh", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    loadingOverlay.setVisibility(View.GONE);
                    captureButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Lỗi nhận diện văn bản", Toast.LENGTH_SHORT).show();
                }
            });
        }

    private void setupObservers() {
        viewModel.lookupResult.observe(getViewLifecycleOwner(), result -> {
            if (result.getClass() == ApiResult.Loading.class) {
                loadingOverlay.setVisibility(View.VISIBLE);
            } else if (result instanceof ApiResult.Success) {
                loadingOverlay.setVisibility(View.GONE);
                captureButton.setEnabled(true);

                ApiResult.Success<VocabularyLookup> successResult = (ApiResult.Success<VocabularyLookup>) result;
                if (successResult.getData() != null) {
                    WordPreviewDialog dialog = WordPreviewDialog.newInstance(successResult.getData());
                    dialog.show(getChildFragmentManager(), "WordPreviewDialog");
                } else {
                    Toast.makeText(requireContext(), "Không lấy được dữ liệu từ vựng", Toast.LENGTH_SHORT).show();
                }
                viewModel.resetLookupState();
            }

             else if (result instanceof ApiResult.Error) {
                loadingOverlay.setVisibility(View.GONE);
                captureButton.setEnabled(true);
                Toast.makeText(requireContext(), ((ApiResult.Error<?>) result).getMessage(), Toast.LENGTH_LONG).show();
                viewModel.resetLookupState();
            }
        });

        viewModel.saveResult.observe(getViewLifecycleOwner(), result -> {
            if (result.getClass() == ApiResult.Loading.class) {
                loadingOverlay.setVisibility(View.VISIBLE);
            } else if (result instanceof ApiResult.Success) {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), ((ApiResult.Success<String>) result).getData(), Toast.LENGTH_SHORT).show();
                viewModel.resetSaveState();
                pendingVocabulary = null;
            } else if (result instanceof ApiResult.Fallback) {
                loadingOverlay.setVisibility(View.GONE);
                showDuplicateChoiceDialog();
                viewModel.resetSaveState();
            } else if (result instanceof ApiResult.Error) {
                loadingOverlay.setVisibility(View.GONE);
                Toast.makeText(requireContext(), ((ApiResult.Error<?>) result).getMessage(), Toast.LENGTH_SHORT).show();
                viewModel.resetSaveState();
            }
        });
    }

    public void setPendingVocabulary(VocabularyEntity entity) {
        this.pendingVocabulary = entity;
    }

    private void showDuplicateChoiceDialog() {
        if (pendingVocabulary == null) return;

        new AlertDialog.Builder(requireContext())
                .setTitle("Từ vựng đã tồn tại")
                .setMessage("Từ vựng này đã có trong hệ thống. Bạn muốn cập nhật ngữ cảnh cho từ cũ hay lưu thành một từ mới hoàn toàn?")
                .setPositiveButton("Cập nhật", (dialog, which) -> {
                    viewModel.saveScannedVocabulary(pendingVocabulary, true);
                })
                .setNegativeButton("Lưu từ mới", (dialog, which) -> {
                    pendingVocabulary = new VocabularyEntity(
                            0L,
                            pendingVocabulary.getTopicId(),
                            pendingVocabulary.getWord(),
                            pendingVocabulary.getMeaning(),
                            pendingVocabulary.getPhonetic(),
                            pendingVocabulary.getExampleSentence(),
                            pendingVocabulary.getImagePath(),
                            pendingVocabulary.getAudioPath(),
                            null, // FIX LỖI: Biến note truyền vào null
                            pendingVocabulary.getSourceType(), // Biến sourceType
                            0,
                            false,
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            System.currentTimeMillis()
                    );
                    viewModel.saveScannedVocabulary(pendingVocabulary, false);
                })
                .setNeutralButton("Hủy", (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
    private void showTextSelectionDialog(String rawText) {
        EditText editText = new EditText(requireContext());
        editText.setText(rawText);
        editText.setPadding(32, 32, 32, 32);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Chọn từ cần tra")
                .setMessage("Chỉnh sửa lại văn bản nếu cần:")
                .setView(editText)
                .setPositiveButton("Tra từ", (dialog, which) -> {
                    String selectedText = editText.getText().toString().trim();
                    if (!selectedText.isEmpty()) {
                        viewModel.lookupScannedWord(selectedText);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }
}