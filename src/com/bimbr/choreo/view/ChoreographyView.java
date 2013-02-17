package com.bimbr.choreo.view;

import static android.content.Context.WINDOW_SERVICE;
import static android.graphics.Paint.Style.FILL;
import static android.graphics.Paint.Style.STROKE;
import static com.bimbr.android.graphics.Paints.paint;
import static com.bimbr.android.graphics.Paints.textPaint;
import static java.lang.Math.max;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.bimbr.android.media.NotifyingMediaPlayer;
import com.bimbr.android.media.NotifyingMediaPlayer.OnPausedListener;
import com.bimbr.android.media.NotifyingMediaPlayer.OnStartedListener;
import com.bimbr.choreo.model.Choreography;
import com.bimbr.choreo.model.Choreography.OnMoveAddedListener;
import com.bimbr.choreo.model.Move;

/**
 * A view that displays the choreography chart.
 *
 * @author mmakowski
 */
public class ChoreographyView extends View {
    private static final double  SECOND_WIDTH_CM           = 1.0;
    // TODO: configure all sizes in CM for consistent scaling
    private static final int     MEASURE_PADDING_PX        = 5;
    private static final int     MOVE_LINE_HEIGHT_PX       = 60;
    private static final int     MOVE_SYMBOL_SIZE_PX       = 30;

    private static final int     PLAYBACK_TRACKING_FREQ_MS = 25;

    private static final int     NO_MEASURE                = -1;

    private static final String LOG_TAG          = "ChoreoView";

    private static final int    DIP              = 160;
    private static final double INCHES_PER_CM    = 0.393700787;
    private static final int    MILLIS_IN_SECOND = 1000;
    private static final double DIP_PER_CM       = DIP * INCHES_PER_CM;

    // mutable fields

    private NotifyingMediaPlayer  player;

    private int[]                 barPositions;
    private int[]                 measurePositions;

    private int                   selectedMeasure           = NO_MEASURE;

    private TimerTask             playbackTrackingTask;

    public ChoreographyView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    // -- drawing ------------------------

    private static final Paint barBarPaint           = paint(0xff202020, STROKE);
    private static final Paint playBarPaint          = paint(0xff33B5E5, STROKE);
    private static final Paint measureBarPaint       = paint(0xffa0a0a0, STROKE);
    private static final Paint measureSelectionPaint = paint(0xffe0e0e0, FILL);
    private static final Paint moveSymbolPaint       = textPaint(0xff000000, MOVE_SYMBOL_SIZE_PX);

    @Override
    public void onDraw(final Canvas canvas) {
        drawMeasureSelection(canvas);
        drawMeasureBars(canvas);
        drawBarBars(canvas);
        drawMoveSymbols(canvas);
        drawPlayBar(canvas);
    }

    private void drawMeasureSelection(final Canvas canvas) {
        if (selectedMeasure != NO_MEASURE) {
            canvas.drawRect(measurePositions[selectedMeasure], 0, nextMeasurePosition(selectedMeasure), getHeight(), measureSelectionPaint);
        }
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

    private void drawMoveSymbols(final Canvas canvas) {
        if (choreography != null) {
            for (int i = 0; i < choreography.getMeasureCount(); i++) {
                final int x = measurePositions[i] + MEASURE_PADDING_PX;
                int y = MOVE_LINE_HEIGHT_PX + MEASURE_PADDING_PX;
                for (final Move move : choreography.getMovesAt(i)) {
                    canvas.drawText(move.getSymbol(), x, y, moveSymbolPaint);
                    y += MOVE_LINE_HEIGHT_PX;
                }
            }
        }
    }

    private void drawPlayBar(final Canvas canvas) {
        if (player != null) {
            final int playBarPos = playBarPosition();
            canvas.drawLine(playBarPos, 0, playBarPos, getHeight(), playBarPaint);
        }
    }

    private void redrawMeasure(final int measureIndex) {
        postInvalidate(measurePositions[measureIndex],    0,
                       nextMeasurePosition(measureIndex), getHeight());
    }

    // -- reacting to user input --------

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        super.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        // need to return true, otherwise we will not be notified of subsequent events of the gesture;
        // see http://stackoverflow.com/questions/12588263/in-ontouchevent-action-up-doesnt-work
        return true;
    }

    private final GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureHandler());

    private final class GestureHandler extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(final MotionEvent e) {
            if (measurePositions.length > 0) {
                final int tappedMeasure = indexOfMeasureAt(e.getX());
                if (selectedMeasure != NO_MEASURE) {
                    if (tappedMeasure == selectedMeasure) addMove();
                    else changeMeasureSelectionTo(tappedMeasure);
                } else {
                    selectMeasure(tappedMeasure);
                }
            }
            return true;
        }

    }

    private void changeMeasureSelectionTo(final int tappedMeasure) {
        deselectMeasure();
        selectMeasure(tappedMeasure);
    }

    private void addMove() {
        if (addMoveListener != null) addMoveListener.onAddMove(selectedMeasure);
    }

    private void deselectMeasure() {
        final int lastSelected = selectedMeasure;
        selectedMeasure = NO_MEASURE;
        redrawMeasure(lastSelected);
    }

    private void selectMeasure(final int measureIndex) {
        selectedMeasure = measureIndex;
        redrawMeasure(selectedMeasure);
    }

    // -- reacting to model changes -----

    public void onMoveAdded(final int measureIndex) {
        redrawMeasure(measureIndex);
    }

    // -- state modification ------------

    private Choreography choreography;

    public void setChoreography(final Choreography choreography) {
        this.choreography = choreography;
        choreography.setOnMoveAddedListener(new OnMoveAddedListener() {
            @Override
            public void onMoveAdded(final int measureIndex, final Move move) {
                redrawMeasure(measureIndex);
            }
        });
        setBarAndMeasurePositions();
        requestLayout();
    }

    // -- playback tracking -------------

    // TODO: make this a part of NotifyingMediaPlayer and don't even keep a reference to media player in this view

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
    }

    private final Timer timer = new Timer(true);

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

    private final class PlaybackTrackingTask extends TimerTask {
        @Override
        public void run() {
            // TODO: restricted invalidation for better performance
            postInvalidate();
        }
    }

    // -- postion calculation -----------

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        setBarAndMeasurePositions();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        final int minw = getPaddingLeft() + getPaddingRight() + max(getSuggestedMinimumWidth(), audioWidth()) ;
        final int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        final int minh = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        final int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        setMeasuredDimension(w, h);
    }

    private final float displayDpi = getDisplayDpi();

    private int playBarPosition() {
        assert player != null;
        return msToX(player.getCurrentPosition());
    }

    private void setBarAndMeasurePositions() {
        if (player == null) {
            barPositions = new int[0];
            measurePositions = new int[0];
        } else {
            final int measureCount = choreography.getMeasureCount();
            final int measuresPerBar = choreography.getMeasuresPerBar();
            final int measureWidth = measureWidth();
            measurePositions = new int[measureCount];
            barPositions = new int[measureCount / measuresPerBar + 1];
            int currPos = 0;
            for (int i = 0; i < measureCount; i++) {
                measurePositions[i] = currPos;
                if (i % measuresPerBar == 0) barPositions[i / measuresPerBar] = currPos;
                currPos += measureWidth;
            }
            Log.d(LOG_TAG, String.format("positions set; measure count: %d, width: %d", measureCount, measureWidth));
        }
    }

    private int measureWidth() {
        return getWidth() / choreography.getMeasureCount();
    }

    private int nextMeasurePosition(final int measureIndex) {
        return measureIndex == measurePositions.length ? getWidth() : measurePositions[measureIndex + 1];
    }

    private int indexOfMeasureAt(final float x) {
        return (int) (x / measureWidth());
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

    private float getDisplayDpi() {
        if (isInEditMode()) return 1f;
        final DisplayMetrics metrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }

    // -- events --------------------------------

    private OnAddMoveListener addMoveListener;

    public void setOnAddMoveListener(final OnAddMoveListener addMoveListener) {
        this.addMoveListener = addMoveListener;
    }

    public interface OnAddMoveListener {
        /**
         * @param measureIndex index of measure to which move is to be added
         */
        void onAddMove(int measureIndex);
    }
}
