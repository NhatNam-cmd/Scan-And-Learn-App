package com.example.englishapp.feature.scan.presentation.state;

import android.graphics.Bitmap;

public abstract class ScanUiEvent {

    public static final class ScanImage extends ScanUiEvent {
        private final Bitmap bitmap;
        public ScanImage(Bitmap bitmap) { this.bitmap = bitmap; }
        public Bitmap getBitmap() { return bitmap; }
    }

    public static final class LookupWord extends ScanUiEvent {
        private final String word;
        public LookupWord(String word) { this.word = word; }
        public String getWord() { return word; }
    }

    public static final class SaveWord extends ScanUiEvent {
        public static final SaveWord INSTANCE = new SaveWord();
        private SaveWord() {}
    }

    public static final class ClearResult extends ScanUiEvent {
        public static final ClearResult INSTANCE = new ClearResult();
        private ClearResult() {}
    }
}