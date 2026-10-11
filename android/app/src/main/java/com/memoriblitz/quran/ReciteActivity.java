package com.memoriblitz.quran;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.List;
import java.util.Random;

/** RECITE: recall the full transliteration from the English meaning. */
public class ReciteActivity extends AppCompatActivity {

    private final Random rnd = new Random();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;

    private TextView scoreView, livesView, refView, promptView, revealView;
    private EditText input;

    private Ayah target;
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

        promptView = Ui.title(this, "", store.q(18), R.color.text_primary);
        root.addView(promptView);
        root.addView(Ui.vspace(this, 16));

        input = new EditText(this);
        input.setHint("type the transliteration");
        input.setTextSize(store.q(18));
        input.setTextColor(Ui.color(this, R.color.text_primary));
        input.setHintTextColor(Ui.color(this, R.color.text_secondary));
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setPadding(dp(12), dp(10), dp(12), dp(10));
        input.setBackground(Ui.outline(this, R.color.cream, 14));
        root.addView(input);
        root.addView(Ui.vspace(this, 12));

        Button check = Ui.button(this, "CHECK", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { check(); }
        });
        root.addView(check);
        root.addView(Ui.vspace(this, 8));

        Button reveal = Ui.ghost(this, "REVEAL", R.color.text_secondary, new View.OnClickListener() {
            @Override public void onClick(View v) { reveal(); }
        });
        root.addView(reveal);
        root.addView(Ui.vspace(this, 12));

        revealView = Ui.label(this, "", store.q(14), R.color.text_secondary);
        root.addView(revealView);

        setContentView(root);

        score = 0; lives = 3;
        nextCard();
    }

    private void nextCard() {
        target = study.isEmpty() ? null : study.get(rnd.nextInt(study.size()));
        input.setText("");
        revealView.setText("");
        if (target != null) {
            refView.setText(Emoji.forAyah(target.english) + "  " + target.fullRef());
            promptView.setText(target.english);
        }
    }

    private String norm(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    private void check() {
        if (target == null) return;
        String got = norm(input.getText().toString());
        if (got.isEmpty()) return;
        if (got.equals(norm(target.translit))) {
            score += 100;
            store.record(target, true);
            revealView.setText("CORRECT  " + target.translit);
            revealView.setTextColor(Ui.color(this, R.color.leaf));
            scoreView.setText("SCORE " + score);
            nextCard();
        } else {
            lives--;
            store.record(target, false);
            livesView.setText("LIVES " + lives);
            revealView.setText("Not quite. Correct: " + target.translit);
            revealView.setTextColor(Ui.color(this, R.color.warm_red));
            if (lives <= 0) { store.submitScore("recite", score); finish(); return; }
            nextCard();
        }
    }

    private void reveal() {
        if (target == null) return;
        lives--;
        store.record(target, false);
        livesView.setText("LIVES " + lives);
        revealView.setText(target.arabic + "\n" + target.translit);
        revealView.setTextColor(Ui.color(this, R.color.text_secondary));
        if (lives <= 0) { store.submitScore("recite", score); finish(); return; }
        nextCard();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
