package com.ccat.coderhero;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/** Starts the music when any screen is visible and pauses it when the app leaves the screen. */
public class CoderHeroApp extends Application {
    private int started = 0;

    @Override public void onCreate() {
        super.onCreate();
        SoundFx.init(this);
        if (Prefs.sound(this)) MusicManager.prepare(this);   // begin loading immediately

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override public void onActivityStarted(Activity a) {
                if (++started == 1) MusicManager.start(a);
            }
            @Override public void onActivityStopped(Activity a) {
                if (--started == 0) MusicManager.pause();
            }
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityResumed(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }
}