package com.bimbr.choreo.view;

import static java.lang.Math.max;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.android.media.NotifyingMediaPlayer.OnPausedListener;
import com.bimbr.android.media.NotifyingMediaPlayer.OnStartedListener;

/**
 * A view that displays the choreography chart.
 * 
 * @author mmakowski
 */
public class ChoreographyView extends View {
    private static final double  SECOND_WIDTH_CM           = 1.0f;
    private static final int     PLAYBACK_TRACKING_FREQ_MS = 25;

    private static final String LOG_TAG          = "ChoreoView";

    private static final int    DIP              = 160;
    private static final double INCHES_PER_CM    = 0.393700787;
    private static final int    MILLIS_IN_SECOND = 1000;
    private static final double DIP_PER_CM       = DIP * INCHES_PER_CM;

    private final Timer timer = new Timer(true);

    private final Paint barBarPaint;
    private final Paint playBarPaint;
    private final float displayDpi;

    // mutable fields
    private NotifyingMediaPlayer player;
    private TimerTask            playbackTrackingTask;

    public ChoreographyView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        barBarPaint = newBarPaint(0xff202020);
        playBarPaint = newBarPaint(0xff33B5E5);
        displayDpi = getDisplayDpi();
    }

    private float getDisplayDpi() {
        if (isInEditMode()) return 1f;
        final DisplayMetrics metrics = new DisplayMetrics();
        ((android.view.WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    private Paint newBarPaint(final int colour) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colour);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    public void onDraw(final Canvas canvas) {
        drawBarBars(canvas);
        drawPlayBar(canvas);
    }

    private void drawPlayBar(final Canvas canvas) {
        if (player != null) {
            final int playBarPos = playBarPosition();
            canvas.drawLine(playBarPos, 0, playBarPos, getHeight(), playBarPaint);
        }
    }

    private int playBarPosition() {
        assert player != null;
        return msToX(player.getCurrentPosition());
    }

    private void drawBarBars(final Canvas canvas) {
        canvas.drawLine(0, 0, getWidth(), getHeight(), barBarPaint);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int minw = getPaddingLeft() + getPaddingRight() + max(getSuggestedMinimumWidth(), audioWidth()) ;
        final int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        final int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        final int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        Log.d(LOG_TAG, String.format("calculated w=%d, h=%d", w, h));
        setMeasuredDimension(w, h);
    }

    private int audioWidth() {
        return player == null ? 0 : msToX(player.getDuration());
    }

    private int msToX(final int ms) {
        return ms * cmToPixels(SECOND_WIDTH_CM) / MILLIS_IN_SECOND;
    }

    private int cmToPixels(final double sizeInCm) {
        return (int) (sizeInCm * DIP_PER_CM * displayDpi);
    }

    public void setMediaPlayer(final NotifyingMediaPlayer player) {
        this.player = player;
        player.setOnStartedListener(new OnStartedListener() {
            @Override
            public void onStarted(final NotifyingMediaPlayer player) {
                startTrackingPlayback();
            }
        });
        player.setOnPausedListener(new OnPausedListener() {
            @Override
            public void onPaused(final NotifyingMediaPlayer player) {
                stopTrackingPlayback();
            }
        });
        requestLayout();
    }

    private void stopTrackingPlayback() {
        if (playbackTrackingTask != null) {
            playbackTrackingTask.cancel();
            playbackTrackingTask = null;
        }
    }

    private void startTrackingPlayback() {
        playbackTrackingTask = new PlaybackTrackingTask();
        timer.schedule(playbackTrackingTask, 0, PLAYBACK_TRACKING_FREQ_MS);
    }

    public class PlaybackTrackingTask extends TimerTask {
        @Override
        public void run() {
            // TODO: restricted invalidation for better performance
            postInvalidate();
        }
    }
}
