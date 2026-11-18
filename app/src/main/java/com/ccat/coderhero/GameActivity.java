package com.ccat.coderhero;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * One activity runs every game mode:
 *  STORY (one Level, timed, one mistake ends it) / ENDLESS (lives, tiers) / PRACTICE (no timer, no fail) /
 *  DAILY (same questions all day, streak) / REVIEW (retry missed questions).
 * Tuning numbers live in res/values/config.xml.
 */
public class GameActivity extends BaseActivity {
    public static final String EXTRA_MODE = "mode";
    public static final String EXTRA_LEVEL = "level";
    public static final String EXTRA_IDS = "ids";

    public static final String MODE_STORY = "story", MODE_ENDLESS = "endless", MODE_PRACTICE = "practice",
            MODE_DAILY = "daily", MODE_REVIEW = "review";

    private static final String[] TIER_NAMES = {"", "EASY", "MEDIUM", "HARD"};

    private enum State { INTRO, PLAYING, RESULT }

    private String mode;
    private boolean story, endless, practice, daily, review, timed;
    private Level level;
    private int practiceLevel;          // 0 = all topics
    private int[] reviewIds;
    private State state = State.INTRO;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random rnd = new Random();

    private View root, introGroup, playGroup, resultGroup, btnPause, bossGroup, optionsGroup, typeGroup, heartIcon;
    private View introInfoBox, introAlertBox, introSystemBox;
    private TextView introInfo, introAlert, introSystem, btnIntroStart, timerText, codeText,
            promptText, resultText, explainText, btnPrimary, btnSecondary, btnReview, btnHint, btnSubmit,
            bossLabel, badgeText;
    private BevelTextView header, feedback;
    private final BevelTextView[] opts = new BevelTextView[4];
    private EditText typeInput;
    private ProgressBar timerBar, bossBar;
    private HeroView heroIntro, heroPlay, heroResult;

    private CountDownTimer timer;
    private ObjectAnimator pulse;
    private boolean warnActive;
    private long barTotal = 1000, remainingMs = 0;

    private List<Question> queue = new ArrayList<>();
    private final List<Question> missed = new ArrayList<>();
    private int index, lives, score, streak, correct, hintsLeft;
    private Question current;
    private boolean locked, dialogOpen, waitTap;
    private Dialog pauseDialog;


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_STORY;
        story = MODE_STORY.equals(mode);
        endless = MODE_ENDLESS.equals(mode);
        practice = MODE_PRACTICE.equals(mode);
        daily = MODE_DAILY.equals(mode);
        review = MODE_REVIEW.equals(mode);
        timed = story || endless || daily;
        practiceLevel = practice ? getIntent().getIntExtra(EXTRA_LEVEL, 0) : 0;
        level = Level.get(story ? getIntent().getIntExtra(EXTRA_LEVEL, 1) : Math.max(1, practiceLevel));
        reviewIds = getIntent().getIntArrayExtra(EXTRA_IDS);

        root = findViewById(R.id.root);
        introGroup = findViewById(R.id.introGroup);
        playGroup = findViewById(R.id.playGroup);
        resultGroup = findViewById(R.id.resultGroup);
        header = findViewById(R.id.header);
        introInfo = findViewById(R.id.introInfo);
        introAlert = findViewById(R.id.introAlert);
        introSystem = findViewById(R.id.introSystem);
        introInfoBox = findViewById(R.id.introInfoBox);
        introAlertBox = findViewById(R.id.introAlertBox);
        introSystemBox = findViewById(R.id.introSystemBox);
        btnIntroStart = findViewById(R.id.btnIntroStart);
        timerText = findViewById(R.id.timerText);
        timerBar = findViewById(R.id.timerBar);
        heartIcon = findViewById(R.id.heartIcon);
        bossGroup = findViewById(R.id.bossGroup);
        bossBar = findViewById(R.id.bossBar);
        bossLabel = findViewById(R.id.bossLabel);
        codeText = findViewById(R.id.codeText);
        promptText = findViewById(R.id.promptText);
        feedback = findViewById(R.id.feedback);
        optionsGroup = findViewById(R.id.optionsGroup);
        typeGroup = findViewById(R.id.typeGroup);
        typeInput = findViewById(R.id.typeInput);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnHint = findViewById(R.id.btnHint);
        resultText = findViewById(R.id.resultText);
        explainText = findViewById(R.id.explainText);
        badgeText = findViewById(R.id.badgeText);
        btnPrimary = findViewById(R.id.btnPrimary);
        btnReview = findViewById(R.id.btnReview);
        btnSecondary = findViewById(R.id.btnSecondary);
        btnPause = findViewById(R.id.btnPause);
        heroIntro = findViewById(R.id.heroIntro);
        heroPlay = findViewById(R.id.heroPlay);
        heroResult = findViewById(R.id.heroResult);



        int[] ids = {R.id.opt0, R.id.opt1, R.id.opt2, R.id.opt3};
        for (int i = 0; i < 4; i++) {
            opts[i] = findViewById(ids[i]);
            final int idx = i;
            opts[i].setOnClickListener(v -> onAnswer(idx));
            Ui.pressableQuiet(opts[i]);
        }
        Ui.pressable(btnIntroStart); Ui.pressable(btnPrimary); Ui.pressable(btnSecondary);
        Ui.pressable(btnPause); Ui.pressable(btnReview); Ui.pressable(btnSubmit); Ui.pressable(btnHint);

        boolean lab = endless || daily || review || (practice && practiceLevel == 0);
        root.setBackgroundResource(lab ? R.drawable.bg_main_coderhero : level.background);
        heroIntro.startIdle();
        heroIntro.setTalkative(true);

        btnIntroStart.setOnClickListener(v -> startGame());
        btnSecondary.setOnClickListener(v -> finish());
        btnPause.setOnClickListener(v -> showPause());
        btnHint.setOnClickListener(v -> useHint());
        btnSubmit.setOnClickListener(v -> onSubmitTyping());
        typeInput.setOnEditorActionListener((v, actionId, e) -> { onSubmitTyping(); return true; });
        feedback.setOnClickListener(v -> {
            if (waitTap) { waitTap = false; handler.removeCallbacksAndMessages(null); showQuestion(); }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (state == State.PLAYING) showPause(); else finish();
            }
        });

        showIntro();
    }

    // ---------------------------------------------------------------- intro
    private void showIntro() {
        state = State.INTRO;
        introGroup.setVisibility(View.VISIBLE);
        playGroup.setVisibility(View.GONE);
        resultGroup.setVisibility(View.GONE);
        CharSequence alertText;
        int maxLives = Config.endlessLives(this);
        if (endless) {
            introInfo.setText(html("<b>ENDLESS MODE</b><br><font color='#A8003A'><b>SURVIVE THE CORRUPTION</b></font><br>"
                    + "<b>Lives:</b> " + maxLives + " &nbsp; <b>Best:</b> " + Prefs.bestEndless(this)));
            alertText = html("<b>NO END IN SIGHT.</b><br>Questions start easy and get harder. "
                    + "The streak bonus grows, but the clock gets shorter.");
            introSystem.setText("\u201CEach wrong symbol costs a life. Lose all " + maxLives + " and the Corruptor wins.\u201D");
        } else if (practice) {
            String topic = practiceLevel == 0 ? getString(R.string.all_topics) : level.topic;
            introInfo.setText(html("<b>PRACTICE MODE</b><br><font color='#A8003A'><b>" + topic + "</b></font><br>"
                    + "<b>Timer:</b> none &nbsp; <b>Questions:</b> " + Config.practiceCount(this)));
            alertText = html("<b>TAKE YOUR TIME.</b><br>No timer, no fail. Every answer shows its explanation.");
            introSystem.setText("\u201CLearn the syntax here, then face the Corruptor.\u201D");
        } else if (daily) {
            introInfo.setText(html("<b>DAILY CHALLENGE</b><br><font color='#A8003A'><b>SAME 5 FOR EVERYONE TODAY</b></font><br>"
                    + "<b>Limit:</b> " + Config.dailySeconds(this) + " seconds &nbsp; <b>Streak:</b> " + Prefs.dailyStreak(this)));
            alertText = html("<b>" + (Prefs.dailyDoneToday(this) ? "ALREADY DONE TODAY." : "NEW ALERT TODAY.")
                    + "</b><br>" + (Prefs.dailyDoneToday(this)
                    ? "You can replay for fun. Your streak was already counted."
                    : "Answer all " + Config.dailyCount(this) + " to keep your streak alive."));
            introSystem.setText("\u201CCome back tomorrow for a fresh set.\u201D");
        } else if (review) {
            introInfo.setText(html("<b>REVIEW</b><br><font color='#A8003A'><b>RETRY YOUR MISSES</b></font><br>"
                    + "<b>Questions:</b> " + (reviewIds == null ? 0 : reviewIds.length)));
            alertText = html("<b>SECOND CHANCE.</b><br>No timer. Fix the ones you got wrong.");
            introSystem.setText("\u201CMistakes are just unexplained syntax.\u201D");
        } else {
            introInfo.setText(html("<b>LEVEL " + level.id + ":</b><br><font color='#A8003A'><b>" + level.title
                    + "</b></font><br><b>Victim:</b> " + level.victim
                    + "<br><b>Limit:</b> " + level.seconds + " Seconds Challenge"
                    + "<br><b>Topic:</b> " + level.topic));
            alertText = html("<b>" + level.alertTitle + "</b><br>" + level.alert);
            introSystem.setText("\u201C" + level.system + "\u201D");
        }
        android.text.SpannableString s = new android.text.SpannableString(alertText);
        int nl = s.toString().indexOf('\n');
        s.setSpan(new android.text.style.LeadingMarginSpan.Standard(Ui.dp(this, 48), 0),
                0, nl > 0 ? nl : s.length(), android.text.Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        introAlert.setText(s);

        // background first, then the panels one after another
        Ui.fadeIn(introInfoBox, 700);
        Ui.fadeIn(introAlertBox, 850);
        Ui.fadeIn(introSystemBox, 1000);
        Ui.fadeIn(heroIntro, 1150);
        Ui.fadeIn(btnIntroStart, 1300);
    }

    // ---------------------------------------------------------------- game flow
    private void startGame() {
        handler.removeCallbacksAndMessages(null);
        cancelTimer();
        state = State.PLAYING;
        introGroup.setVisibility(View.GONE);
        resultGroup.setVisibility(View.GONE);
        playGroup.setVisibility(View.VISIBLE);
        heroPlay.setUnmasked(false);
        heroPlay.setMood(HeroView.Mood.NORMAL);
        score = 0; streak = 0; index = 0; correct = 0; remainingMs = 0; waitTap = false;
        lives = Config.endlessLives(this);
        hintsLeft = Config.hintsPerRun(this);
        missed.clear();
        locked = false;
        Prefs.recordPlay(this);

        // the clock row is only meaningful in timed modes
        int vis = timed ? View.VISIBLE : View.INVISIBLE;
        timerBar.setVisibility(vis); timerText.setVisibility(vis); heartIcon.setVisibility(vis);

        buildQueue();
        boolean boss = story && level.id == Config.bossLevel(this);
        bossGroup.setVisibility(boss ? View.VISIBLE : View.GONE);
        if (boss) { bossBar.setProgress(1000); updateBoss(false); }

        if (story) { Prefs.addAttempt(this, level.id); beginTimer(level.seconds * 1000L); }
        else if (daily) beginTimer(Config.dailySeconds(this) * 1000L);
        showQuestion();
    }

    private void buildQueue() {
        queue = new ArrayList<>();
        if (endless) return; // Endless picks one question at a time (see pickEndless)
        if (daily) {
            Random dr = new Random(Long.parseLong(Prefs.today())); // same seed all day = same questions
            List<Question> pool = new ArrayList<>(QuestionBank.all(this));
            Collections.shuffle(pool, dr);
            for (int i = 0; i < Math.min(Config.dailyCount(this), pool.size()); i++) queue.add(pool.get(i).shuffled(dr));
        } else if (review) {
            if (reviewIds != null) for (int id : reviewIds) {
                Question q = QuestionBank.byId(this, id);
                if (q != null) queue.add(q.shuffled(rnd));
            }
        } else if (practice) {
            List<Question> pool = new ArrayList<>(practiceLevel == 0
                    ? QuestionBank.all(this) : QuestionBank.forLevel(this, practiceLevel));
            Collections.shuffle(pool, rnd);
            for (int i = 0; i < Math.min(Config.practiceCount(this), pool.size()); i++) queue.add(pool.get(i).shuffled(rnd));
        } else {
            List<Question> pool = new ArrayList<>(QuestionBank.forLevel(this, level.id));
            Collections.shuffle(pool, rnd);
            for (int i = 0; i < Math.min(level.count, pool.size()); i++) queue.add(pool.get(i).shuffled(rnd));
        }
    }

    private int endlessTier() {
        return Math.min(3, 1 + correct / Config.endlessTierStep(this));
    }

    /** Endless: pick a random question of the current tier that was not asked recently (history is saved). */
    private Question pickEndless() {
        int tier = endlessTier();
        List<Question> all = QuestionBank.all(this);
        List<Integer> recent = Prefs.recentIds(this);
        List<Question> pool = new ArrayList<>();
        for (Question q : all) if (q.difficulty == tier && !recent.contains(q.id)) pool.add(q);
        if (pool.isEmpty()) for (Question q : all)
            if (Math.abs(q.difficulty - tier) <= 1 && !recent.contains(q.id)) pool.add(q);
        if (pool.isEmpty()) for (Question q : all) if (!recent.contains(q.id)) pool.add(q);
        if (pool.isEmpty()) { Prefs.clearRecent(this); pool.addAll(all); }
        if (pool.isEmpty()) return null;
        Question q = pool.get(rnd.nextInt(pool.size())).shuffled(rnd);
        Prefs.pushRecent(this, q.id, Config.historySize(this));
        return q;
    }

    private void showQuestion() {
        if (state != State.PLAYING) return;
        locked = false; waitTap = false;
        feedback.setVisibility(View.GONE);
        if (endless) {
            current = pickEndless();
            if (current == null) { finish(); return; } // safety: empty knowledge base
        } else {
            if (queue.isEmpty()) { finish(); return; }
            if (index >= queue.size()) { finishRun(); return; }
            current = queue.get(index);
        }

        if (current.code.isEmpty()) codeText.setVisibility(View.GONE);
        else { codeText.setVisibility(View.VISIBLE); codeText.setText(CodeHighlighter.format(this, current.code)); }
        promptText.setText(current.prompt);

        if (current.isTyping()) {
            optionsGroup.setVisibility(View.GONE);
            typeGroup.setVisibility(View.VISIBLE);
            typeInput.setText("");
            typeInput.setEnabled(true);
            typeInput.setBackgroundResource(R.drawable.opt_default);
            typeInput.requestFocus();
            showKeyboard();
        } else {
            hideKeyboard();
            typeGroup.setVisibility(View.GONE);
            optionsGroup.setVisibility(View.VISIBLE);
            for (int i = 0; i < 4; i++) {
                if (i < current.options.size()) {
                    opts[i].setVisibility(View.VISIBLE);
                    opts[i].setText(current.options.get(i));
                    opts[i].setCompoundDrawablesRelativeWithIntrinsicBounds(
                            new LetterBadge(this, (char) ('A' + i)), null, null, null);
                    opts[i].setCompoundDrawablePadding(Ui.dp(this, 14));
                    styleOption(opts[i], DEFAULT);
                } else {
                    opts[i].setVisibility(View.GONE);
                }
            }
        }
        updateHintButton();
        heroPlay.setMood(warnActive ? HeroView.Mood.WORRIED : HeroView.Mood.NORMAL);
        updateHud();
        if (endless) beginTimer(endlessMillis());
        else if (timed && timer == null && remainingMs > 0) runTimer(remainingMs); // resume after backgrounding
    }

    private long endlessMillis() {
        int seconds = Math.max(Config.endlessMinSec(this),
                Config.endlessStartSec(this) - correct / Config.endlessSecStep(this)); // clock shrinks as you improve
        return seconds * 1000L;
    }

    private void updateHud() {
        if (endless) {
            StringBuilder hearts = new StringBuilder();
            for (int i = 0; i < Config.endlessLives(this); i++) hearts.append(i < lives ? "\u2665" : "\u2661");
            header.setText(html("<b>ENDLESS</b> &nbsp; <font color='#A8003A'>" + hearts + "</font> &nbsp;<small>"
                    + TIER_NAMES[endlessTier()] + "</small><br>"
                    + "Score <b>" + score + "</b> &nbsp; Streak <b>" + streak + "</b>"));
        } else if (practice) {
            header.setText(html("<b>PRACTICE</b> \u00B7 <font color='#A8003A'><b>"
                    + (practiceLevel == 0 ? getString(R.string.all_topics) : level.topic)
                    + "</b></font><br>Question <b>" + Math.min(index + 1, queue.size()) + "/" + queue.size() + "</b>"));
        } else if (daily) {
            header.setText(html("<b>DAILY CHALLENGE</b><br>Question <b>" + Math.min(index + 1, queue.size())
                    + "/" + queue.size() + "</b> &nbsp; Correct <b>" + correct + "</b>"));
        } else if (review) {
            header.setText(html("<b>REVIEW</b><br>Question <b>" + Math.min(index + 1, queue.size())
                    + "/" + queue.size() + "</b>"));
        } else {
            header.setText(html("<b>LEVEL " + level.id + "</b> \u00B7 <font color='#A8003A'><b>" + level.title
                    + "</b></font><br>Question <b>" + Math.min(index + 1, queue.size()) + "/" + queue.size() + "</b>"));
        }
    }

    // ---------------------------------------------------------------- hint (50/50)
    private void updateHintButton() {
        btnHint.setText(hintsLeft > 0 ? getString(R.string.hint_btn, hintsLeft) : getString(R.string.hint_btn_empty));
        boolean usable = hintsLeft > 0 && current != null && !current.isTyping();
        btnHint.setAlpha(usable ? 1f : 0.4f);
    }

    private void useHint() {
        if (state != State.PLAYING || locked || hintsLeft <= 0 || current == null || current.isTyping()) return;
        List<Integer> wrong = new ArrayList<>();
        for (int i = 0; i < current.options.size() && i < 4; i++)
            if (i != current.answer && opts[i].getVisibility() == View.VISIBLE) wrong.add(i);
        int hide = Math.max(0, Math.min(2, wrong.size() - 1));
        if (hide == 0) return;
        hintsLeft--;
        Collections.shuffle(wrong, rnd);
        for (int k = 0; k < hide; k++) opts[wrong.get(k)].setVisibility(View.INVISIBLE);
        updateHintButton();
    }

    // ---------------------------------------------------------------- answering
    private static final int DEFAULT = 0, GOOD = 1, BAD = 2;

    private void styleOption(BevelTextView v, int state) {
        boolean cb = Prefs.colorBlind(this);
        switch (state) {
            case GOOD:
                if (cb) v.setColors(0xFFE3F2FF, 0xFF0D3B66, 0xFF4FA3E8);   // blue for colour-blind mode
                else    v.setColors(0xFFEAFBEF, 0xFF0B4A1E, 0xFF4DEB6A);
                break;
            case BAD:
                if (cb) v.setColors(0xFFFFF1DF, 0xFF5A3300, 0xFFE69A2E);   // orange for colour-blind mode
                else    v.setColors(0xFFFFF0F3, 0xFF5A0020, 0xFFE56A8E);
                break;
            default:
                v.setColors(0xFFF7F5FC, 0xFF2A3358, 0xFFA9B8E8);
        }
    }
    private void markOption(int i, boolean good) {
        styleOption(opts[i], good ? GOOD : BAD);
        if (Prefs.colorBlind(this)) {
            opts[i].setText(current.options.get(i) + (good ? "  \u2713" : "  \u2717")); // not colour-only
        }
    }

    private void onAnswer(int i) {
        if (locked || state != State.PLAYING || current == null || current.isTyping()) return;
        locked = true;
        boolean ok = i == current.answer;
        markOption(i, ok);
        if (!ok) markOption(current.answer, true);
        resolve(ok);
    }

    private void onSubmitTyping() {
        if (locked || state != State.PLAYING || current == null || !current.isTyping()) return;
        String typed = typeInput.getText().toString();
        if (typed.trim().isEmpty()) return;
        locked = true;
        hideKeyboard();
        typeInput.setEnabled(false);
        boolean ok = current.matchesTyped(typed);
        boolean cb = Prefs.colorBlind(this);
        typeInput.setBackgroundResource(ok ? (cb ? R.drawable.opt_correct_cb : R.drawable.opt_correct)
                                           : (cb ? R.drawable.opt_wrong_cb : R.drawable.opt_wrong));
        resolve(ok);
    }

    private void showFeedback(boolean ok, String text) {
        if (ok) {
            feedback.setColors(0xFFEAFBEF, 0xFF0B4A1E, 0xFF4DEB6A);
            feedback.setTextColor(0xFF0B4A1E);
        } else {
            feedback.setColors(0xFFFFF0F3, 0xFF5A0020, 0xFFE56A8E);
            feedback.setTextColor(getColor(R.color.crimson));
        }
        feedback.setText(text);
        feedback.setVisibility(View.VISIBLE);
    }

    private String wrongText() {
        String head = current.isTyping() ? "\u2717 " + getString(R.string.wrong_answer_is, current.correctText()) + "\n" : "";
        return head + "\uD83D\uDCA1 " + current.explain;
    }

    private void resolve(boolean ok) {
        Prefs.recordAnswer(this, current.level, ok, current.isTyping());
        boolean tapMode = practice || review;

        if (ok) {
            SoundFx.play(this, SoundFx.Kind.CORRECT);
            heroPlay.bounce();
            heroPlay.setMood(HeroView.Mood.HAPPY);
            correct++; streak++;
            Prefs.noteStreak(this, streak);
            if (endless) { cancelTimer(); score += 10 + Math.min(streak, 10) * 2; }
            index++;
            updateHud();
            updateBoss(true);
            if (tapMode) {
                showFeedback(true, getString(R.string.correct_mark) + "\n\uD83D\uDCA1 " + current.explain
                        + "\n" + getString(R.string.tap_continue));
                waitTap = true;
            } else {
                handler.postDelayed(this::showQuestion, 650);
            }
        } else {
            SoundFx.play(this, SoundFx.Kind.WRONG);
            Haptics.buzz(this, Config.vibrateWrongMs(this));
            missed.add(current);
            streak = 0;
            if (story || endless) cancelTimer();
            heroPlay.setMood(HeroView.Mood.SAD);
            showFeedback(false, wrongText() + (tapMode ? "\n" + getString(R.string.tap_continue) : ""));
            index++;

            if (story) {
                final String why = current.explain;
                handler.postDelayed(() -> lose(why), 1700);
            } else if (endless) {
                lives--;
                updateHud();
                if (lives <= 0) handler.postDelayed(this::endlessOver, 1700);
                else handler.postDelayed(this::showQuestion, 1900);
            } else if (daily) {
                updateHud();
                handler.postDelayed(this::showQuestion, 1900);
            } else {
                updateHud();
                waitTap = true;
            }
        }
    }

    private void onTimeUp() {
        if (state != State.PLAYING) return;
        if (endless) {
            if (locked) return;
            locked = true;
            hideKeyboard();
            Prefs.recordAnswer(this, current.level, false, current.isTyping());
            missed.add(current);
            SoundFx.play(this, SoundFx.Kind.WRONG);
            Haptics.buzz(this, Config.vibrateWrongMs(this));
            heroPlay.setMood(HeroView.Mood.SAD);
            if (!current.isTyping()) markOption(current.answer, true);
            showFeedback(false, "\u23F0 Time's up! " + (current.isTyping()
                    ? getString(R.string.wrong_answer_is, current.correctText()) + "\n" : "") + current.explain);
            streak = 0; lives--; index++;
            updateHud();
            if (lives <= 0) handler.postDelayed(this::endlessOver, 1700);
            else handler.postDelayed(this::showQuestion, 1900);
        } else if (story) {
            if (!locked && current != null) {
                Prefs.recordAnswer(this, current.level, false, current.isTyping());
                missed.add(current);
            }
            locked = true;
            hideKeyboard();
            lose("Time's up! The countdown hit zero before the system could be fixed.");
        } else if (daily) {
            if (!locked && current != null) {
                Prefs.recordAnswer(this, current.level, false, current.isTyping());
                missed.add(current);
            }
            locked = true;
            hideKeyboard();
            dailyFinish(true);
        }
    }

    // ---------------------------------------------------------------- boss fight
    private void updateBoss(boolean animate) {
        if (bossGroup.getVisibility() != View.VISIBLE) return;
        int total = Math.max(1, queue.size());
        int hp = Math.max(0, total - correct);
        int target = 1000 * hp / total;
        ObjectAnimator a = ObjectAnimator.ofInt(bossBar, "progress", bossBar.getProgress(), target);
        a.setDuration(animate ? 450 : 0);
        a.start();
        bossLabel.setText(getString(R.string.boss_name) + "  " + hp + "/" + total);
        if (animate) {
            ObjectAnimator shake = ObjectAnimator.ofFloat(bossGroup, View.TRANSLATION_X, 0, 16, -16, 9, -9, 0);
            shake.setDuration(380);
            shake.start();
        }
    }

    // ---------------------------------------------------------------- results
    private void finishRun() {
        if (story) win();
        else if (daily) dailyFinish(false);
        else if (practice) practiceDone();
        else reviewDone();
    }

    private static String starText(int stars) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) sb.append(i < stars ? "\u2605" : "\u2606");
        return sb.toString();
    }

    private void win() {
        cancelTimer();
        Prefs.unlock(this, level.id + 1);
        int pct = barTotal > 0 ? (int) (remainingMs * 100 / barTotal) : 0;
        int stars = pct >= Config.star3Percent(this) ? 3 : pct >= Config.star2Percent(this) ? 2 : 1;
        Prefs.setStars(this, level.id, stars);
        if (Prefs.attempts(this, level.id) <= 1) Prefs.setFirstTry(this, level.id);
        Prefs.clearAttempts(this, level.id);
        if (bossGroup.getVisibility() == View.VISIBLE) { correct = queue.size(); updateBoss(true); }
        SoundFx.play(this, SoundFx.Kind.WIN);
        showResult(true, "\u2705 SUCCESS: \u201C" + level.success + "\u201D",
                starText(stars) + "\nPerfect syntax! " + correct + "/" + queue.size() + " correct. "
                        + pct + "% of the time left.",
                level.isLast() ? "FINISH" : "CONTINUE", () -> {
                    Intent i = new Intent(this, SceneActivity.class);
                    if (level.isLast()) i.putExtra(SceneActivity.EXTRA_EPILOGUE, true);
                    else i.putExtra(EXTRA_LEVEL, level.id + 1);
                    startActivity(i);
                    finish();
                });
    }

    private void lose(String why) {
        cancelTimer();
        SoundFx.play(this, SoundFx.Kind.LOSE);
        showResult(false, "\u274C FAIL: \u201C" + level.fail + "\u201D", "\uD83D\uDCA1 " + why, "RE-TRY", this::startGame);
    }

    private void endlessOver() {
        cancelTimer();
        int best = Prefs.bestEndless(this);
        boolean record = score > best;
        if (record) Prefs.setBestEndless(this, score);
        int rank = Leaderboard.add(this, score);
        SoundFx.play(this, SoundFx.Kind.LOSE);
        showResult(false, "\u274C GAME OVER: \u201CConnection lost. The Corruptor wins this round...\u201D",
                "Score: " + score + "   Correct: " + correct + "\n"
                        + (record ? "\uD83C\uDFC6 NEW BEST SCORE!" : "Best: " + best)
                        + (rank > 0 ? "\n\uD83E\uDD47 Leaderboard rank #" + rank : ""),
                "RE-TRY", this::startGame);
    }

    private void practiceDone() {
        SoundFx.play(this, SoundFx.Kind.WIN);
        showResult(true, "\u2705 " + getString(R.string.practice_done),
                correct + "/" + queue.size() + " correct. Nice work!", "AGAIN", this::startGame);
    }

    private void reviewDone() {
        SoundFx.play(this, SoundFx.Kind.WIN);
        showResult(true, "\u2705 " + getString(R.string.review_done),
                correct + "/" + queue.size() + " fixed." + (missed.isEmpty() ? " All clear!" : ""),
                "DONE", this::finish);
    }

    private void dailyFinish(boolean timeout) {
        cancelTimer();
        handler.removeCallbacksAndMessages(null);
        if (timeout) {
            SoundFx.play(this, SoundFx.Kind.LOSE);
            showResult(false, "\u23F0 " + getString(R.string.daily_timeout),
                    "You answered " + Math.min(index, queue.size()) + "/" + queue.size()
                            + " in time. Finish all of them to keep your streak.", "RE-TRY", this::startGame);
            return;
        }
        boolean first = !Prefs.dailyDoneToday(this);
        Prefs.completeDaily(this, correct);
        SoundFx.play(this, SoundFx.Kind.WIN);
        showResult(true, "\u2705 " + getString(R.string.daily_done),
                "Score: " + correct + "/" + queue.size() + "\n\uD83D\uDD25 Daily streak: " + Prefs.dailyStreak(this)
                        + (first ? "" : "\n(Streak was already counted today.)"),
                "REPLAY", this::startGame);
    }

    private void showResult(boolean success, String title, String detail, String primaryLabel, Runnable primary) {
        state = State.RESULT;
        hideKeyboard();
        playGroup.setVisibility(View.GONE);
        resultGroup.setVisibility(View.VISIBLE);
        resultText.setText(title);
        resultText.setBackgroundResource(success ? R.drawable.panel_green : R.drawable.panel_red);
        explainText.setText(detail);
        heroResult.setUnmasked(!success);
        if (success) heroResult.bounce();

        btnPrimary.setBackgroundResource(success ? R.drawable.btn_yellow : R.drawable.btn_blue);
        btnPrimary.setText(primaryLabel);
        btnPrimary.setOnClickListener(v -> primary.run());

        // new trophies
        List<Achievements.Def> fresh = Achievements.evaluate(this);
        if (fresh.isEmpty()) badgeText.setVisibility(View.GONE);
        else {
            StringBuilder sb = new StringBuilder();
            for (Achievements.Def d : fresh) {
                if (sb.length() > 0) sb.append("<br>");
                sb.append(getString(R.string.new_trophy, "<b>" + d.title + "</b>")).append("<br><small>").append(d.desc).append("</small>");
            }
            badgeText.setText(html(sb.toString()));
            badgeText.setVisibility(View.VISIBLE);
        }

        // review missed questions
        Set<Integer> ids = new LinkedHashSet<>();
        for (Question q : missed) ids.add(q.id);
        if (ids.isEmpty()) btnReview.setVisibility(View.GONE);
        else {
            final int[] arr = new int[ids.size()];
            int k = 0;
            for (int id : ids) arr[k++] = id;
            btnReview.setText(getString(R.string.review_missed, arr.length));
            btnReview.setVisibility(View.VISIBLE);
            btnReview.setOnClickListener(v -> startActivity(new Intent(this, ReviewActivity.class)
                    .putExtra(EXTRA_IDS, arr)));
        }
    }

    // ---------------------------------------------------------------- timer
    private void beginTimer(long totalMs) {
        barTotal = totalMs;
        runTimer(totalMs);
    }

    private void runTimer(long ms) {
        cancelTimer();
        remainingMs = ms;
        if (dialogOpen) return;
        timer = new CountDownTimer(ms, 100) {
            @Override public void onTick(long left) {
                remainingMs = left;
                timerBar.setProgress((int) (left * 1000 / Math.max(1, barTotal)));
                timerText.setText(((left + 999) / 1000) + "s");
                long threshold = Math.min(Config.warnSeconds(GameActivity.this) * 1000L, barTotal / 3);
                setWarn(left <= threshold);
            }
            @Override public void onFinish() {
                remainingMs = 0;
                timerBar.setProgress(0);
                timerText.setText("0s");
                onTimeUp();
            }
        }.start();
    }

    private void cancelTimer() {
        if (timer != null) { timer.cancel(); timer = null; }
        setWarn(false);
    }

    /** Low-time warning: red bar that pulses, red number and a worried hero. */
    private void setWarn(boolean on) {
        if (warnActive == on) return;
        warnActive = on;
        int p = timerBar.getProgress();
        timerBar.setProgressDrawable(getDrawable(on ? R.drawable.bar_hp_warn : R.drawable.bar_hp));
        timerBar.setProgress(p);
        timerText.setTextColor(getColor(on ? R.color.warn_red : R.color.white));
        if (on) {
            pulse = ObjectAnimator.ofFloat(timerBar, View.ALPHA, 1f, 0.45f);
            pulse.setDuration(350);
            pulse.setRepeatCount(ValueAnimator.INFINITE);
            pulse.setRepeatMode(ValueAnimator.REVERSE);
            pulse.start();
            if (state == State.PLAYING && !locked) heroPlay.setMood(HeroView.Mood.WORRIED);
        } else {
            if (pulse != null) { pulse.cancel(); pulse = null; }
            timerBar.setAlpha(1f);
            if (state == State.PLAYING && !locked) heroPlay.setMood(HeroView.Mood.NORMAL);
        }
    }

    // ---------------------------------------------------------------- keyboard
    private void showKeyboard() {
        typeInput.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && typeInput.isEnabled()) imm.showSoftInput(typeInput, InputMethodManager.SHOW_IMPLICIT);
        }, 150);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(typeInput.getWindowToken(), 0);
    }

    // ---------------------------------------------------------------- pause
    private void showPause() {
        if (state != State.PLAYING || locked || dialogOpen) return;
        dialogOpen = true;
        hideKeyboard();
        cancelTimer();
        pauseDialog = new Dialog(this);
        pauseDialog.setContentView(R.layout.dialog_pause);
        pauseDialog.setCancelable(false);
        if (pauseDialog.getWindow() != null) {
            pauseDialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        }
        TextView resume = pauseDialog.findViewById(R.id.btnResume);
        TextView quit = pauseDialog.findViewById(R.id.btnQuitGame);
        Ui.pressable(resume); Ui.pressable(quit);
        resume.setOnClickListener(v -> {
            pauseDialog.dismiss();
            dialogOpen = false;
            if (remainingMs > 0 && state == State.PLAYING) runTimer(remainingMs);
        });
        quit.setOnClickListener(v -> { pauseDialog.dismiss(); dialogOpen = false; finish(); });
        pauseDialog.show();
    }

    @Override protected void onPause() {
        super.onPause();
        if (state == State.PLAYING && !locked) showPause();
        else if (state == State.PLAYING) cancelTimer();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (timer != null) { timer.cancel(); timer = null; }
        if (pulse != null) { pulse.cancel(); pulse = null; }
        if (pauseDialog != null && pauseDialog.isShowing()) pauseDialog.dismiss();
    }

    @SuppressWarnings("deprecation")
    private static CharSequence html(String s) {
        return Html.fromHtml(s, Html.FROM_HTML_MODE_LEGACY);
    }
}
