package com.example.englishapp.core.service;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TtsManager {

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private final Context context;

    @Inject
    public TtsManager(Context context) {
        this.context = context;
        initializeTts();
    }

    private void initializeTts() {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                isInitialized = status == TextToSpeech.SUCCESS;
                if (isInitialized) {
                    tts.setLanguage(Locale.US);
                    tts.setSpeechRate(1.0f);
                    tts.setPitch(1.0f);
                }
            }
        });
    }

    /**
     * Phát âm thanh của từ
     */
    public CompletableFuture<Boolean> speakWord(String word, float speechRate) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!isInitialized || tts == null) {
            future.completeExceptionally(new Exception("TTS not initialized"));
            return future;
        }

        tts.setSpeechRate(speechRate);
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {}

            @Override
            public void onDone(String utteranceId) {
                future.complete(true);
            }

            @Override
            public void onError(String utteranceId) {
                future.completeExceptionally(new Exception("TTS error"));
            }
        });

        int result = tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, word);
        if (result != TextToSpeech.SUCCESS) {
            future.completeExceptionally(new Exception("Failed to start TTS"));
        }

        return future;
    }

    public CompletableFuture<Boolean> speakWord(String word) {
        return speakWord(word, 1.0f);
    }

    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
            isInitialized = false;
        }
    }
}