package com.memoriblitz.quran;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.List;
import java.util.Random;

public class FlashActivity extends AppCompatActivity {

    private final Random rnd = new Random();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView, arabicView, translitView, meaningView;
    private Button revealBtn, gotBtn, missBtn;

    private Ayah current;
    private boolean revealed;
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
        hud.addView(Ui.hspace(this, 20));
        hud.addView(livesView);
        root.addView(hud);
        root.addView(Ui.quranPeek(this));
        root.addView(Ui.vspace(this, 12));

        refView = Ui.title(this, "", store.q(13), R.color.cream);
        root.addView(refView);
        root.addView(Ui.vspace(this, 8));

        ScrollView scroll = new ScrollView(this);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(18), dp(18), dp(18));
        card.setBackground(Ui.pill(this, R.color.card, 16));

        TextView aLabel = Ui.title(this, "ARABIC", 11, R.color.text_secondary);
        card.addView(aLabel);
        arabicView = Ui.title(this, "", store.q(22), R.color.cream);
        arabicView.setTextDirection(View.TEXT_DIRECTION_RTL);
        card.addView(arabicView);
        card.addView(Ui.vspace(this, 10));

        TextView tLabel = Ui.title(this, "TRANSLITERATION", 11, R.color.text_secondary);
        card.addView(tLabel);
        translitView = Ui.title(this, "", store.q(18), R.color.text_primary);
        card.addView(translitView);
        card.addView(Ui.vspace(this, 14));

        TextView mLabel = Ui.title(this, "MEANING", 11, R.color.text_secondary);
        card.addView(mLabel);
        meaningView = Ui.title(this, "", store.q(15), R.color.cream);
        card.addView(meaningView);

        scroll.addView(card);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(Ui.vspace(this, 14));

        revealBtn = Ui.button(this, "REVEAL MEANING", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { reveal(); }
        });
        root.addView(revealBtn);

        LinearLayout gradeRow = new LinearLayout(this);
        gradeRow.setOrientation(LinearLayout.HORIZONTAL);
        gotBtn = Ui.button(this, "GOT IT", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { grade(true); }
        });
        missBtn = Ui.ghost(this, "MISS", R.color.warm_red, new View.OnClickListener() {
            @Override public void onClick(View v) { grade(false); }
        });
        gradeRow.addView(gotBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        gradeRow.addView(Ui.hspace(this, 10));
        gradeRow.addView(missBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(gradeRow);

        setContentView(root);

        score = 0; lives = 3;
        nextCard();
    }

    private void nextCard() {
        current = adaptivePick();
        revealed = false;
        refView.setText(current.fullRef());
        arabicView.setText(current.arabic);
        translitView.setText(current.translit);
        meaningView.setText("think it through, then reveal");
        revealBtn.setVisibility(View.VISIBLE);
        gotBtn.setVisibility(View.GONE);
        missBtn.setVisibility(View.GONE);
    }

    private Ayah adaptivePick() {
        if (study.isEmpty()) return null;
        double total = 0;
        for (Ayah a : study) total += weight(a);
        double t = rnd.nextDouble() * total;
        for (Ayah a : study) {
            t -= weight(a);
            if (t <= 0) return a;
        }
        return study.get(study.size() - 1);
    }

    private double weight(Ayah a) {
        int[] p = store.progress(a.global);
        double w = 1.0 + p[1] * 2.2 - Math.min(p[0], 6) * 0.28;
        return w < 0.35 ? 0.35 : w;
    }

    private void reveal() {
        if (current == null) return;
        revealed = true;
        meaningView.setText(current.english);
        revealBtn.setVisibility(View.GONE);
        gotBtn.setVisibility(View.VISIBLE);
        missBtn.setVisibility(View.VISIBLE);
    }

    private void grade(boolean gotIt) {
        if (current == null) return;
        if (!revealed) { reveal(); return; }
        if (gotIt) {
            score += 100;
            store.record(current, true);
        } else {
            lives--;
            store.record(current, false);
            if (lives <= 0) {
                store.submitScore("flash", score);
                finish();
                return;
            }
        }
        scoreView.setText("SCORE " + score);
        livesView.setText("LIVES " + lives);
        nextCard();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
