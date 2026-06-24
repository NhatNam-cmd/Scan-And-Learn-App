package com.example.englishapp.core.service;
import dagger.hilt.android.qualifiers.ApplicationContext;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class TtsManager {

    public interface OnReadyCallback {
        void onReady(boolean success);
    }

    public interface OnSpeakListener {
        void onSpeakStart(String utteranceId);
        void onSpeakDone(String utteranceId);
        void onSpeakError(String utteranceId);
    }

    private TextToSpeech tts;
    private boolean isReady = false;
    private OnSpeakListener speakListener;

    @Inject
    public TtsManager(@ApplicationContext Context context) {
        tts = new TextToSpeech(context.getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                // Prefer English (story content is English vocabulary)
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to device default
                    tts.setLanguage(Locale.getDefault());
                }
                tts.setSpeechRate(0.9f);   // Slightly slower for learners
                tts.setPitch(1.0f);
                isReady = true;
            } else {
                isReady = false;
            }
        });

        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (speakListener != null) speakListener.onSpeakStart(utteranceId);
            }

            @Override
            public void onDone(String utteranceId) {
                if (speakListener != null) speakListener.onSpeakDone(utteranceId);
            }
            @Override
            public void onError(String utteranceId) {
                if (speakListener != null) speakListener.onSpeakError(utteranceId);
            }
        });
    }

    /** Speak the given text. Safe to call even if not yet ready. */
    public void speak(String text) {
        if (!isReady || tts == null || text == null || text.isEmpty()) return;
        tts.stop();
        android.os.Bundle params = new android.os.Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "story_tts");
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, params, "story_tts");
    }

    /** Stop any ongoing speech. */
    public void stop() {
        if (tts != null && isReady) tts.stop();
    }

    /** @return true if TTS engine is currently speaking */
    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }

    public boolean isReady() {
        return isReady;
    }

    /** Set a listener to track speak start/done/error events. */
    public void setSpeakListener(OnSpeakListener listener) {
        this.speakListener = listener;
    }

    /** Must be called in Fragment.onDestroyView() to release TTS resources. */
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        isReady = false;
    }
}