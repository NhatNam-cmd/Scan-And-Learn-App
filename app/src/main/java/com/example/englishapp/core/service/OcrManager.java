package com.example.englishapp.core.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OcrManager {

    private final TextRecognizer recognizer;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public OcrManager() {
        this.recognizer = TextRecognition.getClient(
                TextRecognizerOptions.DEFAULT_OPTIONS
        );
    }

    /**
     * Nhận diện văn bản từ bitmap
     * Sử dụng CompletableFuture để hỗ trợ async
     */
    public CompletableFuture<String> recognizeText(Bitmap bitmap) {
        CompletableFuture<String> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                // Downscale ảnh để tránh OOM
                Bitmap scaledBitmap = downscaleBitmap(bitmap, 1024, 1024);
                InputImage image = InputImage.fromBitmap(scaledBitmap, 0);

                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text result) {
                                StringBuilder fullText = new StringBuilder();
                                for (Text.TextBlock block : result.getTextBlocks()) {
                                    if (block.getText() != null) {
                                        fullText.append(block.getText()).append(" ");
                                    }
                                }

                                // Giải phóng memory
                                if (scaledBitmap != bitmap && !scaledBitmap.isRecycled()) {
                                    scaledBitmap.recycle();
                                }
                                if (!bitmap.isRecycled()) {
                                    bitmap.recycle();
                                }

                                future.complete(fullText.toString().trim());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                future.completeExceptionally(e);
                            }
                        });

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Downscale ảnh để giảm kích thước, tránh OOM
     */
    private Bitmap downscaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width <= maxWidth && height <= maxHeight) {
            return bitmap;
        }

        float ratio = Math.min((float) maxWidth / width, (float) maxHeight / height);
        int newWidth = (int) (width * ratio);
        int newHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    /**
     * Chuyển byte array thành bitmap
     */
    public Bitmap byteArrayToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
}