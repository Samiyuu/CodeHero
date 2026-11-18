package com.ccat.coderhero;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Local top-N Endless scores, saved on the device (size is set in config.xml). */
public final class Leaderboard {
    public static final class Entry {
        public final int score;
        public final long time;
        Entry(int score, long time) { this.score = score; this.time = time; }
        public String dateText() { return DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(time)); }
    }

    private Leaderboard() {}

    public static List<Entry> load(Context c) {
        List<Entry> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(Prefs.getString(c, "leaderboard", "[]"));
            for (int i = 0; i < a.length(); i++) {
                JSONObject o = a.getJSONObject(i);
                out.add(new Entry(o.getInt("s"), o.getLong("t")));
            }
        } catch (Exception ignored) { }
        return out;
    }

    /** Adds a score. Returns its rank (1 = best) or 0 if it did not make the board. */
    public static int add(Context c, int score) {
        if (score <= 0) return 0;
        List<Entry> list = load(c);
        int pos = 0;
        while (pos < list.size() && list.get(pos).score >= score) pos++;
        int max = Config.leaderboardSize(c);
        if (pos >= max) return 0;
        list.add(pos, new Entry(score, System.currentTimeMillis()));
        while (list.size() > max) list.remove(list.size() - 1);
        JSONArray a = new JSONArray();
        try {
            for (Entry e : list) a.put(new JSONObject().put("s", e.score).put("t", e.time));
        } catch (Exception ignored) { }
        Prefs.putString(c, "leaderboard", a.toString());
        return pos + 1;
    }
}
