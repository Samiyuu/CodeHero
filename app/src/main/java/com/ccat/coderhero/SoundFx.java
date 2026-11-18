package com.ccat.coderhero;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

/** Plays custom sound effects from res/raw using SoundPool. */
public final class SoundFx {
    public enum Kind { CLICK, CORRECT, WRONG, WIN, LOSE }

    private static SoundPool pool;
    private static final int[] ids = new int[Kind.values().length];
    private static final boolean[] ready = new boolean[Kind.values().length];

    private SoundFx() {}

    /** Loads the sounds once. Safe to call many times. */
    public static synchronized void init(Context c) {
        if (pool != null) return;
        Context app = c.getApplicationContext();
        pool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .build();
        pool.setOnLoadCompleteListener((p, sampleId, status) -> {
            for (int i = 0; i < ids.length; i++) if (ids[i] == sampleId) ready[i] = status == 0;
        });
        ids[Kind.CLICK.ordinal()]   = pool.load(app, R.raw.click_sfx, 1);
        ids[Kind.CORRECT.ordinal()] = pool.load(app, R.raw.correct_answer_sfx, 1);
        ids[Kind.WRONG.ordinal()]   = pool.load(app, R.raw.wrong_answer_sfx, 1);
        ids[Kind.WIN.ordinal()]     = pool.load(app, R.raw.winning_sfx, 1);
        ids[Kind.LOSE.ordinal()]    = pool.load(app, R.raw.lose_sfx, 1);
    }

    public static void play(Context c, Kind kind) {
        if (!Prefs.sound(c)) return;
        init(c);
        int i = kind.ordinal();
        if (ready[i]) pool.play(ids[i], 1f, 1f, 1, 0, 1f);
    }
}
