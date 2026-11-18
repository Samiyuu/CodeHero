package com.ccat.coderhero;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends BaseActivity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SwitchCompat sound = findViewById(R.id.switchSound);
        sound.setChecked(Prefs.sound(this));
        sound.setOnCheckedChangeListener((b, on) -> {
            Prefs.setSound(this, on);
            if (on) MusicManager.start(this); else MusicManager.pause();
        });

        SwitchCompat vibrate = findViewById(R.id.switchVibrate);
        vibrate.setChecked(Prefs.vibrate(this));
        vibrate.setOnCheckedChangeListener((b, on) -> {
            Prefs.setVibrate(this, on);
            if (on) Haptics.buzz(this, 60);
        });

        SwitchCompat large = findViewById(R.id.switchLargeText);
        large.setChecked(Prefs.largeText(this));
        large.setOnCheckedChangeListener((b, on) -> {
            Prefs.setLargeText(this, on);
            recreate(); // re-applies the font scale to this screen; other screens pick it up when opened
        });

        SwitchCompat cb = findViewById(R.id.switchColorBlind);
        cb.setChecked(Prefs.colorBlind(this));
        cb.setOnCheckedChangeListener((b, on) -> Prefs.setColorBlind(this, on));

        TextView reset = findViewById(R.id.btnReset);
        TextView back = findViewById(R.id.btnBack);
        Ui.pressable(reset); Ui.pressable(back);

        reset.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setTitle(R.string.reset_title)
                .setMessage(R.string.reset_message)
                .setPositiveButton("Reset", (d, w) -> {
                    Prefs.resetProgress(this);
                    Toast.makeText(this, "Progress reset", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show());
        back.setOnClickListener(v -> finish());
    }
}
