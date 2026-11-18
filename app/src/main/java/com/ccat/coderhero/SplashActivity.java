package com.ccat.coderhero;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

/** Shows the logo for a moment (splash_ms in config.xml), then opens the main menu. Tap to skip. */
public class SplashActivity extends BaseActivity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean gone;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView title = findViewById(R.id.splashTitle);
        SpannableString s = new SpannableString("CODER HERO:\nSYNTAX SAVIOR");
        s.setSpan(new ForegroundColorSpan(0xFFA8003A), 6, 10, 0);
        title.setText(s);

        View logo = findViewById(R.id.splashLogo);
        Ui.fadeIn(logo, 0);
        Ui.fadeIn(title, 350);

        findViewById(R.id.splashRoot).setOnClickListener(v -> next());
        handler.postDelayed(this::next, Config.splashMs(this));
    }

    private void next() {
        if (gone) return;
        gone = true;
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
