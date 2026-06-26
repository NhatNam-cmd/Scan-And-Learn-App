package com.example.englishapp.feature.scan.presentation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

import com.google.mlkit.vision.text.Text;
import com.example.englishapp.R;
import com.example.englishapp.core.service.OcrManager;
import com.example.englishapp.feature.scan.processor.ScanImageProcessor;
import com.example.englishapp.feature.scan.ui.ScanWordPickerBottomSheet;
import com.google.android.material.button.MaterialButton;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.camera.core.ImageProxy;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;

import com.example.englishapp.feature.scan.model.ScanWordItem;
import com.example.englishapp.feature.scan.model.WordCandidate;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.example.englishapp.core.common.ApiResult;
import com.example.englishapp.core.model.VocabularyLookup;
@AndroidEntryPoint
public class ScanFragment extends Fragment
        implements ScanWordPickerBottomSheet.OnWordSelectedListener {

    @Inject
    OcrManager ocrManager;

    //==============================
    // ViewModel
    //==============================

    private ScanViewModel viewModel;

    //==============================
    // Views
    //==============================

    private PreviewView previewView;

    private MaterialButton captureButton;

    private ProgressBar progressBar;

    private View loadingOverlay;

    //==============================
    // CameraX
    //==============================

    private ImageCapture imageCapture;

    private ProcessCameraProvider cameraProvider;

    //==============================
    // Processor
    //==============================

    private final ScanImageProcessor imageProcessor =
            new ScanImageProcessor();

    //==============================
    // Permission Launcher
    //==============================

    private final ActivityResultLauncher<String> requestCameraPermission =
            registerForActivityResult(
                    new ActivityResultContracts.RequestPermission(),
                    granted -> {

                        if (granted) {

                            startCamera();

                        } else {

                            Toast.makeText(
                                    requireContext(),
                                    "Camera permission denied",
                                    Toast.LENGTH_SHORT
                            ).show();

                        }

                    });

    //==========================================================
    // Lifecycle
    //==========================================================

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        return inflater.inflate(
                R.layout.fragment_scan,
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

        viewModel = new ViewModelProvider(this)
                .get(ScanViewModel.class);

        initViews(view);

        setupClickListeners();
        observeViewModel();
        checkCameraPermission();

    }

    //==========================================================
    // Init
    //==========================================================

    private void initViews(View view) {

        previewView = view.findViewById(R.id.preview_view);

        captureButton = view.findViewById(R.id.captureButton);

        progressBar = view.findViewById(R.id.progressBar);

        loadingOverlay = view.findViewById(R.id.loading_overlay);

    }

    private void setupClickListeners() {

        captureButton.setOnClickListener(v -> captureImage());

    }

    //==========================================================
    // Permission
    //==========================================================

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED) {

            startCamera();

        } else {

            requestCameraPermission.launch(
                    Manifest.permission.CAMERA
            );

        }

    }

    //==========================================================
    // Camera
    //==========================================================

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(requireContext());

        future.addListener(() -> {

            try {

                cameraProvider = future.get();

                bindCameraUseCases();

            } catch (ExecutionException | InterruptedException e) {

                Toast.makeText(
                        requireContext(),
                        "Unable to start camera",
                        Toast.LENGTH_SHORT
                ).show();

            }

        }, ContextCompat.getMainExecutor(requireContext()));

    }

    private void bindCameraUseCases() {

        Preview preview =
                new Preview.Builder()
                        .build();

        preview.setSurfaceProvider(
                previewView.getSurfaceProvider()
        );

        imageCapture =
                new ImageCapture.Builder()
                        .setCaptureMode(
                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build();

        CameraSelector selector =
                CameraSelector.DEFAULT_BACK_CAMERA;

        cameraProvider.unbindAll();
        try {
        cameraProvider.bindToLifecycle(
                getViewLifecycleOwner(),
                selector,
                preview,
                imageCapture
        );
        }

        catch (Exception e) {

            Toast.makeText(
                    requireContext(),
                    "Camera binding failed",
                    Toast.LENGTH_SHORT
            ).show();

        }

    }

    //==========================================================
    // Helpers
    //==========================================================

    private void showLoading(boolean show) {

        loadingOverlay.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

    }

    //==========================================================
    // TODO
    //==========================================================

    private void captureImage() {

        if (imageCapture == null) {

            Toast.makeText(
                    requireContext(),
                    "Camera chưa sẵn sàng",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        showLoading(true);

        imageCapture.takePicture(

                ContextCompat.getMainExecutor(requireContext()),

                new ImageCapture.OnImageCapturedCallback() {

                    @Override
                    public void onCaptureSuccess(
                            @NonNull ImageProxy image
                    ) {

                        Bitmap bitmap;

                        try {

                            bitmap = imageProxyToBitmap(image);

                        }

                        finally {

                            image.close();

                        }

                        if (bitmap == null) {

                            showLoading(false);

                            Toast.makeText(
                                    requireContext(),
                                    "Không thể đọc ảnh",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        processOcr(bitmap);

                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception
                    ) {

                        showLoading(false);

                        Toast.makeText(
                                requireContext(),
                                "Không thể chụp ảnh",
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                }

        );

    }
    private void processOcr(Bitmap bitmap) {

        ocrManager.recognizeText(

                bitmap,

                new OcrManager.OcrCallback() {

                    @Override
                    public void onSuccess(Text visionText) {

                        showLoading(false);

                        processDetectedText(
                                visionText.getText()
                        );

                    }

                    @Override
                    public void onError(Exception e) {

                        showLoading(false);

                        Toast.makeText(
                                requireContext(),
                                "OCR thất bại",
                                Toast.LENGTH_SHORT
                        ).show();

                    }

                });


    }
    private void processDetectedText(String rawText) {

        List<ScanWordItem> words =
                imageProcessor.extractWords(rawText);

        if (words.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Không phát hiện từ tiếng Anh",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }

        List<WordCandidate> candidates =
                new ArrayList<>();

        int maxFrequency = 1;

        for (ScanWordItem item : words) {

            if (item.getFrequency() > maxFrequency) {

                maxFrequency = item.getFrequency();

            }

        }

        for (ScanWordItem item : words) {

            if (item.isStopWord()) {

                continue;

            }

            float confidence =
                    (float) item.getFrequency()
                            / maxFrequency;

            candidates.add(

                    new WordCandidate(

                            item.getWord(),

                            confidence

                    )

            );

        }

        if (candidates.isEmpty()) {

            Toast.makeText(
                    requireContext(),
                    "Không tìm thấy từ phù hợp",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }

        ScanWordPickerBottomSheet sheet =
                new ScanWordPickerBottomSheet();

        sheet.submitWords(candidates);

        sheet.setOnWordSelectedListener(this);

        sheet.show(
                getChildFragmentManager(),
                "ScanWords"
        );

    }
    private Bitmap imageProxyToBitmap(ImageProxy image) {

        try {

            ImageProxy.PlaneProxy[] planes =
                    image.getPlanes();

            ByteBuffer yBuffer =
                    planes[0].getBuffer();

            ByteBuffer uBuffer =
                    planes[1].getBuffer();

            ByteBuffer vBuffer =
                    planes[2].getBuffer();

            int ySize = yBuffer.remaining();

            int uSize = uBuffer.remaining();

            int vSize = vBuffer.remaining();

            byte[] nv21 =
                    new byte[
                            ySize +
                                    uSize +
                                    vSize
                            ];

            yBuffer.get(
                    nv21,
                    0,
                    ySize
            );

            vBuffer.get(
                    nv21,
                    ySize,
                    vSize
            );

            uBuffer.get(
                    nv21,
                    ySize + vSize,
                    uSize
            );

            YuvImage yuvImage =
                    new YuvImage(
                            nv21,
                            ImageFormat.NV21,
                            image.getWidth(),
                            image.getHeight(),
                            null
                    );

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();

            yuvImage.compressToJpeg(

                    new Rect(
                            0,
                            0,
                            image.getWidth(),
                            image.getHeight()
                    ),

                    100,

                    out

            );

            byte[] bytes =
                    out.toByteArray();

            return android.graphics.BitmapFactory
                    .decodeByteArray(
                            bytes,
                            0,
                            bytes.length
                    );

        }

        catch (Exception e) {

            return null;

        }

    }
    private void observeViewModel() {

        viewModel.getLookupResult().observe(

                getViewLifecycleOwner(),

                result -> {

                    if (result == null) {

                        return;

                    }

                    if (result instanceof ApiResult.Loading) {

                        showLoading(true);

                        return;

                    }

                    showLoading(false);

                    if (result instanceof ApiResult.Success) {

                        VocabularyLookup lookup =

                                ((ApiResult.Success<VocabularyLookup>) result)
                                        .getData();

                        if (lookup != null) {

                            WordPreviewDialog
                                    .newInstance(lookup)
                                    .show(
                                            getChildFragmentManager(),
                                            "PreviewDialog"
                                    );

                        }

                        viewModel.clearLookupResult();

                        return;

                    }

                    if (result instanceof ApiResult.Error) {

                        Toast.makeText(

                                requireContext(),

                                ((ApiResult.Error<?>) result)
                                        .getMessage(),

                                Toast.LENGTH_SHORT

                        ).show();

                        viewModel.clearLookupResult();

                    }

                });

    }
    @Override
    public void onWordSelected(String word) {

        if (word == null) {

            return;

        }

        word = word.trim();

        if (word.isEmpty()) {

            return;

        }

        viewModel.lookupScannedWord(word);

    }
    }