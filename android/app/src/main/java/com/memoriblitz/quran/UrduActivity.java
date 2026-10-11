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

/** URDU: see the Arabic, choose the correct Urdu meaning. */
public class UrduActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView, promptView;
    private Button[] opts = new Button[4];

    private Ayah target;
    private int answerIdx;
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

        promptView = Ui.title(this, "", store.q(22), R.color.text_primary);
        promptView.setTextDirection(View.TEXT_DIRECTION_RTL);
        root.addView(promptView);
        root.addView(Ui.vspace(this, 16));

        for (int i = 0; i < 4; i++) {
            opts[i] = Ui.ghost(this, "", R.color.cream, null);
            opts[i].setTextSize(store.q(15));
            root.addView(opts[i]);
            root.addView(Ui.vspace(this, 10));
        }

        setContentView(root);

        score = 0; lives = 3;
        newQuestion();
    }

    private void newQuestion() {
        if (study.isEmpty()) { finish(); return; }
        target = study.get(rnd.nextInt(study.size()));
        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        promptView.setText(target.arabic);

        String right = target.urdu == null ? target.english : target.urdu;
        List<String> pool = new ArrayList<>();
        pool.add(right);
        while (pool.size() < 4) {
            Ayah a = study.get(rnd.nextInt(study.size()));
            String m = a.urdu == null ? a.english : a.urdu;
            if (!pool.contains(m)) pool.add(m);
        }
        Collections.shuffle(pool, rnd);
        answerIdx = pool.indexOf(right);

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
        for (int i = 0; i < 4; i++) opts[i].setEnabled(false);
        if (idx == answerIdx) {
            score += 100;
            store.record(target, true);
            opts[idx].setBackground(Ui.pill(this, R.color.leaf, 16));
            opts[idx].setTextColor(Ui.color(this, R.color.espresso));
        } else {
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
                if (lives <= 0) { store.submitScore("urdu", score); finish(); }
                else newQuestion();
            }
        }, 500);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
