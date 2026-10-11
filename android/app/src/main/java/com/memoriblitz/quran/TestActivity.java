package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, promptView, refView;
    private Button[] opts = new Button[4];

    private Ayah target;
    private boolean askPhrase;
    private int answerIdx;
    private int score, combo, lives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);
        study = store.study(quran);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, dp(18), pad, dp(18));

        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.HORIZONTAL);
        scoreView = Ui.label(this, "SCORE 0", 15, R.color.cream);
        livesView = Ui.label(this, "LIVES 3", 15, R.color.leaf);
        hud.addView(scoreView);
        hud.addView(Ui.hspace(this, 24));
        hud.addView(livesView);
        root.addView(hud);
        root.addView(Ui.quranPeek(this));

        root.addView(space(14));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);

        promptView = Ui.title(this, "", store.q(19), R.color.text_primary);
        root.addView(promptView);

        root.addView(space(18));

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            opts[i] = Ui.ghost(this, "", R.color.emerald, new View.OnClickListener() {
                @Override public void onClick(View v) { answer(idx); }
            });
            opts[i].setAllCaps(false);
            opts[i].setTextSize(store.q(15));
            opts[i].setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            opts[i].setPadding(dp(18), dp(14), dp(18), dp(14));
            root.addView(opts[i]);
            root.addView(space(10));
        }

        setContentView(root);

        score = 0; combo = 0; lives = 3;
        newQuestion();
    }

    private void newQuestion() {
        if (study.isEmpty()) { finish(); return; }
        target = study.get(rnd.nextInt(study.size()));
        askPhrase = rnd.nextBoolean();

        String right;
        List<String> wrong = new ArrayList<>();
        if (askPhrase) {
            right = target.english;
            wrong.addAll(distractors(right, true, 3));
        } else {
            right = target.translit;
            wrong.addAll(distractors(right, false, 3));
        }
        List<String> pool = new ArrayList<>();
        pool.add(right);
        pool.addAll(wrong);
        Collections.shuffle(pool, rnd);

        for (int i = 0; i < 4; i++) {
            opts[i].setText(i < pool.size() ? pool.get(i) : "");
            opts[i].setEnabled(true);
            opts[i].setBackground(Ui.outline(this, R.color.emerald, 16));
            opts[i].setTextColor(Ui.color(this, R.color.emerald));
        }
        answerIdx = pool.indexOf(right);
        if (answerIdx < 0) answerIdx = 0;

        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        String phrase = askPhrase
                ? (rnd.nextDouble() < 0.35 ? ProgressStore.partial(target.translit, rnd) : target.translit)
                : target.english;
        promptView.setText(askPhrase ? "WHAT DOES THIS MEAN?\n" + phrase
                                     : "WHICH AYAH IS THIS?\n" + phrase);
    }

    private List<String> distractors(String answer, boolean meanings, int n) {
        List<String> pool = new ArrayList<>();
        for (Ayah a : study) {
            String m = meanings ? a.english : a.translit;
            if (m == null || m.trim().isEmpty()) continue;
            if (!pool.contains(m) && !m.equalsIgnoreCase(answer)) pool.add(m);
        }
        Collections.shuffle(pool, rnd);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    private void answer(int idx) {
        if (idx < 0 || idx >= 4) return;
        boolean right = idx == answerIdx;
        for (int i = 0; i < 4; i++) opts[i].setEnabled(false);
        if (right) {
            combo++;
            int mult = Math.min(8, 1 + combo / 4);
            score += 100 * mult;
            store.record(target, true);
            opts[idx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.espresso));
        } else {
            combo = 0;
            lives--;
            store.record(target, false);
            opts[idx].setBackground(Ui.pill(this, R.color.warm_red, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.white));
            opts[answerIdx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[answerIdx].setTextColor(Ui.color(this, R.color.espresso));
        }
        updateHud();
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (lives <= 0) {
                    store.submitScore("test", score);
                    finish();
                } else {
                    newQuestion();
                }
            }
        }, right ? 350 : 900);
    }

    private void updateHud() {
        scoreView.setText("SCORE " + score);
        livesView.setText("LIVES " + lives);
    }

    private View space(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(dp)));
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
