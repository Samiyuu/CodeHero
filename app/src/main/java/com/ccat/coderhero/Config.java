package com.ccat.coderhero;

import android.content.Context;

/** Thin readers for the tuning values in res/values/config.xml. Edit the XML, not this file. */
public final class Config {
    private Config() {}

    private static int i(Context c, int id) { return c.getResources().getInteger(id); }

    public static int splashMs(Context c)        { return i(c, R.integer.splash_ms); }
    public static int star3Percent(Context c)    { return i(c, R.integer.star3_min_percent); }
    public static int star2Percent(Context c)    { return i(c, R.integer.star2_min_percent); }
    public static int warnSeconds(Context c)     { return i(c, R.integer.warn_seconds); }
    public static int hintsPerRun(Context c)     { return i(c, R.integer.hints_per_run); }
    public static int endlessLives(Context c)    { return i(c, R.integer.endless_lives); }
    public static int endlessStartSec(Context c) { return i(c, R.integer.endless_start_seconds); }
    public static int endlessMinSec(Context c)   { return i(c, R.integer.endless_min_seconds); }
    public static int endlessSecStep(Context c)  { return Math.max(1, i(c, R.integer.endless_correct_per_second_less)); }
    public static int endlessTierStep(Context c) { return Math.max(1, i(c, R.integer.endless_correct_per_tier)); }
    public static int historySize(Context c)     { return i(c, R.integer.question_history_size); }
    public static int practiceCount(Context c)   { return i(c, R.integer.practice_question_count); }
    public static int dailyCount(Context c)      { return i(c, R.integer.daily_question_count); }
    public static int dailySeconds(Context c)    { return i(c, R.integer.daily_seconds); }
    public static int bossLevel(Context c)       { return i(c, R.integer.boss_level); }
    public static int leaderboardSize(Context c) { return i(c, R.integer.leaderboard_size); }
    public static int vibrateWrongMs(Context c)  { return i(c, R.integer.vibrate_wrong_ms); }
    public static int maxContentDp(Context c)    { return i(c, R.integer.max_content_width_dp); }

    public static float largeTextScale(Context c) {
        android.util.TypedValue tv = new android.util.TypedValue();
        c.getResources().getValue(R.dimen.large_text_scale, tv, true);
        return tv.getFloat();
    }
}
