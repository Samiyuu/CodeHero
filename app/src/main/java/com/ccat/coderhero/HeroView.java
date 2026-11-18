package com.ccat.coderhero;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.Random;

/**
 * Draws Kai (the Coder Hero) from swappable images instead of a pixel grid.
 *
 * Default images are looked up by drawable name in res/drawable (png, webp, jpg or vector xml):
 *
 *   hero_masked_normal     hero_unmasked_normal      (required: these are the fallbacks)
 *   hero_masked_happy      hero_unmasked_happy       (optional)
 *   hero_masked_worried    hero_unmasked_worried     (optional)
 *   hero_masked_sad        hero_unmasked_sad         (optional)
 *
 * A missing image falls back to: same mask state + NORMAL, then masked + same mood, then masked + NORMAL.
 * You can also swap images at runtime with setImage(...) / setImageRes(...), e.g. for skins.
 *
 * Call setTalkative(true) to make the hero alive: it greets you, changes mood and says random
 * things every few seconds, and reacts when you tap it.
 */
public class HeroView extends View {
    /** Hero reactions: a different image plus a small bubble (check, cross or "!"). */
    public enum Mood { NORMAL, HAPPY, WORRIED, SAD }

    private static final int MOODS = Mood.values().length;
    // [0] = masked, [1] = unmasked; indexed by Mood.ordinal()
    private final Drawable[][] images = new Drawable[2][MOODS];

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean unmasked = false;
    private Mood mood = Mood.NORMAL;
    private int headroom = 0;   // extra empty rows above the hero so the bubble has space
    private ObjectAnimator idle;

    private static final int COLS = 20, ROWS = 30;   // virtual grid, used only to size the bubble/headroom

    // ---------------------------------------------------------------- talking / mood-swing state
    private static final String[] GREET_MORNING = {"Good Morning, My Coder!", "Rise and shine! Ready to Code?"};
    private static final String[] GREET_AFTERNOON = {"Good Afternoon! Let's fix some bugs.", "Hey there, Syntax Savior!"};
    private static final String[] GREET_EVENING = {"Good Evening! The Corruptor never sleeps.", "Late shift? Let's code!"};
    private static final String[] GREET_NIGHT = {"Still up? Me too. Let's Debug!", "Night owl mode: ON."};

    private static final String[] LINES_NORMAL = {
            "Don't forget the semicolon;",
            "Java is case-sensitive!",
            "Practice makes perfect syntax.",
            "Tip: read the code line by line.",
            "Did you check the Learn notes?",
            "Ready when you are!"};
    private static final String[] LINES_HAPPY = {
            "Yay, I love coding!", "Today is a good day to compile!", "No bugs today. I hope!", "You're doing great!"};
    private static final String[] LINES_WORRIED = {
            "Hmm... did I hear the Corruptor?", "Is that a NullPointerException?!", "I smell an infinite loop...", "Something feels off..."};
    private static final String[] LINES_SAD = {
            "I miss my old bugs...", "Compile error... again?", "Who stole my semicolon?", "Sigh... where's my coffee?"};
    private static final String[] LINES_TAP = {
            "Hey, that tickles!", "Boop!", "Ouch! Careful, I'm a hero!", "Ready to code?",
            "You tapped me!", "Hehe, again!", "Syntax first, fun later!"};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();
    private final TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private final Path tail = new Path();
    private final RectF rect = new RectF();

    private boolean talkative = false;
    private boolean greeted = false;
    private Mood flair = null;          // temporary mood shown on top of the real one
    private String speech = null;       // text in the speech bubble (null = hidden)

    private final Runnable ambient = new Runnable() {
        @Override public void run() {
            if (!talkative) return;
            randomChatter();
            scheduleAmbient();
        }
    };
    private final Runnable clearSpeech = () -> { speech = null; invalidate(); };
    private final Runnable clearFlair = () -> { flair = null; invalidate(); };

    public HeroView(Context c) { super(c); init(); }
    public HeroView(Context c, AttributeSet a) { super(c, a); init(); }

    private void init() {
        paint.setStyle(Paint.Style.FILL);
        textPaint.setColor(0xFF14214B);
        textPaint.setTextSize(Ui.dp(getContext(), 12) * getResources().getConfiguration().fontScale);
        textPaint.setFakeBoldText(true);
        loadDefaults();
    }

    // ---------------------------------------------------------------- image swapping

    private void loadDefaults() {
        String[] states = {"masked", "unmasked"};
        String[] moods = {"normal", "happy", "worried", "sad"};
        for (int s = 0; s < 2; s++) {
            for (int m = 0; m < MOODS; m++) {
                String name = "hero_" + states[s] + "_" + moods[m];
                int id = getResources().getIdentifier(name, "drawable", getContext().getPackageName());
                if (id != 0) images[s][m] = ContextCompat.getDrawable(getContext(), id);
            }
        }
    }

    /** Swap the image used for a given mask state and mood. Pass null to clear (falls back). */
    public void setImage(boolean unmaskedState, Mood m, Drawable d) {
        images[unmaskedState ? 1 : 0][m.ordinal()] = d;
        invalidate();
    }

    /** Same as setImage, from a drawable resource id. */
    public void setImageRes(boolean unmaskedState, Mood m, int resId) {
        setImage(unmaskedState, m, resId == 0 ? null : ContextCompat.getDrawable(getContext(), resId));
    }

    /** Use one image for every mood of a mask state (handy for a simple skin). */
    public void setImageForAllMoods(boolean unmaskedState, Drawable d) {
        for (int m = 0; m < MOODS; m++) images[unmaskedState ? 1 : 0][m] = d;
        invalidate();
    }

    /** The mood actually shown: a temporary flair (random mood swing / tap reaction) wins over the real mood. */
    private Mood shownMood() { return flair != null ? flair : mood; }

    private Drawable currentImage() {
        int s = unmasked ? 1 : 0, m = shownMood().ordinal();
        Drawable d = images[s][m];
        if (d == null) d = images[s][Mood.NORMAL.ordinal()];
        if (d == null) d = images[0][m];
        if (d == null) d = images[0][Mood.NORMAL.ordinal()];
        return d;
    }

    // ---------------------------------------------------------------- state (same API as before)

    public void setUnmasked(boolean value) { unmasked = value; invalidate(); }

    public void setMood(Mood m) { if (m != mood) { mood = m; invalidate(); } }

    /** Reserve empty rows above the hero (use on the game screen so the reaction bubble fits). */
    public void setHeadroom(int rows) { headroom = rows; invalidate(); }

    /** Little hop when the player answers correctly. */
    public void bounce() {
        animate().cancel();
        animate().translationY(-Ui.dp(getContext(), 14)).setDuration(110)
                .withEndAction(() -> animate().translationY(0).setDuration(140).start()).start();
    }

    /** Gentle up/down bobbing for menus. */
    public void startIdle() {
        if (idle != null) return;
        idle = ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, 0f, -Ui.dp(getContext(), 8));
        idle.setDuration(900);
        idle.setRepeatCount(ValueAnimator.INFINITE);
        idle.setRepeatMode(ValueAnimator.REVERSE);
        idle.start();
    }

    // ---------------------------------------------------------------- talking, mood swings, tapping

    /**
     * Makes the hero lively: greets you once, then every few seconds randomly changes mood and talks.
     * Tapping the hero makes it hop, change mood and say something.
     * Leave it off (default) on screens where the game controls the hero's mood.
     */
    public void setTalkative(boolean on) {
        if (talkative == on) return;
        talkative = on;
        if (on) {
            setOnClickListener(v -> onTapped());
            if (isAttachedToWindow()) startAmbient();
        } else {
            setOnClickListener(null);
            setClickable(false);
            stopAmbient();
            speech = null;
            flair = null;
            invalidate();
        }
    }

    /** Show a speech bubble for a moment, optionally with a temporary mood (pass null to keep the mood). */
    public void say(String text, Mood temporaryMood, long millis) {
        speech = text;
        handler.removeCallbacks(clearSpeech);
        handler.postDelayed(clearSpeech, millis);
        if (temporaryMood != null) {
            flair = temporaryMood == Mood.NORMAL ? null : temporaryMood;
            handler.removeCallbacks(clearFlair);
            handler.postDelayed(clearFlair, millis);
        }
        invalidate();
    }

    private void onTapped() {
        bounce();
        Mood m = rnd.nextInt(4) == 0 ? Mood.WORRIED : Mood.HAPPY;   // mostly happy, sometimes startled
        say(pick(LINES_TAP), m, 2200);
    }

    private void startAmbient() {
        handler.removeCallbacks(ambient);
        if (!greeted) {
            greeted = true;
            handler.postDelayed(() -> { if (talkative) say(greeting(), Mood.HAPPY, 3200); }, 700);
            handler.postDelayed(ambient, 6500);
        } else {
            scheduleAmbient();
        }
    }

    private void stopAmbient() {
        handler.removeCallbacksAndMessages(null);
    }

    private void scheduleAmbient() {
        handler.removeCallbacks(ambient);
        handler.postDelayed(ambient, 4500 + rnd.nextInt(5500));   // 4.5 to 10 seconds
    }

    /** Picks a random mood and a line that fits it. */
    private void randomChatter() {
        if (speech != null) return;   // don't interrupt a line that is still showing
        int r = rnd.nextInt(10);
        Mood m;
        String[] lines;
        if (r < 3)      { m = Mood.NORMAL;  lines = LINES_NORMAL; }
        else if (r < 6) { m = Mood.HAPPY;   lines = LINES_HAPPY; }
        else if (r < 8) { m = Mood.WORRIED; lines = LINES_WORRIED; }
        else            { m = Mood.SAD;     lines = LINES_SAD; }
        if (m == Mood.HAPPY) bounce();
        say(pick(lines), m, 3000);
    }

    private String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (h >= 5 && h < 12) return pick(GREET_MORNING);
        if (h >= 12 && h < 18) return pick(GREET_AFTERNOON);
        if (h >= 18 && h < 22) return pick(GREET_EVENING);
        return pick(GREET_NIGHT);
    }

    private String pick(String[] a) { return a[rnd.nextInt(a.length)]; }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (talkative) startAmbient();
    }

    @Override protected void onDetachedFromWindow() {
        if (idle != null) { idle.cancel(); idle = null; }
        stopAmbient();
        super.onDetachedFromWindow();
    }

    // ---------------------------------------------------------------- drawing

    @Override protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w == 0 || h == 0) return;

        Drawable d = currentImage();

        // Aspect ratio of the image (falls back to the old 20x25 grid shape).
        float aspect = (float) COLS / ROWS;
        if (d != null && d.getIntrinsicWidth() > 0 && d.getIntrinsicHeight() > 0) {
            aspect = d.getIntrinsicWidth() / (float) d.getIntrinsicHeight();
        }

        // Reserve headroom (in "rows" of the virtual grid) above the hero, fit the rest.
        float reserve = talkative ? Ui.dp(getContext(), 60) : 0;   // space kept free for the speech bubble
        float rowsTotal = ROWS + headroom;
        float heroH = Math.min((h - reserve) * ROWS / rowsTotal, w / aspect);
        float heroW = heroH * aspect;
        float left = (w - heroW) / 2f;
        float top = h - heroH;   // bottom-aligned, like before

        if (d != null) {
            d.setBounds(Math.round(left), Math.round(top),
                    Math.round(left + heroW), Math.round(top + heroH));
            d.draw(canvas);
        }

        if (shownMood() != Mood.NORMAL) {
            float cell = heroW / COLS;
            drawBubble(canvas, cell, left, top);
        }

        if (speech != null) drawSpeech(canvas, w, left + heroW / 2f, top);
    }

    /** White rounded speech bubble above the hero with a little tail pointing at the head. */
    private void drawSpeech(Canvas canvas, int w, float heroCenterX, float heroTop) {
        float margin = Ui.dp(getContext(), 4);
        float pad = Ui.dp(getContext(), 8);
        float tailH = Ui.dp(getContext(), 8);
        float bw = Math.min(w - margin * 2, Ui.dp(getContext(), 190));
        int textW = Math.max(1, Math.round(bw - pad * 2));

        @SuppressWarnings("deprecation")
        StaticLayout layout = new StaticLayout(speech, textPaint, textW,
                Layout.Alignment.ALIGN_CENTER, 1f, 0f, false);
        float bh = layout.getHeight() + pad * 2;

        float bx = Math.max(margin, Math.min(w - margin - bw, heroCenterX - bw / 2f));
        float by = Math.max(margin, heroTop - bh - tailH);

        rect.set(bx, by, bx + bw, by + bh);
        float r = Ui.dp(getContext(), 10);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF14214B);                       // outline
        rect.inset(-Ui.dp(getContext(), 2), -Ui.dp(getContext(), 2));
        canvas.drawRoundRect(rect, r + 2, r + 2, paint);
        rect.inset(Ui.dp(getContext(), 2), Ui.dp(getContext(), 2));
        paint.setColor(0xFFFFFFFF);
        canvas.drawRoundRect(rect, r, r, paint);

        float tx = Math.max(bx + r, Math.min(bx + bw - r, heroCenterX));
        float ty = by + bh;
        tail.reset();
        tail.moveTo(tx - tailH * 0.8f, ty - 1);
        tail.lineTo(tx + tailH * 0.8f, ty - 1);
        tail.lineTo(tx, ty + tailH);
        tail.close();
        paint.setColor(0xFFFFFFFF);
        canvas.drawPath(tail, paint);

        canvas.save();
        canvas.translate(bx + pad, by + pad);
        layout.draw(canvas);
        canvas.restore();
    }

    /** Round bubble at the top-right of the hero: green check, red cross or yellow "!". */
    private void drawBubble(Canvas canvas, float cell, float ox, float oy) {
        Mood shown = shownMood();
        float rad = Math.max(cell * 2.2f, Ui.dp(getContext(), 11));
        float cx = Math.min(getWidth() - rad - 2, ox + cell * 17f);
        float cy = Math.max(rad + 2, oy + cell * 1.5f);
        int fill = shown == Mood.HAPPY ? 0xFF2FBF4F : shown == Mood.SAD ? 0xFFD81F4A : 0xFFFDB515;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFFFFFFFF);
        canvas.drawCircle(cx, cy, rad + 2, paint);
        paint.setColor(fill);
        canvas.drawCircle(cx, cy, rad, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(rad * 0.28f);
        paint.setColor(shown == Mood.WORRIED ? 0xFF1A1205 : 0xFFFFFFFF);
        float u = rad * 0.45f;
        if (shown == Mood.HAPPY) {
            Path p = new Path();
            p.moveTo(cx - u, cy);
            p.lineTo(cx - u * 0.25f, cy + u * 0.8f);
            p.lineTo(cx + u, cy - u * 0.7f);
            canvas.drawPath(p, paint);
        } else if (shown == Mood.SAD) {
            canvas.drawLine(cx - u * 0.8f, cy - u * 0.8f, cx + u * 0.8f, cy + u * 0.8f, paint);
            canvas.drawLine(cx + u * 0.8f, cy - u * 0.8f, cx - u * 0.8f, cy + u * 0.8f, paint);
        } else {
            canvas.drawLine(cx, cy - u * 1.0f, cx, cy + u * 0.2f, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx, cy + u * 0.85f, rad * 0.15f, paint);
        }
        paint.setStyle(Paint.Style.FILL);
    }
}