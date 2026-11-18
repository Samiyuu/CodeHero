package com.ccat.coderhero;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Set;

/** Statistics, accuracy per topic, local top-5 leaderboard and trophies on one scrolling screen. */
public class StatsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.listTitle)).setText(R.string.title_stats);
        TextView back = findViewById(R.id.btnBack);
        Ui.pressable(back);
        back.setOnClickListener(v -> finish());
    }

    @Override protected void onResume() {
        super.onResume();
        LinearLayout list = findViewById(R.id.listContent);
        list.removeAllViews();

        // ---- overall
        int total = Prefs.totalAnswers(this), ok = Prefs.totalCorrect(this);
        int acc = total == 0 ? 0 : Math.round(100f * ok / total);
        list.addView(Ui.heading(this, "OVERALL"), Ui.lp(this, 0));
        list.addView(Ui.card(this, html(
                "Correct Answers: <b>" + ok + "</b> / " + total + "<br>"
                + "Accuracy: <b>" + acc + "%</b><br>"
                + "Levels Cleared: <b>" + Math.min(Prefs.unlocked(this) - 1, Level.ALL.length) + "/" + Level.ALL.length + "</b><br>"
                + "Stars: <b>" + Prefs.totalStars(this) + "/" + Level.ALL.length * 3 + "</b><br>"
                + "Best Answer Streak: <b>" + Prefs.bestStreak(this) + "</b><br>"
                + "Games Played: <b>" + Prefs.gamesPlayed(this) + "</b><br>"
                + "Play Streak: <b>" + Prefs.playStreak(this) + "</b> Days (Best: " + Prefs.bestPlayStreak(this) + "Days)<br>"
                + "Daily Streak: <b>" + Prefs.dailyStreak(this) + "</b> Days (Best: " + Prefs.bestDailyStreak(this) + "Days)")),
                Ui.lp(this, 6));

        // ---- accuracy per topic
        list.addView(Ui.heading(this, "ACCURACY BY TOPIC"), Ui.lp(this, 14));
        StringBuilder sb = new StringBuilder();
        for (Level l : Level.ALL) {
            int t = Prefs.topicTotal(this, l.id), c = Prefs.topicCorrect(this, l.id);
            int pct = t == 0 ? 0 : Math.round(100f * c / t);
            int blocks = Math.round(pct / 10f);
            StringBuilder bar = new StringBuilder();
            for (int i = 0; i < 10; i++) bar.append(i < blocks ? "\u2588" : "\u2591");
            if (sb.length() > 0) sb.append("<br><br>");
            sb.append("<b>").append(l.id).append(". ").append(l.topic).append("</b><br>")
              .append("<tt>").append(bar).append("</tt> ")
              .append(t == 0 ? "no answers yet" : pct + "% (" + c + "/" + t + ")");
        }
        list.addView(Ui.card(this, html(sb.toString())), Ui.lp(this, 6));

        // ---- leaderboard
        list.addView(Ui.heading(this, "TOP " + Config.leaderboardSize(this) + " ENDLESS SCORES"), Ui.lp(this, 14));
        List<Leaderboard.Entry> board = Leaderboard.load(this);
        StringBuilder lb = new StringBuilder();
        if (board.isEmpty()) lb.append("No Scores Yet. Play Endless mode!");
        for (int i = 0; i < board.size(); i++) {
            if (i > 0) lb.append("<br>");
            String medal = i == 0 ? "\uD83E\uDD47" : i == 1 ? "\uD83E\uDD48" : i == 2 ? "\uD83E\uDD49" : "#" + (i + 1);
            lb.append(medal).append(" <b>").append(board.get(i).score).append("</b> &nbsp;<small>")
              .append(board.get(i).dateText()).append("</small>");
        }
        list.addView(Ui.card(this, html(lb.toString())), Ui.lp(this, 6));

        // ---- trophies
        Set<String> have = Prefs.earned(this);
        List<Achievements.Def> defs = Achievements.all(this);
        list.addView(Ui.heading(this, "TROPHIES " + have.size() + "/" + defs.size()), Ui.lp(this, 14));
        StringBuilder tr = new StringBuilder();
        for (int i = 0; i < defs.size(); i++) {
            Achievements.Def d = defs.get(i);
            boolean got = have.contains(d.id);
            if (i > 0) tr.append("<br><br>");
            tr.append(got ? "\uD83C\uDFC6 " : "\uD83D\uDD12 ").append("<b>").append(d.title).append("</b><br>")
              .append("<small>").append(d.desc).append("</small>");
        }
        list.addView(Ui.card(this, html(tr.toString())), Ui.lp(this, 6));
    }

    @SuppressWarnings("deprecation")
    private static CharSequence html(String s) { return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY); }
}
