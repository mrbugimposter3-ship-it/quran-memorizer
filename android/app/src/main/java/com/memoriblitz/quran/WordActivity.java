package com.memoriblitz.quran;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;

public class WordActivity extends AppCompatActivity {

    private static class Step {
        String word, translit, gloss, ayahEn, ref;
    }

    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Step> steps = new ArrayList<>();
    private int idx = 0;
    private boolean showMeaning = true;
    private boolean autoPlay = false;
    private int wordLimit = 3;   // seconds per word

    private TextView progressView, refView, contextView, wordView, translitView, glossView, ayahView;
    private Button prevBtn, nextBtn, meaningBtn, autoBtn, slowBtn, fastBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);
        List<Ayah> study = store.study(quran);
        buildSteps(study);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(18), dp(18), dp(18));

        TextView title = Ui.title(this, "WORD WALK · " + store.selectionLabel(), 18, R.color.text_primary);
        root.addView(title);

        progressView = Ui.title(this, "", 13, R.color.cream);
        root.addView(progressView);

        root.addView(space(16));

        refView = Ui.title(this, "", 13, R.color.terracotta);
        root.addView(refView);

        contextView = Ui.title(this, "", 15, R.color.text_secondary);
        contextView.setTextDirection(View.TEXT_DIRECTION_RTL);
        root.addView(contextView);

        root.addView(space(10));

        wordView = Ui.title(this, "", 40, R.color.cream);
        wordView.setTextDirection(View.TEXT_DIRECTION_RTL);
        wordView.setGravity(Gravity.CENTER);
        root.addView(wordView);

        translitView = Ui.title(this, "", 20, R.color.text_primary);
        root.addView(translitView);

        glossView = Ui.title(this, "", 16, R.color.caramel);
        root.addView(glossView);

        root.addView(space(8));

        ayahView = Ui.title(this, "", 14, R.color.text_secondary);
        root.addView(ayahView);

        root.addView(space(18));

        // bottom controls
        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);

        prevBtn = Ui.ghost(this, "< PREV", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) { advance(-1); }
        });
        meaningBtn = Ui.ghost(this, "MEANING ON", R.color.olive, new View.OnClickListener() {
            @Override public void onClick(View v) { showMeaning = !showMeaning; render(); }
        });
        autoBtn = Ui.ghost(this, "AUTO OFF", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { autoPlay = !autoPlay; render(); tick(); }
        });
        nextBtn = Ui.button(this, "NEXT >", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { advance(1); }
        });
        slowBtn = Ui.ghost(this, "\u2212", R.color.olive, new View.OnClickListener() {
            @Override public void onClick(View v) { setSpeed(wordLimit - 1); }
        });
        fastBtn = Ui.ghost(this, "+", R.color.olive, new View.OnClickListener() {
            @Override public void onClick(View v) { setSpeed(wordLimit + 1); }
        });

        controls.addView(prevBtn);
        controls.addView(Ui.hspace(this, 8));
        controls.addView(meaningBtn);
        controls.addView(Ui.hspace(this, 6));
        controls.addView(slowBtn);
        controls.addView(Ui.hspace(this, 4));
        controls.addView(fastBtn);
        controls.addView(Ui.hspace(this, 6));
        controls.addView(autoBtn);
        controls.addView(Ui.hspace(this, 8));
        controls.addView(nextBtn);
        root.addView(controls);

        setContentView(root);

        render();
    }

    private void buildSteps(List<Ayah> study) {
        steps.clear();
        for (Ayah a : study) {
            String[] ar = a.arabic.trim().split("\\s+");
            String[] tr = a.translit.trim().split("\\s+");
            String[] en = a.english.replaceAll("[^\\p{L}\\p{N}'\\s-]", " ").trim().split("\\s+");
            int n = ar.length;
            for (int i = 0; i < n; i++) {
                Step s = new Step();
                s.word = ar[i];
                s.translit = map(tr, i, n);
                s.gloss = map(en, i, n);
                s.ayahEn = a.english;
                s.ref = "Surah " + a.surah + " : " + a.ayah;
                steps.add(s);
            }
        }
        idx = 0;
    }

    private static String map(String[] words, int i, int n) {
        if (words == null || words.length == 0) return "";
        int j = (int) Math.round(i * (words.length - 1) / (double) Math.max(1, n - 1));
        if (j < 0) j = 0;
        if (j >= words.length) j = words.length - 1;
        return words[j];
    }

    private void advance(int d) {
        if (steps.isEmpty()) return;
        int ni = idx + d;
        if (ni < 0) ni = 0;
        if (ni >= steps.size()) { ni = 0; }   // loop back at the end
        idx = ni;
        nextBtn.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
        // pop animation on the word
        wordView.setScaleX(1.25f);
        wordView.setScaleY(1.25f);
        wordView.animate().scaleX(1f).scaleY(1f).setDuration(180).start();
        render();
        if (autoPlay) tick();
    }

    private void render() {
        if (steps.isEmpty()) {
            progressView.setText("No ayahs selected");
            return;
        }
        Step s = steps.get(idx);
        progressView.setText("WORD " + (idx + 1) + " / " + steps.size()
                + (autoPlay ? "   ·   AUTO " + wordLimit + "s/word" : "   ·   tap NEXT  ·  " + wordLimit + "s/word"));

        refView.setText(s.ref);

        String prev1 = idx >= 1 ? steps.get(idx - 1).word : "";
        String prev2 = idx >= 2 ? steps.get(idx - 2).word : "";
        String next1 = idx + 1 < steps.size() ? steps.get(idx + 1).word : "";
        String next2 = idx + 2 < steps.size() ? steps.get(idx + 2).word : "";
        StringBuilder ctx = new StringBuilder();
        if (!prev2.isEmpty()) ctx.append(prev2).append(" ");
        if (!prev1.isEmpty()) ctx.append(prev1).append(" ");
        ctx.append("  ··  ");
        if (!next1.isEmpty()) ctx.append(next1).append(" ");
        if (!next2.isEmpty()) ctx.append(next2).append(" ");
        contextView.setText(ctx.toString().trim());

        wordView.setText(s.word);
        translitView.setText(s.translit);
        glossView.setText(s.gloss.isEmpty() ? "" : "\u2248 " + s.gloss);
        ayahView.setText(showMeaning ? s.ayahEn : "");

        meaningBtn.setText(showMeaning ? "MEANING ON" : "MEANING OFF");
        autoBtn.setText(autoPlay ? "AUTO ON" : "AUTO OFF");
        prevBtn.setEnabled(idx > 0);
    }

    private void setSpeed(int s) {
        wordLimit = Math.max(1, Math.min(10, s));
        if (autoPlay) tick();
        render();
    }

    private void tick() {
        handler.removeCallbacksAndMessages(null);
        if (!autoPlay) return;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                if (autoPlay) { advance(1); }
            }
        }, wordLimit * 1000L);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private View space(int dp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(1, dp(dp)));
        return v;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
