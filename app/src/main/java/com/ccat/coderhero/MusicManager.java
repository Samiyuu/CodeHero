package com.ccat.coderhero;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

/** One shared background-music player. Call prepare() at app launch so start() is instant. */
public final class MusicManager {
    private static MediaPlayer player;
    private static boolean prepared = false;
    private static boolean wantPlaying = false;   // an activity is visible and sound is on

    private MusicManager() {}

    /** Loads the track in the background. Safe to call many times. */
    public static synchronized void prepare(Context c) {
        if (player != null) return;
        Context app = c.getApplicationContext();
        try (AssetFileDescriptor afd = app.getResources().openRawResourceFd(R.raw.bg_superhero)) {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mp.setLooping(true);
            mp.setVolume(1f, 1f);
            mp.setOnPreparedListener(p -> {
                synchronized (MusicManager.class) {
                    prepared = true;
                    if (wantPlaying && !p.isPlaying()) p.start();   // starts the instant it is ready
                }
            });
            player = mp;
            mp.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
            player = null;
        }
    }

    public static synchronized void start(Context c) {
        if (!Prefs.sound(c)) return;
        wantPlaying = true;
        prepare(c);
        if (player != null && prepared && !player.isPlaying()) player.start();
    }

    public static synchronized void pause() {
        wantPlaying = false;
        if (player != null && prepared && player.isPlaying()) player.pause();
    }
}