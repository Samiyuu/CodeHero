package com.ccat.coderhero;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Tiny wrapper around SharedPreferences (all data stays on-device). */
public final class Prefs {
    private Prefs() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("coder_hero", Context.MODE_PRIVATE);
    }

    // ------------------------------------------------------------ generic
    public static String getString(Context c, String key, String def) { return sp(c).getString(key, def); }
    public static void putString(Context c, String key, String v) { sp(c).edit().putString(key, v).apply(); }
    private static int geti(Context c, String k) { return sp(c).getInt(k, 0); }
    private static void puti(Context c, String k, int v) { sp(c).edit().putInt(k, v).apply(); }

    // ------------------------------------------------------------ progress
    public static int unlocked(Context c) { return sp(c).getInt("unlocked", 1); }
    public static void unlock(Context c, int level) {
        if (level > unlocked(c)) puti(c, "unlocked", level);
    }

    public static int bestEndless(Context c) { return geti(c, "best_endless"); }
    public static void setBestEndless(Context c, int v) { puti(c, "best_endless", v); }

    public static boolean prologueSeen(Context c) { return sp(c).getBoolean("prologue_seen", false); }
    public static void setPrologueSeen(Context c, boolean v) { sp(c).edit().putBoolean("prologue_seen", v).apply(); }

    // ------------------------------------------------------------ stars
    public static int stars(Context c, int level) { return geti(c, "stars_" + level); }
    public static void setStars(Context c, int level, int stars) {
        if (stars > stars(c, level)) puti(c, "stars_" + level, stars);
    }
    public static int totalStars(Context c) {
        int t = 0;
        for (Level l : Level.ALL) t += stars(c, l.id);
        return t;
    }
    public static int levelsWithStars(Context c, int stars) {
        int n = 0;
        for (Level l : Level.ALL) if (stars(c, l.id) >= stars) n++;
        return n;
    }

    // ------------------------------------------------------------ attempts / first try
    public static void addAttempt(Context c, int level) { puti(c, "attempts_" + level, geti(c, "attempts_" + level) + 1); }
    public static int attempts(Context c, int level) { return geti(c, "attempts_" + level); }
    public static void clearAttempts(Context c, int level) { puti(c, "attempts_" + level, 0); }
    public static boolean firstTry(Context c, int level) { return sp(c).getBoolean("first_try_" + level, false); }
    public static void setFirstTry(Context c, int level) { sp(c).edit().putBoolean("first_try_" + level, true).apply(); }

    // ------------------------------------------------------------ answer statistics
    public static void recordAnswer(Context c, int level, boolean ok, boolean typing) {
        SharedPreferences.Editor e = sp(c).edit();
        SharedPreferences s = sp(c);
        e.putInt("ans_t_" + level, s.getInt("ans_t_" + level, 0) + 1);
        e.putInt("ans_total", s.getInt("ans_total", 0) + 1);
        if (ok) {
            e.putInt("ans_c_" + level, s.getInt("ans_c_" + level, 0) + 1);
            e.putInt("ans_correct", s.getInt("ans_correct", 0) + 1);
            if (typing) e.putInt("typing_correct", s.getInt("typing_correct", 0) + 1);
        }
        e.apply();
    }
    public static int topicTotal(Context c, int level) { return geti(c, "ans_t_" + level); }
    public static int topicCorrect(Context c, int level) { return geti(c, "ans_c_" + level); }
    public static int totalAnswers(Context c) { return geti(c, "ans_total"); }
    public static int totalCorrect(Context c) { return geti(c, "ans_correct"); }
    public static int typingCorrect(Context c) { return geti(c, "typing_correct"); }

    public static int bestStreak(Context c) { return geti(c, "best_streak"); }
    public static void noteStreak(Context c, int streak) { if (streak > bestStreak(c)) puti(c, "best_streak", streak); }

    public static int gamesPlayed(Context c) { return geti(c, "games_played"); }

    // ------------------------------------------------------------ dates / streaks
    private static String fmt(Calendar cal) { return new SimpleDateFormat("yyyyMMdd", Locale.US).format(cal.getTime()); }
    public static String today() { return fmt(Calendar.getInstance()); }
    private static String yesterday() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return fmt(cal);
    }

    private static int bump(Context c, String dateKey, String streakKey, String bestKey) {
        SharedPreferences s = sp(c);
        String t = today(), last = s.getString(dateKey, "");
        int streak = s.getInt(streakKey, 0);
        if (!last.equals(t)) streak = last.equals(yesterday()) ? streak + 1 : 1;
        SharedPreferences.Editor e = s.edit();
        e.putString(dateKey, t).putInt(streakKey, streak);
        if (streak > s.getInt(bestKey, 0)) e.putInt(bestKey, streak);
        e.apply();
        return streak;
    }
    private static int current(Context c, String dateKey, String streakKey) {
        String last = sp(c).getString(dateKey, "");
        if (last.equals(today()) || last.equals(yesterday())) return sp(c).getInt(streakKey, 0);
        return 0;
    }

    /** Call whenever a game starts: keeps the "days played in a row" streak. */
    public static void recordPlay(Context c) {
        puti(c, "games_played", gamesPlayed(c) + 1);
        bump(c, "play_date", "play_streak", "play_best");
    }
    public static int playStreak(Context c) { return current(c, "play_date", "play_streak"); }
    public static int bestPlayStreak(Context c) { return geti(c, "play_best"); }

    // daily challenge
    public static boolean dailyDoneToday(Context c) { return today().equals(sp(c).getString("daily_date", "")); }
    public static int dailyScoreToday(Context c) { return dailyDoneToday(c) ? geti(c, "daily_score") : 0; }
    public static int dailyStreak(Context c) { return current(c, "daily_date", "daily_streak"); }
    public static int bestDailyStreak(Context c) { return geti(c, "daily_best"); }
    /** Marks today's daily as finished (first completion of the day sets the score and streak). */
    public static void completeDaily(Context c, int score) {
        if (dailyDoneToday(c)) return;
        puti(c, "daily_score", score);
        bump(c, "daily_date", "daily_streak", "daily_best");
    }

    // ------------------------------------------------------------ Endless question history
    public static List<Integer> recentIds(Context c) {
        List<Integer> out = new ArrayList<>();
        String s = sp(c).getString("recent_ids", "");
        if (s.isEmpty()) return out;
        for (String p : s.split(",")) { try { out.add(Integer.parseInt(p)); } catch (NumberFormatException ignored) { } }
        return out;
    }
    public static void pushRecent(Context c, int id, int max) {
        List<Integer> l = recentIds(c);
        l.remove(Integer.valueOf(id));
        l.add(id);
        while (l.size() > Math.max(1, max)) l.remove(0);
        StringBuilder sb = new StringBuilder();
        for (int v : l) { if (sb.length() > 0) sb.append(','); sb.append(v); }
        putString(c, "recent_ids", sb.toString());
    }
    public static void clearRecent(Context c) { putString(c, "recent_ids", ""); }

    // ------------------------------------------------------------ trophies
    public static Set<String> earned(Context c) {
        Set<String> set = new LinkedHashSet<>();
        String s = sp(c).getString("earned", "");
        if (!s.isEmpty()) for (String p : s.split(",")) set.add(p);
        return set;
    }
    public static void addEarned(Context c, String id) {
        Set<String> set = earned(c);
        set.add(id);
        putString(c, "earned", android.text.TextUtils.join(",", set));
    }

    // ------------------------------------------------------------ settings
    public static boolean sound(Context c) { return sp(c).getBoolean("sound", true); }
    public static void setSound(Context c, boolean on) { sp(c).edit().putBoolean("sound", on).apply(); }
    public static boolean vibrate(Context c) { return sp(c).getBoolean("vibrate", true); }
    public static void setVibrate(Context c, boolean on) { sp(c).edit().putBoolean("vibrate", on).apply(); }
    public static boolean largeText(Context c) { return sp(c).getBoolean("large_text", false); }
    public static void setLargeText(Context c, boolean on) { sp(c).edit().putBoolean("large_text", on).apply(); }
    public static boolean colorBlind(Context c) { return sp(c).getBoolean("color_blind", false); }
    public static void setColorBlind(Context c, boolean on) { sp(c).edit().putBoolean("color_blind", on).apply(); }

    /** Clears progress, stats, trophies and leaderboard; keeps the settings. */
    public static void resetProgress(Context c) {
        boolean snd = sound(c), vib = vibrate(c), big = largeText(c), cb = colorBlind(c);
        sp(c).edit().clear().putBoolean("sound", snd).putBoolean("vibrate", vib)
                .putBoolean("large_text", big).putBoolean("color_blind", cb).apply();
    }
}
