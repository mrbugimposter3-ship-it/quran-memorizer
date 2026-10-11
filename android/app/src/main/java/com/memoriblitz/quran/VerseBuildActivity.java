package com.memoriblitz.quran;

import android.os.Bundle;
import android.view.View;
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

/** VERSE BUILD: build the ayah word-by-word from a pool (with distractors). */
public class VerseBuildActivity extends AppCompatActivity {

    private final Random rnd = new Random();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView, promptView, builtView;
    private LinearLayout bank;

    private Ayah target;
    private String[] words;
    private List<String> pool;
    private StringBuilder built;
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

        promptView = Ui.title(this, "", store.q(16), R.color.text_secondary);
        root.addView(promptView);
        root.addView(Ui.vspace(this, 8));

        builtView = Ui.title(this, "", store.q(18), R.color.emerald);
        root.addView(builtView);
        root.addView(Ui.vspace(this, 14));

        bank = new LinearLayout(this);
        bank.setOrientation(LinearLayout.VERTICAL);
        ScrollView bankScroll = new ScrollView(this);
        bankScroll.addView(bank);
        bankScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(bankScroll);

        setContentView(root);

        score = 0; lives = 3;
        nextCard();
    }

    private void nextCard() {
        target = study.isEmpty() ? null : study.get(rnd.nextInt(study.size()));
        built = new StringBuilder();
        builtView.setText("");
        if (target != null) {
            refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
            promptView.setText(target.english);
            words = target.translit.split("\\s+");
            pool = new ArrayList<>();
            Collections.addAll(pool, words);
            // add distractors
            int need = Math.min(6, words.length + 4);
            while (pool.size() < need) {
                Ayah a = study.get(rnd.nextInt(study.size()));
                String[] aw = a.translit.split("\\s+");
                if (aw.length == 0) continue;
                String w = aw[rnd.nextInt(aw.length)];
                if (!pool.contains(w)) pool.add(w);
            }
            Collections.shuffle(pool, rnd);
        }
        renderBank();
    }

    private void renderBank() {
        bank.removeAllViews();
        for (final String w : pool) {
            Button b = Ui.ghost(this, w, R.color.cream, new View.OnClickListener() {
                @Override public void onClick(View v) { tap(w); }
            });
            bank.addView(b);
            bank.addView(Ui.vspace(this, 8));
        }
    }

    private int wordIndex() {
        return built.length() == 0 ? 0 : built.toString().split("\\s+").length;
    }

    private void tap(String w) {
        if (target == null) return;
        String expect = words[wordIndex()];
        if (w.equals(expect)) {
            if (built.length() > 0) built.append(' ');
            built.append(w);
            builtView.setText(built.toString());
            if (wordIndex() == words.length) {
                score += 100;
                store.record(target, true);
                scoreView.setText("SCORE " + score);
                nextCard();
            }
        } else {
            lives--;
            store.record(target, false);
            livesView.setText("LIVES " + lives);
            if (lives <= 0) { store.submitScore("versebuild", score); finish(); }
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
