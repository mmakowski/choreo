package com.bimbr.choreo.model;

import java.util.ArrayList;
import java.util.List;


/**
 * A choreography.
 *
 * @author mmakowski
 */
public class Choreography {
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int MILLIS_IN_MINUTE = MILLIS_IN_SECOND * 60;

    private String           title;
    private int              musicDurationMs;
    private final int        beatsPerMinute   = 120;                  // FIXME: make configurable
    private final int        measuresPerBar   = 4;                    // FIXME: make configurable
    private int              measureCount;
    private List<Move>[]     moves;                                   // lists of moves for each measure

    public Choreography(final String title) {
        this.title = title;
    }

    public List<Move> movesInMeasure(final int measureIndex) {
        return moves[measureIndex];
    }

    public void addMove(final int measureIndex, final Move move) {
        moves[measureIndex].add(move);
        if (moveAddedListener != null) moveAddedListener.onMoveAdded(measureIndex, move);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public int getMeasureCount() {
        return measureCount;
    }

    public int getBeatsPerMinute() {
        return beatsPerMinute;
    }

    public int getMusicDurationMs() {
        return musicDurationMs;
    }

    @SuppressWarnings("unchecked")
    public void setMusicDurationMs(final int durationMs) {
        musicDurationMs = durationMs;
        measureCount    = musicDurationMs * beatsPerMinute / MILLIS_IN_MINUTE;
        moves           = new List[measureCount];
        for (int i = 0; i < measureCount; i++) moves[i] = new ArrayList<Move>(5);
    }

    public int getMeasuresPerBar() {
        return measuresPerBar ;
    }

    public Iterable<Move> getMovesAt(final int measureIndex) {
        return moves[measureIndex];
    }

    // -- events -------------------------

    private OnMoveAddedListener moveAddedListener;

    public void setOnMoveAddedListener(final OnMoveAddedListener listener) {
        this.moveAddedListener = listener;
    }

    public interface OnMoveAddedListener {
        /**
         * @param measureIndex index of measure to which move was added
         * @param move the move that was added
         */
        void onMoveAdded(int measureIndex, Move move);
    }
}
