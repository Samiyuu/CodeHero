package com.ccat.coderhero;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class Question {
    public static final int EASY = 1, MEDIUM = 2, HARD = 3;

    public final int id;           // position in questions.json (stable id)
    public final int level;
    public final int difficulty;   // EASY / MEDIUM / HARD
    public final String code;      // optional code snippet ("" if none)
    public final String prompt;
    public final List<String> options;   // empty for typing questions
    public final int answer;             // index of correct option (-1 for typing)
    public final List<String> accepted;  // accepted typed answers (typing questions only)
    public final String explain;

    public Question(int id, int level, int difficulty, String code, String prompt, List<String> options,
                    int answer, List<String> accepted, String explain) {
        this.id = id; this.level = level; this.difficulty = difficulty;
        this.code = code; this.prompt = prompt; this.options = options;
        this.answer = answer; this.accepted = accepted; this.explain = explain;
    }

    public boolean isTyping() { return !accepted.isEmpty(); }

    /** Typing check: ignores spaces, but Java is case-sensitive so case must match. */
    public boolean matchesTyped(String typed) {
        String t = squash(typed);
        for (String a : accepted) if (squash(a).equals(t)) return true;
        return false;
    }
    private static String squash(String s) { return s == null ? "" : s.replaceAll("\\s+", ""); }

    /** The text shown as "the answer" in feedback and the review screen. */
    public String correctText() {
        return isTyping() ? accepted.get(0) : options.get(answer);
    }

    /** Returns a copy with options shuffled (the correct answer is tracked). */
    public Question shuffled(Random rnd) {
        if (isTyping()) return this;
        String correct = options.get(answer);
        List<String> copy = new ArrayList<>(options);
        Collections.shuffle(copy, rnd);
        return new Question(id, level, difficulty, code, prompt, copy, copy.indexOf(correct), accepted, explain);
    }
}
