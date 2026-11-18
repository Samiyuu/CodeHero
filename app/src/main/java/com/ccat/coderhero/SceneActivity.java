package com.ccat.coderhero;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import androidx.core.content.res.ResourcesCompat;


/** Story scene shown before a level (or the epilogue after the last one). */
public class SceneActivity extends BaseActivity {
    public static final String EXTRA_EPILOGUE = "epilogue";

    private Level level;
    private boolean epilogue;
    private String[][] slides;
    private int slide = 0;
    private boolean first = true;
    private TextView top, bottom, next;
    private HeroView hero;
    private Object render;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prologue);

        epilogue = getIntent().getBooleanExtra(EXTRA_EPILOGUE, false);
        level = Level.get(getIntent().getIntExtra(GameActivity.EXTRA_LEVEL, 1));

        View root = findViewById(R.id.sceneRoot);
        root.setBackgroundResource(epilogue ? R.drawable.bg_main_coderhero : level.background);
        slides = epilogue ? StoryScenes.epilogue() : StoryScenes.get(level.id);

        top = findViewById(R.id.panelTop);
        bottom = findViewById(R.id.panelBottom);
        Typeface font = ResourcesCompat.getFont(this, R.font.orbitron_font);
        top.setTypeface(font);
        bottom.setTypeface(font);
        next = findViewById(R.id.btnNext);
        hero = findViewById(R.id.hero);
        hero.startIdle();
        Ui.pressable(next);
        next.setOnClickListener(v -> advance());

        // background first, panels after
        root.setAlpha(0f);
        root.animate().alpha(1f).setDuration(700).start();
        render();
    }

    private void advance() {
        if (slide < slides.length - 1) { slide++; render(); return; }
        if (epilogue) { finish(); return; }
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra(GameActivity.EXTRA_LEVEL, level.id);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void render() {
        top.setTextSize(18);
        top.setText(Html.fromHtml(slides[slide][0], Html.FROM_HTML_MODE_LEGACY));
        bottom.setText(Html.fromHtml(slides[slide][1], Html.FROM_HTML_MODE_LEGACY));
        if (epilogue) hero.setUnmasked(slide >= 2);
        if (slide == slides.length - 1) next.setText(epilogue ? "THE END" : "START");
        else next.setText("CONTINUE");

        long base = first ? 1000 : 0;
        Ui.fadeIn(top, base);
        Ui.fadeIn(bottom, base + 150);
        if (first) {
            Ui.fadeIn(hero, base + 300);
            Ui.fadeIn(next, base + 450);
            first = false;
        }
    }
}
