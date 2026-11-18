package com.ccat.coderhero;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Offline knowledge base. Questions live in assets/questions.json.
 *
 * Each entry: level, difficulty ("easy" | "medium" | "hard"), type ("choice" | "typing"),
 * code (optional), prompt, explain, and either
 *   options: [...]  (FIRST option is the correct one; the game shuffles), or
 *   answers: [...]  (typing questions: every accepted spelling).
 */
public final class QuestionBank {
    private static List<Question> cache;

    private QuestionBank() {}

    public static synchronized List<Question> all(Context c) {
        if (cache == null) cache = load(c.getApplicationContext());
        return cache;
    }

    public static List<Question> forLevel(Context c, int level) {
        List<Question> out = new ArrayList<>();
        for (Question q : all(c)) if (q.level == level) out.add(q);
        return out;
    }

    public static Question byId(Context c, int id) {
        for (Question q : all(c)) if (q.id == id) return q;
        return null;
    }

    /** Reads a whole asset file as UTF-8 text ("" if missing). */
    public static String readAsset(Context c, String name) {
        try (InputStream in = c.getAssets().open(name)) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            return bo.toString("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static int parseDifficulty(String s) {
        if ("easy".equalsIgnoreCase(s)) return Question.EASY;
        if ("hard".equalsIgnoreCase(s)) return Question.HARD;
        return Question.MEDIUM;
    }

    private static List<Question> load(Context c) {
        List<Question> list = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(readAsset(c, "questions.json"));
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                List<String> options = new ArrayList<>();
                List<String> accepted = new ArrayList<>();
                boolean typing = "typing".equalsIgnoreCase(o.optString("type", "choice"));
                if (typing) {
                    JSONArray a = o.getJSONArray("answers");
                    for (int k = 0; k < a.length(); k++) accepted.add(a.getString(k));
                } else {
                    JSONArray opts = o.getJSONArray("options");
                    for (int k = 0; k < opts.length(); k++) options.add(opts.getString(k));
                }
                list.add(new Question(i, o.getInt("level"), parseDifficulty(o.optString("difficulty", "medium")),
                        o.optString("code", ""), o.getString("prompt"), options, typing ? -1 : 0,
                        accepted, o.optString("explain", "")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
