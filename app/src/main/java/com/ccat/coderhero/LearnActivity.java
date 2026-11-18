package com.ccat.coderhero;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Cheat-sheets. Notes live in assets/learn.json (level, sections[heading, text, code]).
 * Opened with no extra = topic list; with EXTRA_LEVEL = that topic's notes directly.
 */
public class LearnActivity extends BaseActivity {
    private LinearLayout content;
    private TextView title, back;
    private JSONArray data;
    private boolean direct;      // opened straight onto one topic (from the level select)
    private boolean showingTopic;

    private Typeface font;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        content = findViewById(R.id.listContent);
        title = findViewById(R.id.listTitle);
        back = findViewById(R.id.btnBack);
        Ui.pressable(back);
        back.setOnClickListener(v -> goBack());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { goBack(); }
        });

        try { data = new JSONArray(QuestionBank.readAsset(this, "learn.json")); }
        catch (Exception e) { data = new JSONArray(); }

        int level = getIntent().getIntExtra(GameActivity.EXTRA_LEVEL, 0);
        font = ResourcesCompat.getFont(this, R.font.orbitron_font);
        if (level > 0) { direct = true; showTopic(level); } else showList();
    }

    private void goBack() {
        if (showingTopic && !direct) showList(); else finish();
    }

    private void showList() {
        showingTopic = false;
        title.setText(R.string.title_learn);
        title.setTextSize(22);
        back.setText(R.string.back);
        content.removeAllViews();
        for (Level l : Level.ALL) {
            TextView b = Ui.button(this, l.id + ". " + l.topic, R.drawable.btn_blue);
            b.setTextSize(15);
            final int id = l.id;
            b.setOnClickListener(v -> showTopic(id));
            content.addView(b, Ui.lp(this, id == 1 ? 0 : 10));
        }
    }

    private void showTopic(int level) {
        showingTopic = true;
        Level l = Level.get(level);
        title.setText("LEVEL " + level);
        title.setTextSize(20);
        content.removeAllViews();
        content.addView(Ui.heading(this, l.topic), Ui.lp(this, 0));
        try {
            for (int i = 0; i < data.length(); i++) {
                JSONObject t = data.getJSONObject(i);
                if (t.getInt("level") != level) continue;
                JSONArray secs = t.getJSONArray("sections");
                for (int k = 0; k < secs.length(); k++) {
                    JSONObject s = secs.getJSONObject(k);
                    TextView card = Ui.card(this, android.text.Html.fromHtml("<b>" + s.getString("heading")
                            + "</b><br>" + s.optString("text", ""), android.text.Html.FROM_HTML_MODE_LEGACY));
                    card.setTypeface(font);
                    content.addView(card, Ui.lp(this, 10));
                    String code = s.optString("code", "");
                    if (!code.isEmpty()) content.addView(Ui.codeCard(this, code), Ui.lp(this, 6));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        back.setText(direct ? getString(R.string.back) : getString(R.string.title_learn));
    }
}
