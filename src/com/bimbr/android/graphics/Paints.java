package com.bimbr.android.graphics;

import android.graphics.Paint;

/**
 * Utility functions for working with {@link Paint} objects.
 *
 * @author mmakowski
 */
public final class Paints {
    public static Paint paint(final int colour, final Paint.Style style) {
        return paint().withFlags(Paint.ANTI_ALIAS_FLAG)
                      .withColour(colour)
                      .withStyle(style)
                      .build();
    }

    public static Paint textPaint(final int colour, final float size) {
        return paint().withFlags(Paint.ANTI_ALIAS_FLAG)
                      .withColour(colour)
                      .withStyle(Paint.Style.FILL)
                      .withTextAlign(Paint.Align.LEFT)
                      .withTextSize(size)
                      .build();
    }

    public static PaintBuilder paint() {
        return new PaintBuilder();
    }

    private Paints() {}

    public static final class PaintBuilder {
        private final Paint paint = new Paint();

        public PaintBuilder withFlags(final int flags) { paint.setFlags(flags); return this; }
        public PaintBuilder withColour(final int colour) { paint.setColor(colour); return this; }
        public PaintBuilder withStyle(final Paint.Style style) { paint.setStyle(style); return this; }
        public PaintBuilder withTextAlign(final Paint.Align align) { paint.setTextAlign(align); return this; }
        public PaintBuilder withTextSize(final float size) { paint.setTextSize(size); return this; }

        public Paint build() { return paint; }

        private PaintBuilder() {};
    }
}
