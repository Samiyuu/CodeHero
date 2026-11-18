package com.ccat.coderhero;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;

public final class Ui {
    private Ui() {}

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Press-squash effect + click sound. */
    public static void pressable(View v) { attach(v, true); }

    /** Press-squash effect without the click sound (used for quiz answers). */
    public static void pressableQuiet(View v) { attach(v, false); }

    private static void attach(View v, boolean sound) {
        v.setOnTouchListener((view, e) -> {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate().scaleX(0.94f).scaleY(0.94f).setDuration(60).start();
                    break;
                case MotionEvent.ACTION_UP:
                    if (sound) SoundFx.play(view.getContext(), SoundFx.Kind.CLICK);
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.animate().scaleX(1f).scaleY(1f).setDuration(80).start();
                    break;
            }
            return false;
        });
    }

    /** Fades a view in after a delay (used for the delayed reveal after the background). */
    public static void fadeIn(View v, long delayMs) {
        v.setAlpha(0f);
        ObjectAnimator a = ObjectAnimator.ofFloat(v, View.ALPHA, 0f, 1f);
        a.setStartDelay(delayMs);
        a.setDuration(400);
        a.start();
    }

    // ------------------------------------------------------------ runtime-built widgets
    public static LinearLayout.LayoutParams lp(Context c, int topMarginDp) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.topMargin = dp(c, topMarginDp);
        return p;
    }

    /** Light panel with left-aligned text. */
    public static BevelTextView card(Context c, CharSequence text) {
        BevelTextView t = new BevelTextView(c);
        t.setText(text);
        t.setTextColor(c.getColor(R.color.black));
        t.setTextSize(14);
        t.setGravity(Gravity.START);
        int p = dp(c, 16);
        t.setPadding(p, p, p, p);
        return t;
    }

    /** Dark code card with syntax colours. */
    public static BevelTextView codeCard(Context c, String code) {
        BevelTextView t = new BevelTextView(c);
        t.setColors(0xFF0E1433, 0xFF4FC3F7, 0xFF2D6FB0);
        t.setTypeface(ResourcesCompat.getFont(c, R.font.orbitron_font));
        t.setTextColor(c.getColor(R.color.code_default));
        t.setTextSize(14);
        t.setGravity(Gravity.START);
        int p = dp(c, 14);
        t.setPadding(p, p, p, p);
        t.setText(CodeHighlighter.format(c, code));
        return t;
    }

    /** Pixel-style button built in code. bg is a btn_* drawable. */
    public static TextView button(Context c, CharSequence text, int bgRes) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setBackgroundResource(bgRes);
        t.setTextColor(c.getColor(R.color.white));
        t.setTextSize(18);
        t.setTypeface(ResourcesCompat.getFont(c, R.font.orbitron_font), Typeface.BOLD);
        t.setGravity(Gravity.CENTER);
        t.setPadding(dp(c, 20), dp(c, 12), dp(c, 24), dp(c, 16));
        t.setClickable(true);
        t.setFocusable(true);
        pressable(t);
        return t;
    }

    /** Section heading chip for list screens. */
    public static TextView heading(Context c, CharSequence text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextColor(c.getColor(R.color.white));
        t.setTextSize(16);
        t.setTypeface(ResourcesCompat.getFont(c, R.font.orbitron_font), Typeface.BOLD);
        t.setPadding(dp(c, 4), dp(c, 6), dp(c, 4), dp(c, 2));
        return t;
    }
}
