package com.ccat.coderhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;


public class PrologueActivity extends BaseActivity {
    private int slide = 0;
    private TextView top, bottom, next;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prologue);
        top = findViewById(R.id.panelTop);
        bottom = findViewById(R.id.panelBottom);
        next = findViewById(R.id.btnNext);
        HeroView hero = findViewById(R.id.hero);
        hero.startIdle();
        Ui.pressable(next);
        next.setOnClickListener(v -> advance());
        render();
    }

    private void advance() {
        if (slide == 0) { slide = 1; render(); return; }
        Prefs.setPrologueSeen(this, true);
        startActivity(new Intent(this, LevelSelectActivity.class));
        finish();
    }

    private void render() {
        if (slide == 0) {
            top.setText("PROLOGUE:\nTHE GLITCH");
            top.setTextSize(26);
            bottom.setText("LATE NIGHT AT CCAT LAB.\nSCREENS FLICKER RED.");
            next.setText("CONTINUE");
        } else {
            top.setTextSize(15);
            top.setText(fromHtml("<b>Boris</b> rub his tired eyes. Suddenly, every monitor blares an emergency alert. "
                    + "A rogue program called <b>The Corruptor</b> has taken over the city\u2019s core systems. "
                    + "Thousands of people are trapped\u2014their lives tied to the code keeping their facilities running."));
            bottom.setText(fromHtml("<b>A voice echoes from the mainframe:</b><br>"
                    + "\u201CThe only way to unlock them\u2026 is to speak the language perfectly. "
                    + "One wrong symbol, and the door closes forever.\u201D"));
            next.setText("START");
        }
    }

    @SuppressWarnings("deprecation")
    private static CharSequence fromHtml(String s) {
        return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
    }
}
