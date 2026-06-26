package com.example.englishapp.feature.story.presentation;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.core.content.ContextCompat;

import com.example.englishapp.R;

/**
 * AudioWaveView – a custom View that renders 5 animated bars resembling an audio waveform.
 *
 * Each bar animates independently at a slightly different speed/phase to produce
 * a natural "dancing bars" effect while TTS is speaking.
 *
 * Call startWave() to begin animation, stopWave() to end it.
 */
public class AudioWaveView extends View {

    private static final int BAR_COUNT = 5;

    // Current animated height fraction for each bar (0.0 – 1.0)
    private final float[] barHeights = new float[BAR_COUNT];
    private final ValueAnimator[] animators = new ValueAnimator[BAR_COUNT];

    private Paint barPaint;
    private float barWidth;
    private float barSpacing;

    // Different animation durations per bar for a more organic look
    private static final long[] DURATIONS = {380L, 280L, 460L, 320L, 400L};
    // Phase offsets (delay before each bar starts) so they're never in sync
    private static final long[] DELAYS = {0L, 80L, 160L, 40L, 120L};

    public AudioWaveView(Context context) {
        super(context);
        init(context);
    }

    public AudioWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AudioWaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(ContextCompat.getColor(context, R.color.light_on_primary));
        barPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Compute bar width and spacing based on available width
        barSpacing = w / (float) (BAR_COUNT * 2 + 1);
        barWidth = barSpacing;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int h = getHeight();
        int minBarH = (int) (h * 0.15f);

        for (int i = 0; i < BAR_COUNT; i++) {
            float barH = minBarH + barHeights[i] * (h - minBarH);
            float left  = barSpacing + i * (barWidth + barSpacing);
            float top   = (h - barH) / 2f;
            float right = left + barWidth;
            float bottom = top + barH;
            float radius = barWidth / 2f;
            canvas.drawRoundRect(left, top, right, bottom, radius, radius, barPaint);
        }
    }

    /** Start the wave animation (call when TTS begins speaking). */
    public void startWave() {
        stopWave(); // cancel any previous
        for (int i = 0; i < BAR_COUNT; i++) {
            final int idx = i;
            ValueAnimator anim = ValueAnimator.ofFloat(0.15f, 1f);
            anim.setDuration(DURATIONS[i]);
            anim.setStartDelay(DELAYS[i]);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.REVERSE);
            anim.setInterpolator(new AccelerateDecelerateInterpolator());
            anim.addUpdateListener(animation -> {
                barHeights[idx] = (float) animation.getAnimatedValue();
                invalidate();
            });
            anim.start();
            animators[i] = anim;
        }
        setVisibility(VISIBLE);
    }

    /** Stop the wave animation and hide the view (call when TTS stops). */
    public void stopWave() {
        for (int i = 0; i < BAR_COUNT; i++) {
            if (animators[i] != null) {
                animators[i].cancel();
                animators[i] = null;
            }
            barHeights[i] = 0.15f;
        }
        invalidate();
        setVisibility(GONE);
    }

    /** Change bar color (useful to match theme). */
    public void setBarColor(int color) {
        barPaint.setColor(color);
        invalidate();
    }
}
