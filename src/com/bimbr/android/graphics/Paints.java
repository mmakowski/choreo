package com.bimbr.android.graphics;

import android.graphics.Paint;

/**
 * Utility functions for working with {@link Paint} objects.
 * 
 * @author mmakowski
 */
public final class Paints {
    public static Paint paint(final int colour, final Paint.Style style) {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(colour);
        paint.setStyle(style);
        return paint;
    }

    private Paints() {}
}
