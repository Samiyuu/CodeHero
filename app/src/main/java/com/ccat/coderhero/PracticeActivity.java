package com.ccat.coderhero;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Topic picker for Practice mode (no timer, no fail). Topics come from Level.ALL. */
public class PracticeActivity extends BaseActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.listTitle)).setText(R.string.title_practice);
        TextView back = findViewById(R.id.btnBack);
        Ui.pressable(back);
        back.setOnClickListener(v -> finish());

        LinearLayout list = findViewById(R.id.listContent);
        list.addView(topicButton(getString(R.string.all_topics), 0, R.drawable.chip_label), Ui.lp(this, 0));
        for (Level l : Level.ALL) {
            list.addView(topicButton(l.id + ". " + l.topic, l.id, R.drawable.btn_blue), Ui.lp(this, 10));
        }
    }

    private TextView topicButton(String label, int level, int bg) {
        TextView b = Ui.button(this, label, bg);
        b.setTextSize(15);
        b.setOnClickListener(v -> startActivity(new Intent(this, GameActivity.class)
                .putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_PRACTICE)
                .putExtra(GameActivity.EXTRA_LEVEL, level)));
        return b;
    }
}
