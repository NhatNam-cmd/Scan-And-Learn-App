package com.example.englishapp.core.service;

import android.graphics.Bitmap;
import com.example.englishapp.core.utils.ExecutorProvider;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OcrManager {

    private final TextRecognizer recognizer;
    private final ExecutorProvider executorProvider;

    public interface OcrCallback {
        void onSuccess(String text);
        void onError(Exception e);
    }

    @Inject
    public OcrManager(ExecutorProvider executorProvider) {
        this.executorProvider = executorProvider;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void recognizeText(Bitmap bitmap, OcrCallback callback) {
        executorProvider.getIoExecutor().execute(() -> {
            try {
                Bitmap resizedBitmap = downscaleBitmap(bitmap, 1024);
                InputImage image = InputImage.fromBitmap(resizedBitmap, 0);

                recognizer.process(image)
                        .addOnSuccessListener(executorProvider.getMainExecutor(), visionText -> {
                            callback.onSuccess(visionText.getText());
                        })
                        .addOnFailureListener(executorProvider.getMainExecutor(), callback::onError);
            } catch (Exception e) {
                executorProvider.postToMainThread(() -> callback.onError(e));
            }
        });
    }

    private Bitmap downscaleBitmap(Bitmap original, int maxDimension) {
        int width = original.getWidth();
        int height = original.getHeight();

        if (width <= maxDimension && height <= maxDimension) {
            return original;
        }

        float ratio = (float) width / height;
        int newWidth;
        int newHeight;

        if (ratio > 1) {
            newWidth = maxDimension;
            newHeight = (int) (maxDimension / ratio);
        } else {
            newHeight = maxDimension;
            newWidth = (int) (maxDimension * ratio);
        }

        return Bitmap.createScaledBitmap(original, newWidth, newHeight, true);
    }
}