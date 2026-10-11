package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FillActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView, promptView;
    private Button[] opts = new Button[4];

    private Ayah target;
    private String[] words;
    private int blankIdx;
    private String correctWord;
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
        root.addView(Ui.vspace(this, 12));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);

        ScrollView scroll = new ScrollView(this);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(Ui.pill(this, R.color.card, 16));
        promptView = Ui.title(this, "", store.q(18), R.color.text_primary);
        card.addView(promptView);
        scroll.addView(card);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(Ui.vspace(this, 14));

        TextView q = Ui.title(this, "WHICH WORD IS MISSING?", 12, R.color.text_secondary);
        root.addView(q);
        root.addView(Ui.vspace(this, 8));

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            opts[i] = Ui.ghost(this, "", R.color.terracotta, new View.OnClickListener() {
                @Override public void onClick(View v) { answer(idx); }
            });
            opts[i].setAllCaps(false);
            opts[i].setTextSize(store.q(15));
            opts[i].setGravity(android.view.Gravity.START | android.view.Gravity.CENTER_VERTICAL);
            opts[i].setPadding(dp(18), dp(12), dp(18), dp(12));
            root.addView(opts[i]);
            root.addView(Ui.vspace(this, 10));
        }

        setContentView(root);

        score = 0; combo = 0; lives = 3;
        newQuestion();
    }

    private void newQuestion() {
        if (study.isEmpty()) { finish(); return; }
        target = null;
        for (int attempt = 0; attempt < 40; attempt++) {
            Ayah a = study.get(rnd.nextInt(study.size()));
            String[] w = a.translit.trim().split("\\s+");
            if (w.length >= 3) {
                target = a;
                words = w;
                blankIdx = rnd.nextInt(w.length);
                correctWord = w[blankIdx];
                break;
            }
        }
        if (target == null) { finish(); return; }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            if (i == blankIdx) sb.append("______");
            else sb.append(words[i]);
        }
        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        promptView.setText(sb.toString());

        List<String> pool = new ArrayList<>();
        for (Ayah a : study) {
            if (a == target) continue;
            for (String w : a.translit.trim().split("\\s+")) {
                if (!pool.contains(w) && !w.equalsIgnoreCase(correctWord)) pool.add(w);
            }
        }
        Collections.shuffle(pool, rnd);
        List<String> wrong = new ArrayList<>();
        for (int i = 0; i < pool.size() && wrong.size() < 3; i++) wrong.add(pool.get(i));

        List<String> optsList = new ArrayList<>();
        optsList.add(correctWord);
        optsList.addAll(wrong);
        Collections.shuffle(optsList, rnd);

        for (int i = 0; i < 4; i++) {
            opts[i].setText(i < optsList.size() ? optsList.get(i) : "");
            opts[i].setEnabled(true);
            opts[i].setBackground(Ui.outline(this, R.color.terracotta, 16));
            opts[i].setTextColor(Ui.color(this, R.color.terracotta));
        }
        answerIdx = optsList.indexOf(correctWord);
        if (answerIdx < 0) answerIdx = 0;
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
        scoreView.setText("SCORE " + score);
        livesView.setText("LIVES " + lives);
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (lives <= 0) {
                    store.submitScore("fill", score);
                    finish();
                } else {
                    newQuestion();
                }
            }
        }, right ? 350 : 900);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
