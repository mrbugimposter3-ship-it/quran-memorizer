package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.List;
import java.util.Random;

/** SPEED: rapid-fire true/false on translation accuracy, against the clock. */
public class SpeedActivity extends AppCompatActivity {

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, timeView, refView, promptView, transView;

    private Ayah target;
    private boolean shownCorrect;
    private int score, timeLeft;
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
        scoreView = Ui.label(this, "SCORE 0", 15, R.color.cream);
        timeView = Ui.label(this, "60s", 15, R.color.leaf);
        hud.addView(scoreView);
        hud.addView(Ui.hspace(this, 24));
        hud.addView(timeView);
        root.addView(hud);
        root.addView(Ui.quranPeek(this));
        root.addView(Ui.vspace(this, 12));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);
        root.addView(Ui.vspace(this, 8));

        promptView = Ui.title(this, "", store.q(22), R.color.text_primary);
        root.addView(promptView);
        root.addView(Ui.vspace(this, 8));

        transView = Ui.title(this, "", store.q(16), R.color.text_secondary);
        root.addView(transView);
        root.addView(Ui.vspace(this, 18));

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button yes = Ui.button(this, "CORRECT", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { judge(true); }
        });
        Button no = Ui.button(this, "WRONG", R.color.warm_red, new View.OnClickListener() {
            @Override public void onClick(View v) { judge(false); }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        yes.setLayoutParams(lp);
        no.setLayoutParams(lp);
        row.addView(yes);
        row.addView(Ui.hspace(this, 12));
        row.addView(no);
        root.addView(row);

        setContentView(root);

        score = 0;
        timeLeft = 60;
        running = true;
        newQuestion();
        startClock();
    }

    private void startClock() {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (!running) return;
                timeLeft--;
                timeView.setText(timeLeft + "s");
                if (timeLeft <= 0) {
                    store.submitScore("speed", score);
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
        refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
        promptView.setText(target.arabic);
        shownCorrect = rnd.nextBoolean();
        if (shownCorrect) {
            transView.setText(target.english);
        } else {
            Ayah a = study.get(rnd.nextInt(study.size()));
            transView.setText(a.english);
        }
    }

    private void judge(boolean saidCorrect) {
        if (!running) return;
        if (saidCorrect == shownCorrect) {
            score += 100;
            store.record(target, true);
        } else {
            store.record(target, false);
        }
        scoreView.setText("SCORE " + score);
        newQuestion();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
