package com.example.englishapp.core.service;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Queue;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TtsManager implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private boolean isInitialized = false;
    private final Queue<String> pendingWords = new LinkedList<>();

    @Inject
    public TtsManager(Context context) {
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                isInitialized = true;
                flushQueue();
            }
        }
    }

    public void speakWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            return;
        }
        if (isInitialized) {
            tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            pendingWords.add(word);
        }
    }

    private void flushQueue() {
        while (!pendingWords.isEmpty()) {
            String word = pendingWords.poll();
            if (word != null) {
                tts.speak(word, TextToSpeech.QUEUE_ADD, null, null);
            }
        }
    }

    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}