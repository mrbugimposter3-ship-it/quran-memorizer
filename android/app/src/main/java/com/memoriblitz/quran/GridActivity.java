package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
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

/** GRID: classic memory game — flip cards to match Arabic with its meaning. */
public class GridActivity extends AppCompatActivity {

    private static class Card {
        int global;
        boolean arabic;
        String text;
        Button btn;
        boolean matched;
    }

    private final Random rnd = new Random();
    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView;
    private LinearLayout grid;

    private List<Card> cards;
    private Card first;
    private boolean locked;
    private int score, lives, pairsLeft;

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

        refView = Ui.title(this, "Flip two cards to match Arabic with its meaning", 14, R.color.text_secondary);
        root.addView(refView);
        root.addView(Ui.vspace(this, 16));

        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        ScrollView gridScroll = new ScrollView(this);
        gridScroll.addView(grid);
        gridScroll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        root.addView(gridScroll);

        setContentView(root);

        score = 0; lives = 3;
        newRound();
    }

    private void newRound() {
        if (study.size() < 2) { finish(); return; }
        int n = Math.min(4, study.size() / 2);
        List<Ayah> picked = new ArrayList<>();
        while (picked.size() < n) {
            Ayah a = study.get(rnd.nextInt(study.size()));
            if (!picked.contains(a)) picked.add(a);
        }

        cards = new ArrayList<>();
        for (Ayah a : picked) {
            Card ar = new Card();
            ar.global = a.global; ar.arabic = true; ar.text = a.arabic;
            cards.add(ar);
            Card en = new Card();
            en.global = a.global; en.arabic = false; en.text = a.english;
            cards.add(en);
        }
        Collections.shuffle(cards, rnd);
        first = null;
        locked = false;
        pairsLeft = n;
        renderGrid();
    }

    private void renderGrid() {
        grid.removeAllViews();
        LinearLayout row = null;
        for (int i = 0; i < cards.size(); i++) {
            if (i % 4 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            final Card c = cards.get(i);
            Button b = Ui.ghost(this, "?", R.color.cream, new View.OnClickListener() {
                @Override public void onClick(View v) { tap(c); }
            });
            c.btn = b;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(90), 1f);
            b.setLayoutParams(lp);
            row.addView(b);
            if (i % 4 == 3) grid.addView(Ui.vspace(this, 10));
        }
        refreshCards();
    }

    private void refreshCards() {
        for (Card c : cards) {
            if (c.matched) {
                c.btn.setText(c.text);
                c.btn.setBackground(Ui.pill(this, R.color.leaf, 12));
                c.btn.setTextColor(Ui.color(this, R.color.espresso));
            } else if (c == first) {
                c.btn.setText(c.text);
                c.btn.setBackground(Ui.pill(this, R.color.cream, 12));
                c.btn.setTextColor(Ui.color(this, R.color.espresso));
            } else {
                c.btn.setText("?");
                c.btn.setBackground(Ui.outline(this, R.color.cream, 12));
                c.btn.setTextColor(Ui.color(this, R.color.text_primary));
            }
        }
    }

    private void tap(Card c) {
        if (locked || c.matched || c == first) return;
        if (first == null) {
            first = c;
            refreshCards();
            return;
        }
        // second card
        locked = true;
        c.btn.setText(c.text);
        c.btn.setBackground(Ui.pill(this, R.color.cream, 12));
        c.btn.setTextColor(Ui.color(this, R.color.espresso));

        if (first.global == c.global && first.arabic != c.arabic) {
            first.matched = true;
            c.matched = true;
            first = null;
            locked = false;
            score += 100;
            scoreView.setText("SCORE " + score);
            pairsLeft--;
            if (pairsLeft <= 0) {
                for (Card x : cards) store.record(quran.ayahs.get(x.global), true);
                newRound();
                return;
            }
            refreshCards();
        } else {
            lives--;
            store.record(quran.ayahs.get(c.global), false);
            livesView.setText("LIVES " + lives);
            final Card f = first;
            final Card s = c;
            handler.postDelayed(new Runnable() {
                @Override public void run() {
                    first = null;
                    locked = false;
                    refreshCards();
                    if (lives <= 0) { store.submitScore("grid", score); finish(); }
                }
            }, 700);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
