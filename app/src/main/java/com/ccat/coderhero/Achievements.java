package com.ccat.coderhero;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Trophies are defined in assets/achievements.json (id, title, desc, type, param).
 * Supported types: levels_cleared, boss, best_streak, first_try, stars_any_3, endless_score,
 * daily_streak, typing_correct, total_correct.
 */
public final class Achievements {
    public static final class Def {
        public final String id, title, desc, type;
        public final int param;
        Def(String id, String title, String desc, String type, int param) {
            this.id = id; this.title = title; this.desc = desc; this.type = type; this.param = param;
        }
    }

    private static List<Def> cache;

    private Achievements() {}

    public static synchronized List<Def> all(Context c) {
        if (cache == null) {
            cache = new ArrayList<>();
            try {
                JSONArray a = new JSONArray(QuestionBank.readAsset(c.getApplicationContext(), "achievements.json"));
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    cache.add(new Def(o.getString("id"), o.getString("title"), o.optString("desc", ""),
                            o.getString("type"), o.optInt("param", 1)));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return cache;
    }

    private static boolean met(Context c, Def d) {
        switch (d.type) {
            case "levels_cleared": return Prefs.unlocked(c) - 1 >= d.param;
            case "boss":           return Prefs.unlocked(c) > d.param;
            case "best_streak":    return Prefs.bestStreak(c) >= d.param;
            case "first_try":      return Prefs.firstTry(c, d.param);
            case "stars_any_3":    return Prefs.levelsWithStars(c, 3) >= d.param;
            case "endless_score":  return Prefs.bestEndless(c) >= d.param;
            case "daily_streak":   return Prefs.bestDailyStreak(c) >= d.param;
            case "typing_correct": return Prefs.typingCorrect(c) >= d.param;
            case "total_correct":  return Prefs.totalCorrect(c) >= d.param;
            default: return false;
        }
    }

    /** Checks every trophy and returns the ones earned just now (and saves them). */
    public static List<Def> evaluate(Context c) {
        Set<String> have = Prefs.earned(c);
        List<Def> fresh = new ArrayList<>();
        for (Def d : all(c)) {
            if (!have.contains(d.id) && met(c, d)) {
                Prefs.addEarned(c, d.id);
                fresh.add(d);
            }
        }
        return fresh;
    }
}
