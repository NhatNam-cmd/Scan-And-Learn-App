package com.example.englishapp.core.service;

import android.content.Context;
import android.media.MediaPlayer;
import com.example.englishapp.core.common.ExecutorProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AudioCacheManager {
    private final File cacheDir;
    private final ExecutorProvider executorProvider;

    @Inject
    public AudioCacheManager(Context context, ExecutorProvider executorProvider) {
        this.cacheDir = context.getCacheDir();
        this.executorProvider = executorProvider;
    }

    public CompletableFuture<File> getAudioFile(String word, String audioUrl) {
        CompletableFuture<File> future = new CompletableFuture<>();

        executorProvider.getIoExecutor().execute(() -> {
            try {
                String extension = audioUrl.substring(audioUrl.lastIndexOf("."));
                if (!extension.matches("\\.[a-zA-Z0-9]+")) {
                    extension = ".mp3";
                }

                String fileName = word.toLowerCase() + extension;
                File audioDir = new File(cacheDir, "audio");
                if (!audioDir.exists()) {
                    audioDir.mkdirs();
                }

                File cacheFile = new File(audioDir, fileName);

                if (cacheFile.exists() && cacheFile.length() > 0) {
                    future.complete(cacheFile);
                    return;
                }

                if (downloadAudio(audioUrl, cacheFile)) {
                    future.complete(cacheFile);
                } else {
                    future.completeExceptionally(new Exception("Download failed"));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    private boolean downloadAudio(String urlString, File destination) {
        try (InputStream in = new URL(urlString).openStream();
             FileOutputStream out = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void playAudio(File file) {
        executorProvider.postToMainThread(() -> {
            try {
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(file.getAbsolutePath());
                mediaPlayer.prepareAsync();

                mediaPlayer.setOnPreparedListener(MediaPlayer::start);

                mediaPlayer.setOnCompletionListener(MediaPlayer::release);

                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    mp.release();
                    return true;
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}