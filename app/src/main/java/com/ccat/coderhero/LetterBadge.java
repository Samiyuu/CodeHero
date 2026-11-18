package com.ccat.coderhero;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/** Navy circle with a white letter (A, B, C, D) shown at the left of each answer. */
public class LetterBadge extends Drawable {
    private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String letter;
    private final int size;

    public LetterBadge(Context c, char letter) {
        this.letter = String.valueOf(letter);
        this.size = Ui.dp(c, 30);
        circle.setColor(0xFF14214B);
        text.setColor(0xFFFFFFFF);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);
        text.setTextSize(size * 0.52f);
    }

    @Override public int getIntrinsicWidth() { return size; }
    @Override public int getIntrinsicHeight() { return size; }

    @Override public void draw(Canvas canvas) {
        float cx = getBounds().exactCenterX();
        float cy = getBounds().exactCenterY();
        canvas.drawCircle(cx, cy, size / 2f, circle);
        canvas.drawText(letter, cx, cy - (text.descent() + text.ascent()) / 2f, text);
    }

    @Override public void setAlpha(int alpha) { circle.setAlpha(alpha); text.setAlpha(alpha); }
    @Override public void setColorFilter(ColorFilter cf) { circle.setColorFilter(cf); text.setColorFilter(cf); }
    @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
}
