package com.bimbr.choreo.model;

import static java.util.Arrays.copyOf;

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
    private String           musicPath;
    private int              musicDurationMs;
    private final int        beatsPerMinute   = 120;            // FIXME: make configurable
    private final int        measuresPerBar   = 4;              // FIXME: make configurable
    private int              measureCount;
    @SuppressWarnings("unchecked")
    private List<Move>[]     moves            = new List[0];    // lists of moves for each measure

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

    public String getMusicPath() {
        return musicPath;
    }

    public void setMusicPath(final String musicPath) {
        this.musicPath = musicPath;
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

    public void setMusicDurationMs(final int durationMs) {
        musicDurationMs = durationMs;
        measureCount    = musicDurationMs * beatsPerMinute / MILLIS_IN_MINUTE;
        final int oldLength = moves.length;
        moves           = copyOf(moves, measureCount);
        for (int i = oldLength; i < measureCount; i++) moves[i] = new ArrayList<Move>(5);
    }

    public int getMeasuresPerBar() {
        return measuresPerBar ;
    }

    public Iterable<Move> getMovesAt(final int measureIndex) {
        return moves[measureIndex];
    }

    public void addTempoChangeMeasuresPerMinute(int timeMs, int measuresPerMinute) {
        throw new IllegalStateException("TODO");
    }

    public void addMeterChangeMeasuresPerBar(int timeMs, int measuresPerBar) {
        throw new IllegalStateException("TODO");
    }

    // -- hooks -------------------------

    private transient OnMoveAddedListener moveAddedListener;

    public void setOnMoveAddedListener(final OnMoveAddedListener listener) {
        this.moveAddedListener = listener;
    }

    public Iterable<Event> getEvents() {
        throw new IllegalStateException("TODO");
    }

    public interface OnMoveAddedListener {
        /**
         * @param measureIndex index of measure to which move was added
         * @param move the move that was added
         */
        void onMoveAdded(int measureIndex, Move move);
    }

    // -- events -------------------------

    public interface Event {
        int getTimeMs();
    }

    public static final class Measure implements Event {
        private final int timeMs;

        private Measure(int timeMs) {
            this.timeMs = timeMs;
        }

        public int getTimeMs() {
            return timeMs;
        }
    }
}
