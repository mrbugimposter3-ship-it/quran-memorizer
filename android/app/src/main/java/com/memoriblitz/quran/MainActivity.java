package com.memoriblitz.quran;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private QuranData quran;
    private ProgressStore store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        root.setPadding(pad, dp(28), pad, dp(28));

        TextView title = Ui.title(this, "QURAN MEMORIZER", 30, R.color.emerald);
        root.addView(title);

        TextView subtitle = Ui.title(this, "Full Qur'an · 6236 ayahs · memorise at speed", 14, R.color.text_secondary);
        root.addView(subtitle);

        root.addView(space(18));

        List<Ayah> study = store.study(quran);
        TextView sel = Ui.title(this, store.selectionLabel() + "  ·  " + study.size() + " ayahs", 14, R.color.cream);
        root.addView(sel);
        TextView stats = Ui.title(this, store.totalCorrect() + " correct recalls", 12, R.color.text_secondary);
        root.addView(stats);

        root.addView(space(20));

        root.addView(gameButton("TEST", "Match the ayah to its meaning", R.color.emerald, TestActivity.class));
        root.addView(space(12));
        root.addView(gameButton("MATCH", "Pair transliteration with meaning", R.color.cream, MatchActivity.class));
        root.addView(space(12));
        root.addView(gameButton("FLASH", "Self-graded adaptive cards", R.color.olive, FlashActivity.class));
        root.addView(space(12));
        root.addView(gameButton("FILL", "Supply the missing word", R.color.terracotta, FillActivity.class));
        root.addView(space(12));
        root.addView(gameButton("RECITE", "Type the full transliteration from memory", R.color.emerald, ReciteActivity.class));
        root.addView(space(12));
        root.addView(gameButton("ORDER", "Tap the words in the correct order", R.color.cream, OrderActivity.class));
        root.addView(space(12));
        root.addView(gameButton("NEXT", "Pick the ayah that follows", R.color.olive, NextActivity.class));
        root.addView(space(12));
        root.addView(gameButton("ARABIC", "See Arabic, choose the meaning", R.color.terracotta, ArabicActivity.class));
        root.addView(space(12));
        root.addView(gameButton("REVERSE", "See meaning, choose the Arabic", R.color.caramel, ReverseActivity.class));
        root.addView(space(12));
        root.addView(gameButton("PAIR", "Match Arabic to transliteration", R.color.leaf, PairActivity.class));
        root.addView(space(12));
        root.addView(gameButton("INITIALS", "Recall from first letters", R.color.emerald, InitialsActivity.class));
        root.addView(space(12));
        root.addView(gameButton("SPEED", "Rapid true/false against the clock", R.color.warm_red, SpeedActivity.class));
        root.addView(space(12));
        root.addView(gameButton("GRID", "Memory game: match Arabic & meaning", R.color.olive, GridActivity.class));
        root.addView(space(12));
        root.addView(gameButton("FILL-AR", "Supply the missing Arabic word", R.color.terracotta, FillArActivity.class));
        root.addView(space(12));
        root.addView(gameButton("JUMBLE", "Unscramble the letters", R.color.cream, JumbleActivity.class));
        root.addView(space(12));
        root.addView(gameButton("VS AI", "Race the clock on surah / page / juz", R.color.warm_red, VsAiActivity.class));
        root.addView(space(12));
        root.addView(gameButton("VERSE BUILD", "Build the ayah word by word", R.color.leaf, VerseBuildActivity.class));
        root.addView(space(12));
        root.addView(gameButton("URDU", "See Arabic, choose the Urdu meaning", R.color.terracotta, UrduActivity.class));
        root.addView(space(12));
        root.addView(gameButton("REVEAL", "Ayah revealed word by word - recall it early", R.color.olive, RevealActivity.class));
        root.addView(space(12));
        root.addView(gameButton("WORD WALK", "Step a surah word by word with meaning and context", R.color.caramel, WordActivity.class));
        root.addView(space(12));
        root.addView(gameButton("MIX", "Cycles through every mode at random", R.color.emerald, MixActivity.class));
        root.addView(space(12));
        root.addView(gameButton("ODD ONE OUT", "Three ayahs share a surah - pick the odd one out", R.color.olive, OddOneActivity.class));
        root.addView(space(12));
        root.addView(gameButton("WORD COUNT", "Count the Arabic words of the ayah", R.color.emerald, WordCountActivity.class));
        root.addView(space(12));
        root.addView(gameButton("FIRST / LAST", "Recall the first or last word", R.color.cream, EdgeWordActivity.class));
        root.addView(space(12));
        root.addView(gameButton("NEXT WORD", "Supply the final word of the ayah", R.color.terracotta, NextWordActivity.class));
        root.addView(space(12));
        root.addView(gameButton("PARTIAL", "Identify the full ayah from a fragment", R.color.emerald, PartialActivity.class));
        root.addView(space(12));
        root.addView(gameButton("EN \u2192 URDU", "See the English, choose its Urdu", R.color.terracotta, En2UrActivity.class));
        root.addView(space(12));
        root.addView(gameButton("URDU \u2192 EN", "See the Urdu, choose its English", R.color.caramel, Ur2EnActivity.class));
        root.addView(space(12));
        root.addView(gameButton("URDU \u2192 AR", "See the Urdu, choose the Arabic", R.color.leaf, Ur2ArActivity.class));
        root.addView(space(12));
        root.addView(gameButton("AR \u2192 TRANSLIT", "See the Arabic, choose its transliteration", R.color.olive, Ar2TrActivity.class));
        root.addView(space(12));
        root.addView(gameButton("SURAH TRIVIA", "Ayah counts and revelation place", R.color.cream, SurahInfoActivity.class));
        root.addView(space(12));
        root.addView(gameButton("WHICH SURAH", "Name the surah of the ayah", R.color.emerald, WhereSurahActivity.class));
        root.addView(space(12));
        root.addView(gameButton("WHICH JUZ", "Name the juz of the ayah", R.color.cream, WhereJuzActivity.class));
        root.addView(space(12));
        root.addView(gameButton("WHICH PAGE", "Name the page of the ayah", R.color.olive, WherePageActivity.class));
        root.addView(space(12));
        root.addView(gameButton("AYAH NO.", "Name the ayah number", R.color.caramel, WhereAyahActivity.class));
        root.addView(space(12));
        root.addView(gameButton("NUMBER STORY", "Each ayah as a number-story of famous books", R.color.cream, StoryActivity.class));

        root.addView(space(24));

        Button select = Ui.button(this, "SELECT SURAH / JUZ / PAGE", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SelectActivity.class));
            }
        });
        root.addView(select);

        root.addView(space(12));

        Button read = Ui.ghost(this, "READ", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ReaderActivity.class));
            }
        });
        root.addView(read);

        root.addView(space(12));

        Button reset = Ui.ghost(this, "RESET STATS", R.color.text_secondary, new View.OnClickListener() {
            @Override public void onClick(View v) {
                store.resetStats();
                recreate();
            }
        });
        root.addView(reset);

        root.addView(space(12));

        Button reminder = Ui.ghost(this, "REMINDER INTERVAL", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { adjustReminder(); }
        });
        root.addView(reminder);

        root.addView(space(12));

        final Button fontBtn = Ui.ghost(this, "", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) {
                double s = store.questionScale() + 0.2;
                if (s > 1.6) s = 0.8;
                store.setQuestionScale(s);
                ((Button) v).setText("FONT " + (int) Math.round(s * 100) + "%");
            }
        });
        fontBtn.setText("FONT " + (int) Math.round(store.questionScale() * 100) + "%");
        root.addView(fontBtn);

        root.addView(space(12));

        Button burdah = Ui.ghost(this, "QASEEDA BURDAH SHAREEF", R.color.terracotta, new View.OnClickListener() {
            @Override public void onClick(View v) { openSupplementary("burdah.data"); }
        });
        root.addView(burdah);

        root.addView(space(12));

        Button dalail = Ui.ghost(this, "DALAIL AL-KHAYRAT", R.color.olive, new View.OnClickListener() {
            @Override public void onClick(View v) { openSupplementary("dalail.data"); }
        });
        root.addView(dalail);

        scroll.addView(root);
        setContentView(scroll);

        Reminder.schedule(this);
    }

    private void adjustReminder() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(String.valueOf(store.popupInterval()));
        new AlertDialog.Builder(this)
                .setTitle("Reminder interval (seconds, 0 = off)")
                .setView(input)
                .setPositiveButton("Save", new android.content.DialogInterface.OnClickListener() {
                    @Override public void onClick(android.content.DialogInterface d, int w) {
                        try {
                            int v = Integer.parseInt(input.getText().toString().trim());
                            store.setPopupInterval(v);
                            Reminder.schedule(MainActivity.this);
                        } catch (Throwable ig) {}
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openSupplementary(String file) {
        Intent i = new Intent(MainActivity.this, SupplementaryActivity.class);
        i.putExtra("file", file);
        startActivity(i);
    }

    private View gameButton(String name, String blurb, int colorId, final Class<?> cls) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(20), dp(16), dp(20), dp(16));
        box.setBackground(Ui.outline(this, colorId, 18));

        TextView n = Ui.label(this, name, 20, colorId);
        box.addView(n);
        TextView b = Ui.label(this, blurb, 13, R.color.text_secondary);
        box.addView(b);

        box.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, cls));
            }
        });
        return box;
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
