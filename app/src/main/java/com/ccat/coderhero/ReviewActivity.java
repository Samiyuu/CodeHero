package com.ccat.coderhero;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Lists the questions the player missed, with the explanation, and lets them retry only those. */
public class ReviewActivity extends BaseActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ((TextView) findViewById(R.id.listTitle)).setText(R.string.title_review);
        TextView back = findViewById(R.id.btnBack);
        Ui.pressable(back);
        back.setOnClickListener(v -> finish());

        final int[] ids = getIntent().getIntArrayExtra(GameActivity.EXTRA_IDS);
        LinearLayout list = findViewById(R.id.listContent);
        int shown = 0;
        if (ids != null) {
            for (int id : ids) {
                Question q = QuestionBank.byId(this, id);
                if (q == null) continue;
                if (!q.code.isEmpty()) list.addView(Ui.codeCard(this, q.code), Ui.lp(this, shown == 0 ? 0 : 14));
                list.addView(Ui.card(this, Html.fromHtml("<b>" + q.prompt + "</b><br><br>"
                        + "\u2713 <b>" + escape(q.correctText()) + "</b><br>"
                        + "\uD83D\uDCA1 " + q.explain, Html.FROM_HTML_MODE_LEGACY)),
                        Ui.lp(this, q.code.isEmpty() && shown > 0 ? 14 : 6));
                shown++;
            }
        }
        if (shown == 0) {
            list.addView(Ui.card(this, "Nothing to review. Flawless!"), Ui.lp(this, 0));
        } else {
            TextView retry = findViewById(R.id.btnAction);
            retry.setText(R.string.retry_missed);
            retry.setVisibility(TextView.VISIBLE);
            Ui.pressable(retry);
            retry.setOnClickListener(v -> {
                startActivity(new Intent(this, GameActivity.class)
                        .putExtra(GameActivity.EXTRA_MODE, GameActivity.MODE_REVIEW)
                        .putExtra(GameActivity.EXTRA_IDS, ids));
                finish();
            });
        }
    }

    private static String escape(String s) { return Html.escapeHtml(s); }
}
