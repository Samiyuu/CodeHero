package com.ccat.coderhero;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

/** A TextView drawn as a pixel-game panel: cut corners, dark outline and a light inner line. */
public class BevelTextView extends AppCompatTextView {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private int fill = 0xFFF7F5FC, outer = 0xFF2A3358, line = 0xFFA9B8E8;
    private float cut;
    private boolean stripes;

    public BevelTextView(Context c) { super(c); init(c, null); }
    public BevelTextView(Context c, AttributeSet a) { super(c, a); init(c, a); }
    public BevelTextView(Context c, AttributeSet a, int s) { super(c, a, s); init(c, a); }

    private void init(Context c, AttributeSet attrs) {
        cut = Ui.dp(c, 10);
        if (attrs != null) {
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.BevelTextView);
            fill = a.getColor(R.styleable.BevelTextView_bvFill, fill);
            outer = a.getColor(R.styleable.BevelTextView_bvOuter, outer);
            line = a.getColor(R.styleable.BevelTextView_bvLine, line);
            cut = a.getDimension(R.styleable.BevelTextView_bvCut, cut);
            stripes = a.getBoolean(R.styleable.BevelTextView_bvStripes, false);
            a.recycle();
        }
    }

    /** Change the panel colours from code (used by screens that build their cards at runtime). */
    public void setColors(int fill, int outer, int line) {
        this.fill = fill; this.outer = outer; this.line = line;
        invalidate();
    }

    private void poly(float l, float t, float r, float b, float k) {
        k = Math.max(0, Math.min(k, Math.min(r - l, b - t) / 2f));
        path.reset();
        path.moveTo(l + k, t);
        path.lineTo(r - k, t);
        path.lineTo(r, t + k);
        path.lineTo(r, b - k);
        path.lineTo(r - k, b);
        path.lineTo(l + k, b);
        path.lineTo(l, b - k);
        path.lineTo(l, t + k);
        path.close();
    }

    @Override protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float d = Ui.dp(getContext(), 3);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(outer);
        poly(0, 0, w, h, cut);
        canvas.drawPath(path, paint);

        paint.setColor(fill);
        poly(d, d, w - d, h - d, cut - d * 0.6f);
        canvas.drawPath(path, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Ui.dp(getContext(), 1) * 1.5f);
        paint.setColor(line);
        poly(d * 2, d * 2, w - d * 2, h - d * 2, cut - d * 1.4f);
        canvas.drawPath(path, paint);

        if (stripes) {
            paint.setStrokeWidth(Ui.dp(getContext(), 2));
            for (int i = 0; i < 3; i++) {
                float off = d * 3 + i * Ui.dp(getContext(), 7);
                canvas.drawLine(w - d * 3 - off - Ui.dp(getContext(), 12), h - d * 3,
                        w - d * 3 - off, h - d * 3 - Ui.dp(getContext(), 12), paint);
            }
        }
        super.onDraw(canvas);
    }
}
