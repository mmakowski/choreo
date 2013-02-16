package com.bimbr.choreo.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * A view that displays the choreography chart.
 * 
 * @author mmakowski
 */
public class ChoreographyView extends View {
    private final Paint barPaint;

    public ChoreographyView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        barPaint = newBarPaint();
    }

    private Paint newBarPaint() {
        final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xff202020);
        paint.setStyle(Paint.Style.FILL);
        return paint;
    }

    @Override
    public void onDraw(final Canvas canvas) {
        canvas.drawLine(0, 0, 2000, 500, barPaint);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        // TODO: proper sizing
        final int minw = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth() + 2000;
        final int w = resolveSizeAndState(minw, widthMeasureSpec, 1);
        final int minh = getPaddingBottom() + getPaddingTop() + 500;
        final int h = resolveSizeAndState(minh, heightMeasureSpec, 0);
        setMeasuredDimension(w, h);
    }
}
