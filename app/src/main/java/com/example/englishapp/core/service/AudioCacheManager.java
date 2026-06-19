package com.example.englishapp.core.service;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AudioCacheManager {

    private final File cacheDir;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Inject
    public AudioCacheManager(Context context) {
        this.cacheDir = context.getCacheDir();
    }

    /**
     * Lấy file âm thanh từ cache hoặc download
     */
    public CompletableFuture<File> getAudioFile(String word, String audioUrl) {
        CompletableFuture<File> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                String fileName = word.toLowerCase() + ".mp3";
                File audioDir = new File(cacheDir, "audio");
                if (!audioDir.exists()) {
                    audioDir.mkdirs();
                }

                File cacheFile = new File(audioDir, fileName);

                // Nếu file đã tồn tại trong cache
                if (cacheFile.exists() && cacheFile.length() > 0) {
                    future.complete(cacheFile);
                    return;
                }

                // Download audio
                boolean downloaded = downloadAudio(audioUrl, cacheFile);
                if (downloaded && cacheFile.exists() && cacheFile.length() > 0) {
                    future.complete(cacheFile);
                } else {
                    future.completeExceptionally(new Exception("Failed to download audio"));
                }

            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private boolean downloadAudio(String audioUrl, File outputFile) {
        try {
            URL url = new URL(audioUrl);
            java.net.HttpURLConnection connection =
                    (java.net.HttpURLConnection) url.openConnection();
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();
            connection.disconnect();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public MediaPlayer playAudio(File file) {
        try {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(file.getAbsolutePath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            return mediaPlayer;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clearOldCache(int days) {
        File audioDir = new File(cacheDir, "audio");
        if (!audioDir.exists()) return;

        long currentTime = System.currentTimeMillis();
        long maxAge = days * 24 * 60 * 60 * 1000L;

        File[] files = audioDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (currentTime - file.lastModified() > maxAge) {
                    file.delete();
                }
            }
        }
    }
}