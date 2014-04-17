package com.bimbr.choreo.model;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import org.junit.Ignore;
import org.junit.Test;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static org.junit.Assert.assertEquals;

public class ChoreographyTest {
    @Test
    public void settingTempoAndMeterGeneratesMeasureEvents() {
        final int tempoMpm = 120;
        final int meterMpb = 4;
        final int durationMs = 8000;
        final int millisInMin = 60 * 1000;
        final int measureTimeMs = millisInMin / tempoMpm;
        final ImmutableList<Integer> expectedMeasureTimes = equallySpacedTimes(durationMs, measureTimeMs);
        final Choreography choreo = new Choreography("test-choreo");
        choreo.setMusicDurationMs(durationMs);
        choreo.addTempoChangeMeasuresPerMinute(0, tempoMpm);
        choreo.addMeterChangeMeasuresPerBar(0, meterMpb);

        ImmutableList<Integer> actualMeasureTimes = ImmutableList.copyOf(transform(filter(choreo.getEvents(), instanceOf(Choreography.Measure.class)), toTimeMs));

        assertEquals(expectedMeasureTimes, actualMeasureTimes);
    }

    @Ignore("TODO")
    @Test
    public void moveAddedAtASpecifiedTimeIsReportedInTheAppropriateMeasure() {
        Choreography choreo = new Choreography("test-choreo");
        choreo.setMusicDurationMs(8000);
        choreo.addTempoChangeMeasuresPerMinute(0, 120);
        choreo.addMeterChangeMeasuresPerBar(0, 4);
        Move move = new Move("test-move");

        choreo.addMove(2000, move);

        // choreo.getEvents()
    }

    private static ImmutableList<Integer> equallySpacedTimes(int duration, int interval) {
        final ImmutableList.Builder<Integer> times = ImmutableList.builder();
        for (int t = 0; t < duration; t += interval) times.add(t);
        return times.build();
    }

    private static final Function<Choreography.Event, Integer> toTimeMs = new Function<Choreography.Event, Integer>() {
        public Integer apply(Choreography.Event event) {
            return event.getTimeMs();
        }
    };
}
