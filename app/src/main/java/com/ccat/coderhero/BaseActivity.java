package com.ccat.coderhero;

import android.content.Context;
import android.content.res.Configuration;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Every screen extends this. It (1) applies the "Large text" setting and
 * (2) keeps content from stretching too wide on tablets (limit in config.xml).
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override protected void attachBaseContext(Context base) {
        if (Prefs.largeText(base)) {
            Configuration cfg = new Configuration(base.getResources().getConfiguration());
            cfg.fontScale = cfg.fontScale * Config.largeTextScale(base);
            super.attachBaseContext(base.createConfigurationContext(cfg));
        } else {
            super.attachBaseContext(base);
        }
    }

    /** Keeps AppCompat from resetting the font scale we set above (known AppCompat 1.2+ quirk). */
    @Override public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    @Override public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        limitWidth();
    }

    private void limitWidth() {
        ViewGroup content = findViewById(android.R.id.content);
        if (content == null || content.getChildCount() == 0) return;
        View root = content.getChildAt(0);
        float density = getResources().getDisplayMetrics().density;
        int screenPx = getResources().getDisplayMetrics().widthPixels;
        int maxPx = Math.round(Config.maxContentDp(this) * density);
        if (screenPx > maxPx) {
            int side = (screenPx - maxPx) / 2;
            root.setPadding(root.getPaddingLeft() + side, root.getPaddingTop(),
                    root.getPaddingRight() + side, root.getPaddingBottom());
        }
    }
}
