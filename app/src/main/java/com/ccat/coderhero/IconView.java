package com.ccat.coderhero;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

/** Small pixel-style icons drawn in code (cross, heart, warning, heartbeat, pause). */
public class IconView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final RectF rf = new RectF();
    private int type = 0;
    private float ox, oy, s;

    public IconView(Context c) { super(c); }
    public IconView(Context c, AttributeSet a) {
        super(c, a);
        TypedArray t = c.obtainStyledAttributes(a, R.styleable.IconView);
        type = t.getInt(R.styleable.IconView_iconType, 0);
        t.recycle();
    }

    private float x(float f) { return ox + f * s; }
    private float y(float f) { return oy + f * s; }

    private void rect(Canvas c, float l, float t, float r, float b, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        c.drawRect(x(l), y(t), x(r), y(b), p);
    }

    private void heart(Canvas c, float sc, int color) {
        p.setStyle(Paint.Style.FILL);
        p.setColor(color);
        float cy = 0.5f + (0.32f - 0.5f) * sc;
        float r = 0.22f * sc;
        c.drawCircle(x(0.5f - 0.2f * sc), y(cy), r * s, p);
        c.drawCircle(x(0.5f + 0.2f * sc), y(cy), r * s, p);
        float ty = 0.5f + (0.40f - 0.5f) * sc;
        path.reset();
        path.moveTo(x(0.5f - 0.42f * sc), y(ty));
        path.lineTo(x(0.5f + 0.42f * sc), y(ty));
        path.lineTo(x(0.5f), y(0.5f + 0.42f * sc));
        path.close();
        c.drawPath(path, p);
    }

    @Override protected void onDraw(Canvas c) {
        s = Math.min(getWidth(), getHeight());
        ox = (getWidth() - s) / 2f;
        oy = (getHeight() - s) / 2f;
        float line = Math.max(2f, s * 0.07f);
        switch (type) {
            case 0: // medical cross
                rect(c, 0.22f, 0.02f, 0.78f, 0.98f, 0xFF3A4A85);
                rect(c, 0.02f, 0.22f, 0.98f, 0.78f, 0xFF3A4A85);
                rect(c, 0.30f, 0.10f, 0.70f, 0.90f, 0xFF6F82C4);
                rect(c, 0.10f, 0.30f, 0.90f, 0.70f, 0xFF6F82C4);
                break;
            case 1: // heart
                heart(c, 1.12f, 0xFF7A0F26);
                heart(c, 1.0f, 0xFFEF2F4F);
                rect(c, 0.2f, 0.2f, 0.32f, 0.3f, 0xFFFFB3C0);
                break;
            case 2: // warning triangle
                path.reset();
                path.moveTo(x(0.5f), y(0.06f));
                path.lineTo(x(0.96f), y(0.92f));
                path.lineTo(x(0.04f), y(0.92f));
                path.close();
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFFFD6E0);
                c.drawPath(path, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(line);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(0xFF5A0020);
                c.drawPath(path, p);
                rect(c, 0.45f, 0.36f, 0.55f, 0.64f, 0xFF5A0020);
                rect(c, 0.45f, 0.70f, 0.55f, 0.80f, 0xFF5A0020);
                break;
            case 3: // heartbeat line
                path.reset();
                path.moveTo(x(0f), y(0.5f));
                path.lineTo(x(0.3f), y(0.5f));
                path.lineTo(x(0.4f), y(0.15f));
                path.lineTo(x(0.56f), y(0.9f));
                path.lineTo(x(0.66f), y(0.5f));
                path.lineTo(x(1f), y(0.5f));
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(line);
                p.setStrokeCap(Paint.Cap.ROUND);
                p.setStrokeJoin(Paint.Join.ROUND);
                p.setColor(0xFF1B2447);
                c.drawPath(path, p);
                break;
            default: // pause button
                rf.set(x(0.04f), y(0.04f), x(0.96f), y(0.96f));
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFF14214B);
                c.drawRoundRect(rf, s * 0.2f, s * 0.2f, p);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(line * 0.8f);
                p.setColor(0xFF6C8CFF);
                c.drawRoundRect(rf, s * 0.2f, s * 0.2f, p);
                rect(c, 0.30f, 0.26f, 0.44f, 0.74f, 0xFFFFFFFF);
                rect(c, 0.56f, 0.26f, 0.70f, 0.74f, 0xFFFFFFFF);
                break;
        }
    }
}
