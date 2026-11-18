package com.ccat.coderhero;

import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;


import android.content.Intent;

public class MainActivity extends BaseActivity {
    private HeroView hero;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hero = findViewById(R.id.hero);
        hero.setTalkative(true);

        TextView start = findViewById(R.id.btnStart);
        TextView stats = findViewById(R.id.btnStats);
        TextView settings = findViewById(R.id.btnSettings);
        TextView quit = findViewById(R.id.btnQuit);
        Ui.pressable(start); Ui.pressable(stats); Ui.pressable(settings); Ui.pressable(quit);

        start.setOnClickListener(v -> startActivity(new Intent(this, ModeSelectActivity.class)));
        stats.setOnClickListener(v -> startActivity(new Intent(this, StatsActivity.class)));
        settings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        quit.setOnClickListener(v -> finishAffinity());
    }

    @Override protected void onResume() {
        super.onResume();
        hero.startIdle();
    }
}
