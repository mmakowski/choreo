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
import android.view.MotionEvent;
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
    private static final int    MILLIS_IN_MINUTE = 1000 * 60;
    private static final double DIP_PER_CM       = DIP * INCHES_PER_CM;

    private final Timer timer = new Timer(true);

    private static final Paint barBarPaint     = paint(0xff202020);
    private static final Paint playBarPaint    = paint(0xff33B5E5);
    private static final Paint measureBarPaint = paint(0xffa0a0a0);

    private final float displayDpi = getDisplayDpi();

    // mutable fields

    private NotifyingMediaPlayer player;
    // bar positions and measure points are cached, based on the assumption that the view size will
    // change infrequently relative to how often bar positions need to be checked
    private int[]                barPositions;
    private int[]                measurePositions;

    private int                  beatsPerMinute = 120;
    private int                  measuresPerBar = 4;

    private TimerTask            playbackTrackingTask;

    public ChoreographyView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onDraw(final Canvas canvas) {
        drawMeasureBars(canvas);
        drawBarBars(canvas);
        drawPlayBar(canvas);
    }

    private void drawBarBars(final Canvas canvas) {
        if (player != null) {
            for (final int pos : barPositions) {
                canvas.drawLine(pos, 0, pos, getHeight(), barBarPaint);
            }
        }
    }

    private void drawMeasureBars(final Canvas canvas) {
        if (player != null) {
            for (final int pos : measurePositions) {
                canvas.drawLine(pos, 0, pos, getHeight(), measureBarPaint);
            }
        }
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

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int minw = getPaddingLeft() + getPaddingRight() + max(getSuggestedMinimumWidth(), audioWidth()) ;
        final int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        final int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        final int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        Log.d(LOG_TAG, String.format("calculated w=%d, h=%d", w, h));
        setMeasuredDimension(w, h);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        setBarAndMeasurePositions();
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final boolean superResult = super.onTouchEvent(event);
        // TODO: recognise which measure was touched
        return superResult;
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
        setBarAndMeasurePositions();
        requestLayout();
    }

    private void setBarAndMeasurePositions() {
        if (player == null) {
            barPositions = new int[0];
            measurePositions = new int[0];
        } else {
            final int measureCount = player.getDuration() * beatsPerMinute / MILLIS_IN_MINUTE;
            final int measureWidth = getWidth() / measureCount;
            measurePositions = new int[measureCount];
            barPositions = new int[measureCount / measuresPerBar + 1];
            int currPos = 0;
            for (int i = 0; i < measureCount; i++) {
                measurePositions[i] = currPos;
                if (i % measuresPerBar == 0) barPositions[i / measuresPerBar] = currPos;
                currPos += measureWidth;
            }
            Log.d(LOG_TAG, String.format("measure count: %d, width: %d", measureCount, measureWidth));
        }
    }

    public void setBeatsPerMinute(final int bpm) {
        beatsPerMinute = bpm;
        setBarAndMeasurePositions();
        postInvalidate();
    }

    public void setMeasuresPerBar(final int mpb) {
        measuresPerBar = mpb;
        setBarAndMeasurePositions();
        postInvalidate();
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

    private float getDisplayDpi() {
        if (isInEditMode()) return 1f;
        final DisplayMetrics metrics = new DisplayMetrics();
        ((android.view.WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    private static Paint paint(final int colour) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colour);
        paint.setStyle(Paint.Style.STROKE);
        return paint;
    }

    public class PlaybackTrackingTask extends TimerTask {
        @Override
        public void run() {
            // TODO: restricted invalidation for better performance
            postInvalidate();
        }
    }
}
