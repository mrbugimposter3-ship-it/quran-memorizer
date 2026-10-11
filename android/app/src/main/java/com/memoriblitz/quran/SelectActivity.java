package com.memoriblitz.quran;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.memoriblitz.quran.QuranData.Ayah;
import com.memoriblitz.quran.QuranData.Surah;

import java.util.ArrayList;
import java.util.List;

public class SelectActivity extends AppCompatActivity {

    private static final int MODE_SURAH = 0, MODE_JUZ = 1, MODE_PAGE = 2;

    private QuranData quran;
    private ProgressStore store;
    private int mode = MODE_SURAH;
    private RecyclerView list;
    private Button btnSurah, btnJuz, btnPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(16), pad, dp(16));

        TextView title = Ui.title(this, "SELECT WHAT TO MEMORISE", 20, R.color.text_primary);
        root.addView(title);
        TextView cur = Ui.title(this, "current: " + store.selectionLabel(), 12, R.color.cream);
        root.addView(cur);

        root.addView(space(12));

        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        btnSurah = tab("SURAH", MODE_SURAH, R.color.emerald);
        btnJuz = tab("JUZ", MODE_JUZ, R.color.cream);
        btnPage = tab("PAGE", MODE_PAGE, R.color.terracotta);
        tabs.addView(btnSurah);
        tabs.addView(btnJuz);
        tabs.addView(btnPage);
        root.addView(tabs);

        root.addView(space(8));

        list = new RecyclerView(this);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(new Adapter());
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        Button full = Ui.button(this, "FULL QUR'AN", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) {
                store.setSelection("all", "Full Qur'an (6236 ayahs)");
                finish();
            }
        });
        root.addView(full);

        setContentView(root);
        refreshTabs();
    }

    private Button tab(String text, final int m, final int colorId) {
        Button b = Ui.ghost(this, text, colorId, new View.OnClickListener() {
            @Override public void onClick(View v) {
                mode = m;
                refreshTabs();
                list.getAdapter().notifyDataSetChanged();
            }
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        b.setLayoutParams(lp);
        return b;
    }

    private void refreshTabs() {
        btnSurah.setBackground(Ui.pill(this, mode == MODE_SURAH ? R.color.emerald : R.color.card, 20));
        btnSurah.setTextColor(Ui.color(this, mode == MODE_SURAH ? R.color.espresso : R.color.emerald));
        btnJuz.setBackground(Ui.pill(this, mode == MODE_JUZ ? R.color.cream : R.color.card, 20));
        btnJuz.setTextColor(Ui.color(this, mode == MODE_JUZ ? R.color.espresso : R.color.cream));
        btnPage.setBackground(Ui.pill(this, mode == MODE_PAGE ? R.color.terracotta : R.color.card, 20));
        btnPage.setTextColor(Ui.color(this, mode == MODE_PAGE ? R.color.espresso : R.color.terracotta));
    }

    private int rowCount() {
        if (mode == MODE_SURAH) return quran.surahs.size();
        if (mode == MODE_JUZ) return 30;
        return 604;
    }

    private String rowLabel(int i) {
        if (mode == MODE_SURAH) {
            Surah s = quran.surah(i + 1);
            return (i + 1) + ". " + s.english + "  ·  " + s.translit + "  ·  " + s.ayahCount + " ayahs";
        }
        if (mode == MODE_JUZ) {
            Ayah first = quran.ayahs.get(quran.juzStart[i + 1]);
            return "Juz " + (i + 1) + "  ·  starts " + first.surah + ":" + first.ayah;
        }
        Ayah first = quran.ayahs.get(quran.pageStart[i + 1]);
        return "Page " + (i + 1) + "  ·  starts " + first.surah + ":" + first.ayah;
    }

    private void select(int row) {
        List<Ayah> list;
        String spec, label;
        if (mode == MODE_SURAH) {
            Surah s = quran.surah(row + 1);
            spec = "surah:" + (row + 1);
            label = "Surah " + (row + 1) + " " + s.english;
            list = quran.ayahsForSurah(row + 1);
        } else if (mode == MODE_JUZ) {
            spec = "juz:" + (row + 1);
            label = "Juz " + (row + 1);
            list = quran.ayahsForJuz(row + 1);
        } else {
            spec = "page:" + (row + 1);
            label = "Page " + (row + 1);
            list = quran.ayahsForPage(row + 1);
        }
        store.setSelection(spec, label + " (" + list.size() + " ayahs)");
        finish();
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView t = new TextView(SelectActivity.this);
            t.setTextSize(15);
            t.setTextColor(Ui.color(SelectActivity.this, R.color.text_primary));
            t.setGravity(Gravity.CENTER_VERTICAL);
            t.setPadding(dp(20), dp(14), dp(20), dp(14));
            t.setBackground(Ui.pill(SelectActivity.this, R.color.card, 12));
            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(8));
            t.setLayoutParams(lp);
            return new VH(t);
        }

        @Override
        public void onBindViewHolder(VH h, final int pos) {
            h.t.setText(rowLabel(pos));
            h.t.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { select(pos); }
            });
        }

        @Override
        public int getItemCount() { return rowCount(); }

        class VH extends RecyclerView.ViewHolder {
            TextView t;
            VH(TextView tv) { super(tv); t = tv; }
        }
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
