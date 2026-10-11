package com.memoriblitz.quran;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MatchActivity extends AppCompatActivity {

    private static final int MAX_PAIRS = 6;

    private final Random rnd = new Random();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, roundView;

    private Ayah[] pairs = new Ayah[MAX_PAIRS];
    private int[] leftOrder = new int[MAX_PAIRS];
    private int[] rightOrder = new int[MAX_PAIRS];
    private boolean[] leftDone = new boolean[MAX_PAIRS];
    private boolean[] rightDone = new boolean[MAX_PAIRS];
    private TextView[] leftTiles = new TextView[MAX_PAIRS];
    private TextView[] rightTiles = new TextView[MAX_PAIRS];

    private int pairCount = 4;
    private int selectedLeft = -1;
    private int score, lives;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);
        study = store.study(quran);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(16), pad, dp(16));

        LinearLayout hud = new LinearLayout(this);
        hud.setOrientation(LinearLayout.HORIZONTAL);
        scoreView = Ui.label(this, "SCORE 0", 15, R.color.cream);
        livesView = Ui.label(this, "LIVES 3", 15, R.color.leaf);
        roundView = Ui.label(this, "PAIRS 4", 15, R.color.text_secondary);
        hud.addView(scoreView);
        hud.addView(Ui.hspace(this, 20));
        hud.addView(livesView);
        hud.addView(Ui.hspace(this, 20));
        hud.addView(roundView);
        root.addView(hud);
        root.addView(Ui.quranPeek(this));
        root.addView(Ui.vspace(this, 12));

        TextView hint = Ui.title(this, "Tap a transliteration, then its meaning", 12, R.color.text_secondary);
        root.addView(hint);
        root.addView(Ui.vspace(this, 10));

        ScrollView scroll = new ScrollView(this);
        LinearLayout cols = new LinearLayout(this);
        cols.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout rightCol = new LinearLayout(this);
        rightCol.setOrientation(LinearLayout.VERTICAL);

        for (int i = 0; i < MAX_PAIRS; i++) {
            final int li = i;
            leftTiles[i] = tile(leftCol, R.color.emerald, new View.OnClickListener() {
                @Override public void onClick(View v) { tapLeft(li); }
            });
            final int ri = i;
            rightTiles[i] = tile(rightCol, R.color.cream, new View.OnClickListener() {
                @Override public void onClick(View v) { tapRight(ri); }
            });
        }

        cols.addView(leftCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        cols.addView(Ui.hspace(this, 10));
        cols.addView(rightCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        scroll.addView(cols);
        root.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);

        score = 0; lives = 3;
        newBoard();
    }

    private TextView tile(LinearLayout col, int colorId, View.OnClickListener l) {
        TextView t = new TextView(this);
        t.setTextSize(store.q(13));
        t.setTextColor(Ui.color(this, colorId));
        t.setPadding(dp(12), dp(12), dp(12), dp(12));
        t.setBackground(Ui.outline(this, colorId, 14));
        t.setOnClickListener(l);
        col.addView(t, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) t.getLayoutParams()).setMargins(0, 0, 0, dp(10));
        return t;
    }

    private void newBoard() {
        if (study.size() < pairCount) pairCount = Math.max(2, study.size());
        List<Ayah> pool = new ArrayList<>(study);
        Collections.shuffle(pool, rnd);
        for (int i = 0; i < pairCount; i++) {
            pairs[i] = pool.get(i);
            leftOrder[i] = i;
            rightOrder[i] = i;
            leftDone[i] = false;
            rightDone[i] = false;
        }
        shuffle(leftOrder, pairCount);
        shuffle(rightOrder, pairCount);
        selectedLeft = -1;
        render();
    }

    private void shuffle(int[] a, int n) {
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    private void render() {
        for (int i = 0; i < MAX_PAIRS; i++) {
            boolean vis = i < pairCount;
            leftTiles[i].setVisibility(vis ? View.VISIBLE : View.GONE);
            rightTiles[i].setVisibility(vis ? View.VISIBLE : View.GONE);
            if (vis) {
                leftTiles[i].setText(pairs[leftOrder[i]].translit);
                rightTiles[i].setText(pairs[rightOrder[i]].english);
                styleTile(leftTiles[i], leftDone[i], selectedLeft == i, R.color.emerald);
                styleTile(rightTiles[i], rightDone[i], false, R.color.cream);
            }
        }
        roundView.setText("PAIRS " + pairCount);
    }

    private void styleTile(TextView t, boolean done, boolean selected, int colorId) {
        if (done) {
            t.setBackground(Ui.pill(this, R.color.leaf, 14));
            t.setTextColor(Ui.color(this, R.color.espresso));
            t.setAlpha(0.5f);
        } else if (selected) {
            t.setBackground(Ui.pill(this, R.color.leaf, 14));
            t.setTextColor(Ui.color(this, R.color.espresso));
            t.setAlpha(1f);
        } else {
            t.setBackground(Ui.outline(this, colorId, 14));
            t.setTextColor(Ui.color(this, colorId));
            t.setAlpha(1f);
        }
    }

    private void tapLeft(int i) {
        if (leftDone[i]) return;
        selectedLeft = i;
        render();
    }

    private void tapRight(int j) {
        if (rightDone[j]) return;
        if (selectedLeft < 0) return;
        int li = selectedLeft;
        if (leftOrder[li] == rightOrder[j]) {
            leftDone[li] = true;
            rightDone[j] = true;
            selectedLeft = -1;
            score += 150;
            store.record(pairs[li], true);
            updateHud();
            if (boardClear()) {
                pairCount = Math.min(MAX_PAIRS, pairCount + 1);
                newBoard();
            } else {
                render();
            }
        } else {
            lives--;
            store.record(pairs[li], false);
            updateHud();
            selectedLeft = -1;
            if (lives <= 0) {
                store.submitScore("match", score);
                finish();
            } else {
                render();
            }
        }
    }

    private boolean boardClear() {
        for (int i = 0; i < pairCount; i++) if (!leftDone[i]) return false;
        return true;
    }

    private void updateHud() {
        scoreView.setText("SCORE " + score);
        livesView.setText("LIVES " + lives);
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
