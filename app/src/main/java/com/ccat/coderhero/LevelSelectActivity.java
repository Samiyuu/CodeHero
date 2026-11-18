package com.ccat.coderhero;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LevelSelectActivity extends BaseActivity {
    private LinearLayout list;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);
        list = findViewById(R.id.levelList);
        TextView back = findViewById(R.id.btnBack);
        Ui.pressable(back);
        back.setOnClickListener(v -> finish());
    }

    /** "★★☆" in gold (filled) and grey (empty). */
    static String starsHtml(Context ctx, int stars) {
        String gold = String.format("#%06X", 0xFFFFFF & ctx.getColor(R.color.star_gold));
        String grey = String.format("#%06X", 0xFFFFFF & ctx.getColor(R.color.star_empty));
        StringBuilder sb = new StringBuilder();
        sb.append("<font color='").append(gold).append("'>");
        for (int i = 0; i < stars; i++) sb.append("\u2605");
        sb.append("</font><font color='").append(grey).append("'>");
        for (int i = stars; i < 3; i++) sb.append("\u2606");
        sb.append("</font>");
        return sb.toString();
    }

    @Override protected void onResume() {
        super.onResume();
        list.removeAllViews();
        int unlocked = Prefs.unlocked(this);

        TextView total = Ui.heading(this, getString(R.string.stars_total, Prefs.totalStars(this), Level.ALL.length * 3));
        total.setGravity(Gravity.CENTER);
        list.addView(total, Ui.lp(this, 0));

        for (Level l : Level.ALL) {
            final boolean open = l.id <= unlocked;
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);

            TextView t = new TextView(this);
            t.setText(Html.fromHtml("<b>LEVEL " + l.id + (open ? "" : " \uD83D\uDD12") + "</b> &nbsp;"
                    + (open ? starsHtml(this, Prefs.stars(this, l.id)) : "") + "<br>"
                    + l.title + "<br><small>" + l.topic + "</small>", Html.FROM_HTML_MODE_LEGACY));
            t.setBackgroundResource(R.drawable.wood_sign);
            t.setTextColor(getColor(R.color.wood_text));
            t.setTextSize(16);
            t.setGravity(Gravity.CENTER);
            t.setTypeface(getResources().getFont(R.font.orbitron_font));
            int pad = Ui.dp(this, 16);
            t.setPadding(pad, pad, pad, pad);
            t.setAlpha(open ? 1f : 0.5f);
            row.addView(t, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            Ui.pressable(t);
            t.setOnClickListener(v -> {
                if (!open) {
                    Toast.makeText(this, "Rescue The Previous Level First!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i = new Intent(this, SceneActivity.class);
                i.putExtra(GameActivity.EXTRA_LEVEL, l.id);
                startActivity(i);
            });

            // book button: read the notes for this level's topic before playing
            TextView book = Ui.button(this, "\uD83D\uDCD6", R.drawable.btn_blue);
            book.setTextSize(20);
            book.setPadding(Ui.dp(this, 12), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 14));
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            bp.setMarginStart(Ui.dp(this, 8));
            row.addView(book, bp);
            book.setOnClickListener(v -> startActivity(new Intent(this, LearnActivity.class)
                    .putExtra(GameActivity.EXTRA_LEVEL, l.id)));

            list.addView(row, Ui.lp(this, 12));
        }
    }
}
