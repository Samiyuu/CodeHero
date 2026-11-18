package com.ccat.coderhero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class ModeSelectActivity extends BaseActivity {
    private TextView storyInfo, endlessInfo, dailyInfo;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mode_select);

        storyInfo = findViewById(R.id.storyInfo);
        endlessInfo = findViewById(R.id.endlessInfo);
        dailyInfo = findViewById(R.id.dailyInfo);
        TextView story = findViewById(R.id.btnStory);
        TextView endless = findViewById(R.id.btnEndless);
        TextView daily = findViewById(R.id.btnDaily);
        TextView practice = findViewById(R.id.btnPractice);
        TextView learn = findViewById(R.id.btnLearn);
        TextView back = findViewById(R.id.btnBack);
        for (TextView t : new TextView[]{story, endless, daily, practice, learn, back}) Ui.pressable(t);

        story.setOnClickListener(v -> {
            Class<?> next = Prefs.prologueSeen(this) ? LevelSelectActivity.class : PrologueActivity.class;
            startActivity(new Intent(this, next));
        });
        endless.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class)
                .putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_ENDLESS)));
        daily.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class)
                .putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_DAILY)));
        practice.setOnClickListener(v -> startActivity(new Intent(this, PracticeActivity.class)));
        learn.setOnClickListener(v -> startActivity(new Intent(this, LearnActivity.class)));
        back.setOnClickListener(v -> finish());
    }

    @Override protected void onResume() {
        super.onResume();
        int done = Math.min(Prefs.unlocked(this) - 1, Level.ALL.length);
        storyInfo.setText(getString(R.string.info_story, done, Level.ALL.length,
                Prefs.totalStars(this), Level.ALL.length * 3));
        endlessInfo.setText(getString(R.string.info_endless, Prefs.bestEndless(this)));
        if (Prefs.dailyDoneToday(this)) {
            dailyInfo.setText(getString(R.string.info_daily_done, Prefs.dailyScoreToday(this),
                    Config.dailyCount(this), Prefs.dailyStreak(this)));
        } else {
            dailyInfo.setText(getString(R.string.info_daily_todo, Prefs.dailyStreak(this)));
        }
    }
}
