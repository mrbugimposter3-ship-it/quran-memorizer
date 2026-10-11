package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared base for the 4-option multiple-choice game activities. Subclasses
 * implement {@link #generate()} to fill in target / prompt / options.
 */
public abstract class QuizActivity extends AppCompatActivity {

    protected final Random rnd = new Random();
    protected final Handler handler = new Handler();

    protected QuranData quran;
    protected ProgressStore store;
    protected List<Ayah> study;
    protected String gameKey = "quiz";

    private TextView scoreView, livesView, refView, promptView;
    protected final Button[] opts = new Button[4];
    protected Ayah target;
    protected String[] optText = new String[4];
    protected int answerIdx;
    private int score, lives;

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
        root.addView(Ui.vspace(this, 12));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);
        root.addView(Ui.vspace(this, 8));

        promptView = Ui.title(this, "", store.q(18), R.color.text_primary);
        root.addView(promptView);
        root.addView(Ui.vspace(this, 18));

        for (int i = 0; i < 4; i++) {
            opts[i] = Ui.ghost(this, "", R.color.cream, null);
            root.addView(opts[i]);
            root.addView(Ui.vspace(this, 10));
        }

        setContentView(root);
        score = 0;
        lives = 3;
        newQuestion();
    }

    /** Subclass: set target, call prompt(...) and setOptions(...). */
    protected abstract void generate();

    protected void prompt(String text) { promptView.setText(text); }

    protected void setOptions(String right, List<String> wrong) {
        List<String> pool = new ArrayList<>();
        pool.add(right);
        pool.addAll(wrong);
        Collections.shuffle(pool, rnd);
        for (int i = 0; i < 4; i++) optText[i] = pool.get(i);
        answerIdx = pool.indexOf(right);
    }

    /** Up to n distinct strings from source, excluding the answer. */
    protected List<String> distinct(List<String> source, String answer, int n) {
        List<String> pool = new ArrayList<>();
        for (String s : source) {
            if (s != null && !s.trim().isEmpty() && !pool.contains(s) && !s.equals(answer)) pool.add(s);
        }
        Collections.shuffle(pool, rnd);
        List<String> out = new ArrayList<>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    protected List<String> meanings() {
        List<String> l = new ArrayList<>();
        for (Ayah a : study) l.add(a.english);
        return l;
    }

    protected List<String> translits() {
        List<String> l = new ArrayList<>();
        for (Ayah a : study) l.add(a.translit);
        return l;
    }

    protected List<String> urdus() {
        List<String> l = new ArrayList<>();
        for (Ayah a : study) if (a.urdu != null && !a.urdu.trim().isEmpty()) l.add(a.urdu);
        return l;
    }

    protected List<String> arabics() {
        List<String> l = new ArrayList<>();
        for (Ayah a : study) l.add(a.arabic);
        return l;
    }

    protected List<String> arabicWords() {
        List<String> l = new ArrayList<>();
        for (Ayah a : study) {
            for (String w : a.arabic.split("\\s+")) {
                if (w.length() >= 2 && !l.contains(w)) l.add(w);
            }
        }
        return l;
    }

    private void newQuestion() {
        if (study.isEmpty()) { finish(); return; }
        target = null;
        generate();
        refView.setText(target != null ? Emoji.forAyah(target.english) + "  " + target.fullRef() : "");
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            opts[i].setText(optText[i] == null ? "" : optText[i]);
            opts[i].setBackground(Ui.outline(this, R.color.cream, 16));
            opts[i].setTextColor(Ui.color(this, R.color.text_primary));
            opts[i].setEnabled(true);
            opts[i].setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { answer(idx); }
            });
        }
    }

    private void answer(int idx) {
        for (int i = 0; i < 4; i++) opts[i].setEnabled(false);
        if (idx == answerIdx) {
            score += 100;
            if (target != null) store.record(target, true);
            opts[idx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.espresso));
        } else {
            lives--;
            if (target != null) store.record(target, false);
            opts[idx].setBackground(Ui.pill(this, R.color.warm_red, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.white));
            opts[answerIdx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[answerIdx].setTextColor(Ui.color(this, R.color.espresso));
        }
        scoreView.setText("SCORE " + score);
        livesView.setText("LIVES " + lives);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (lives <= 0) { store.submitScore(gameKey, score); finish(); }
                else newQuestion();
            }
        }, 500);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
