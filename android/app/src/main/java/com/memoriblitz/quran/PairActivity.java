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

/** PAIR: match Arabic phrases to their transliterations. */
public class PairActivity extends AppCompatActivity {

    private static class Item {
        int global;
        boolean arabic;
        String text;
        Button btn;
        boolean done;
    }

    private final Random rnd = new Random();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView;
    private LinearLayout grid;

    private List<Item> items;
    private Item selected;
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

        refView = Ui.title(this, "Tap an Arabic phrase, then its transliteration", 14, R.color.text_secondary);
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

        items = new ArrayList<>();
        for (Ayah a : picked) {
            Item ar = new Item();
            ar.global = a.global; ar.arabic = true; ar.text = a.arabic;
            items.add(ar);
            Item tr = new Item();
            tr.global = a.global; tr.arabic = false; tr.text = a.translit;
            items.add(tr);
        }
        Collections.shuffle(items, rnd);
        selected = null;
        pairsLeft = n;
        renderGrid();
    }

    private void renderGrid() {
        grid.removeAllViews();
        LinearLayout row = null;
        for (int i = 0; i < items.size(); i++) {
            if (i % 2 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                grid.addView(row);
            }
            final Item it = items.get(i);
            Button b = Ui.ghost(this, it.text, R.color.cream, new View.OnClickListener() {
                @Override public void onClick(View v) { tap(it); }
            });
            it.btn = b;
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            b.setLayoutParams(lp);
            row.addView(b);
            if (i % 2 == 1) grid.addView(Ui.vspace(this, 10));
        }
        refreshButtons();
    }

    private void refreshButtons() {
        for (Item it : items) {
            if (it.done) {
                it.btn.setBackground(Ui.pill(this, R.color.leaf, 16));
                it.btn.setTextColor(Ui.color(this, R.color.espresso));
            } else if (it == selected) {
                it.btn.setBackground(Ui.pill(this, R.color.cream, 16));
                it.btn.setTextColor(Ui.color(this, R.color.espresso));
            } else {
                it.btn.setBackground(Ui.outline(this, R.color.cream, 16));
                it.btn.setTextColor(Ui.color(this, R.color.text_primary));
            }
        }
    }

    private void tap(Item it) {
        if (it.done) return;
        if (selected == null) {
            selected = it;
            refreshButtons();
        } else if (selected == it) {
            selected = null;
            refreshButtons();
        } else {
            if (selected.global == it.global && selected.arabic != it.arabic) {
                selected.done = true;
                it.done = true;
                selected = null;
                score += 100;
                scoreView.setText("SCORE " + score);
                pairsLeft--;
                if (pairsLeft <= 0) {
                    for (Item x : items) store.record(quran.ayahs.get(x.global), true);
                    newRound();
                    return;
                }
                refreshButtons();
            } else {
                lives--;
                store.record(quran.ayahs.get(it.global), false);
                livesView.setText("LIVES " + lives);
                selected = null;
                refreshButtons();
                if (lives <= 0) { store.submitScore("pair", score); finish(); }
            }
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
