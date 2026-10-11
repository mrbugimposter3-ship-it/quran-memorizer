package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** REVEAL: an ayah's Arabic is revealed word by word - recall it early for more points. */
public class RevealActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, refView, arView, progressView;

    private Ayah target;
    private String[] words;
    private boolean[] revealed;
    private int revealedCount;
    private int score;

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
        TextView hint = Ui.label(this, "REVEAL", 15, R.color.leaf);
        hud.addView(scoreView);
        hud.addView(Ui.hspace(this, 24));
        hud.addView(hint);
        root.addView(hud);
        root.addView(Ui.quranPeek(this));
        root.addView(Ui.vspace(this, 12));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);
        root.addView(Ui.vspace(this, 20));

        arView = Ui.label(this, "", store.q(26), R.color.cream);
        arView.setGravity(Gravity.CENTER);
        arView.setTextDirection(View.TEXT_DIRECTION_RTL);
        root.addView(arView);
        root.addView(Ui.vspace(this, 20));

        progressView = Ui.title(this, "", store.q(13), R.color.text_secondary);
        root.addView(progressView);
        root.addView(Ui.vspace(this, 16));

        Button recall = Ui.button(this, "I CAN RECALL IT", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { recall(); }
        });
        root.addView(recall);

        setContentView(root);

        score = 0;
        nextCard();
    }

    private void nextCard() {
        handler.removeCallbacksAndMessages(null);
        if (study.isEmpty()) { store.submitScore("reveal", score); finish(); return; }
        target = study.get(rnd.nextInt(study.size()));
        words = target.arabic.split("\\s+");
        revealed = new boolean[words.length];
        revealedCount = 0;
        if (words.length > 0) revealRandom();
        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        render();
        scheduleReveal();
    }

    private void revealRandom() {
        List<Integer> hidden = new ArrayList<>();
        for (int i = 0; i < words.length; i++) if (!revealed[i]) hidden.add(i);
        if (hidden.isEmpty()) return;
        revealed[hidden.get(rnd.nextInt(hidden.size()))] = true;
        revealedCount++;
    }

    private void scheduleReveal() {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (target == null) return;
                if (revealedCount < words.length) {
                    revealRandom();
                    render();
                    scheduleReveal();
                } else {
                    score += 20;
                    store.record(target, false);
                    scoreView.setText("SCORE " + score);
                    nextCard();
                }
            }
        }, 1300);
    }

    private void render() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            if (revealed[i]) sb.append(words[i]);
            else sb.append(mask(words[i]));
        }
        arView.setText(sb.toString());
        progressView.setText(revealedCount + "/" + words.length + " words revealed");
    }

    private String mask(String w) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length(); i++) sb.append('\u00b7');
        return sb.toString();
    }

    private void recall() {
        if (target == null) return;
        double frac = words.length == 0 ? 1.0 : (double) revealedCount / words.length;
        int g = (int) Math.round((1.0 - frac) * 150) + 20;
        score += g;
        store.record(target, true);
        scoreView.setText("SCORE " + score);
        nextCard();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
