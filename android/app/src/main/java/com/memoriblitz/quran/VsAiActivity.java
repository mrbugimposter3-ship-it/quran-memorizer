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

/** VS AI: race the clock on ayah metadata (surah / page / juz / ayah number). */
public class VsAiActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, aiView, timeView, refView, promptView;
    private Button[] opts = new Button[4];

    private Ayah target;
    private int answerIdx;
    private int score, aiScore, timeLeft;
    private boolean running;

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
        scoreView = Ui.label(this, "YOU 0", 15, R.color.cream);
        aiView = Ui.label(this, "AI 0", 15, R.color.warm_red);
        timeView = Ui.label(this, "60s", 15, R.color.leaf);
        hud.addView(scoreView);
        hud.addView(Ui.hspace(this, 16));
        hud.addView(aiView);
        hud.addView(Ui.hspace(this, 16));
        hud.addView(timeView);
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

        score = 0; aiScore = 0; timeLeft = 60; running = true;
        newQuestion();
        startClock();
    }

    private void startClock() {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!running) return;
                timeLeft--;
                aiScore += 15;
                timeView.setText(timeLeft + "s");
                aiView.setText("AI " + aiScore);
                if (timeLeft <= 0) {
                    store.submitScore("vsai", score);
                    finish();
                    return;
                }
                startClock();
            }
        }, 1000);
    }

    private void newQuestion() {
        if (study.isEmpty()) { finish(); return; }
        target = study.get(rnd.nextInt(study.size()));
        int type = rnd.nextInt(4);
        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        promptView.setText(target.arabic + "\n" + target.translit);

        List<String> pool = new ArrayList<>();
        String correct;
        switch (type) {
            case 0: // surah
                correct = quran.surah(target.surah).english;
                while (pool.size() < 3) {
                    int s = 1 + rnd.nextInt(quran.surahs.size());
                    String n = quran.surah(s).english;
                    if (!n.equals(correct) && !pool.contains(n)) pool.add(n);
                }
                break;
            case 1: // page
                correct = "Page " + target.page;
                while (pool.size() < 3) {
                    int p = 1 + rnd.nextInt(604);
                    String n = "Page " + p;
                    if (!n.equals(correct) && !pool.contains(n)) pool.add(n);
                }
                break;
            case 2: // juz
                correct = "Juz " + target.juz;
                while (pool.size() < 3) {
                    int j = 1 + rnd.nextInt(30);
                    String n = "Juz " + j;
                    if (!n.equals(correct) && !pool.contains(n)) pool.add(n);
                }
                break;
            default: // ayah number
                correct = "Ayah " + target.ayah;
                while (pool.size() < 3) {
                    int a = 1 + rnd.nextInt(286);
                    String n = "Ayah " + a;
                    if (!n.equals(correct) && !pool.contains(n)) pool.add(n);
                }
                break;
        }
        pool.add(correct);
        Collections.shuffle(pool, rnd);
        answerIdx = pool.indexOf(correct);

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            opts[i].setText(pool.get(i));
            opts[i].setBackground(Ui.outline(this, R.color.cream, 16));
            opts[i].setTextColor(Ui.color(this, R.color.text_primary));
            opts[i].setEnabled(true);
            opts[i].setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { answer(idx); }
            });
        }
    }

    private void answer(int idx) {
        if (!running) return;
        for (int i = 0; i < 4; i++) opts[i].setEnabled(false);
        if (idx == answerIdx) {
            score += 100;
            store.record(target, true);
            opts[idx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.espresso));
        } else {
            store.record(target, false);
            opts[idx].setBackground(Ui.pill(this, R.color.warm_red, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.white));
            opts[answerIdx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[answerIdx].setTextColor(Ui.color(this, R.color.espresso));
        }
        scoreView.setText("YOU " + score);
        handler.postDelayed(new Runnable() {
            @Override public void run() { newQuestion(); }
        }, 400);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
