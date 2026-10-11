package com.memoriblitz.quran;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.List;

public class ReaderActivity extends AppCompatActivity {

    private final Handler handler = new Handler();

    private QuranData quran;
    private ProgressStore store;
    private List<Ayah> study;
    private RecyclerView list;
    private LinearLayoutManager layoutManager;
    private Adapter adapter;

    private float arFont = 24f;
    private int speedMin = 15;   // minutes per juz
    private boolean scrolling = false;
    private TextView speedLabel, fontLabel, surahLabel;
    private Button playBtn;
    private int currentSurah = 1;
    private int colMode = 3;   // 1 stacked, 2 two-col, 3 three-col

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        quran = QuranData.get(this);
        store = ProgressStore.get(this);
        study = store.study(quran);
        if (!study.isEmpty()) currentSurah = study.get(0).surah;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(16);
        root.setPadding(pad, dp(16), pad, dp(16));

        TextView title = Ui.title(this, "READ · " + store.selectionLabel(), 18, R.color.text_primary);
        root.addView(title);
        root.addView(space(8));

        // surah navigator row
        LinearLayout surahRow = new LinearLayout(this);
        surahRow.setOrientation(LinearLayout.HORIZONTAL);
        surahRow.setGravity(Gravity.CENTER);
        Button prev = Ui.ghost(this, "< PREV", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) { goSurah(currentSurah - 1); }
        });
        Button select = Ui.ghost(this, "SELECT", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(ReaderActivity.this, SelectActivity.class));
            }
        });
        Button next = Ui.ghost(this, "NEXT >", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) { goSurah(currentSurah + 1); }
        });
        surahRow.addView(prev);
        surahRow.addView(Ui.hspace(this, 10));
        surahRow.addView(select);
        surahRow.addView(Ui.hspace(this, 10));
        surahRow.addView(next);
        root.addView(surahRow);
        surahLabel = Ui.label(this, "", 12, R.color.text_secondary);
        surahLabel.setGravity(Gravity.CENTER);
        root.addView(surahLabel);
        root.addView(space(8));

        // controls row 1: font
        LinearLayout fontRow = new LinearLayout(this);
        fontRow.setOrientation(LinearLayout.HORIZONTAL);
        Button fMinus = Ui.ghost(this, "A-", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { setFont(arFont - 2f); }
        });
        Button fPlus = Ui.ghost(this, "A+", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { setFont(arFont + 2f); }
        });
        fontLabel = Ui.label(this, "font 24", 13, R.color.text_secondary);
        fontRow.addView(fMinus);
        fontRow.addView(Ui.hspace(this, 8));
        fontRow.addView(fPlus);
        fontRow.addView(Ui.hspace(this, 16));
        fontRow.addView(fontLabel);
        fontRow.addView(Ui.hspace(this, 16));
        Button colsBtn = Ui.ghost(this, "COLS: 3", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { cycleCols((Button) v); }
        });
        fontRow.addView(colsBtn);
        root.addView(fontRow);
        root.addView(space(8));

        // controls row 2: autoscroll
        LinearLayout scrollRow = new LinearLayout(this);
        scrollRow.setOrientation(LinearLayout.HORIZONTAL);
        playBtn = Ui.button(this, "PLAY", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) {
                scrolling = !scrolling;
                playBtn.setText(scrolling ? "PAUSE" : "PLAY");
                if (scrolling) tick();
            }
        });
        Button sMinus = Ui.ghost(this, "-", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { setSpeed(speedMin - 5); }
        });
        Button sPlus = Ui.ghost(this, "+", R.color.cream, new View.OnClickListener() {
            @Override public void onClick(View v) { setSpeed(speedMin + 5); }
        });
        speedLabel = Ui.label(this, "15 min/juz", 13, R.color.text_secondary);
        scrollRow.addView(playBtn);
        scrollRow.addView(Ui.hspace(this, 10));
        scrollRow.addView(sMinus);
        scrollRow.addView(Ui.hspace(this, 6));
        scrollRow.addView(sPlus);
        scrollRow.addView(Ui.hspace(this, 12));
        scrollRow.addView(speedLabel);
        root.addView(scrollRow);
        root.addView(space(10));

        list = new RecyclerView(this);
        layoutManager = new LinearLayoutManager(this);
        list.setLayoutManager(layoutManager);
        adapter = new Adapter();
        list.setAdapter(adapter);
        root.addView(list, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(root);

        updateSurahLabel();

        // Restore the last-viewed ayah position (so re-opening from a game's
        // QURAN peek returns to the same ayah instead of scrolling to the top).
        int[] saved = store.readerScroll();
        if (saved[0] > 0 || saved[1] != 0) {
            layoutManager.scrollToPositionWithOffset(saved[0], saved[1]);
        }

        Reminder.schedule(this);
    }

    private void goSurah(int n) {
        if (n < 1 || n > quran.surahs.size()) return;
        currentSurah = n;
        QuranData.Surah s = quran.surah(n);
        store.setSelection("surah:" + n, "Surah " + n + " " + s.english);
        study = quran.ayahsForSurah(n);
        adapter.notifyDataSetChanged();
        layoutManager.scrollToPosition(0);
        updateSurahLabel();
    }

    private void updateSurahLabel() {
        QuranData.Surah s = quran.surah(currentSurah);
        surahLabel.setText(s == null ? "" : s.number + ". " + s.english + " (" + s.translit + ")");
    }

    @Override
    protected void onPause() {
        super.onPause();
        int pos = layoutManager.findFirstVisibleItemPosition();
        View first = layoutManager.findViewByPosition(pos);
        int offset = first != null ? first.getTop() - list.getPaddingTop() : 0;
        store.saveReaderScroll(pos, offset);
    }

    private void setFont(float f) {
        arFont = Math.max(14f, Math.min(48f, f));
        fontLabel.setText("font " + (int) arFont);
        adapter.notifyDataSetChanged();
    }

    private void setSpeed(int s) {
        speedMin = Math.max(5, Math.min(60, s));
        speedLabel.setText(speedMin + " min/juz");
    }

    private void cycleCols(Button b) {
        colMode = colMode >= 3 ? 1 : colMode + 1;
        b.setText("COLS: " + colMode);
        adapter.notifyDataSetChanged();
    }

    private void tick() {
        if (!scrolling) return;
        float rate = 30000f / (speedMin * 60f);   // px per second
        list.scrollBy(0, (int) (rate * 0.05f));
        handler.postDelayed(new Runnable() {
            @Override public void run() { tick(); }
        }, 50);
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout box = new LinearLayout(ReaderActivity.this);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(dp(14), dp(12), dp(14), dp(12));
            box.setBackground(Ui.pill(ReaderActivity.this, R.color.card, 14));

            RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, dp(10));
            box.setLayoutParams(lp);

            // header: emoji + reference
            LinearLayout header = new LinearLayout(ReaderActivity.this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER);
            TextView emoji = Ui.label(ReaderActivity.this, "", 18, R.color.cream);
            TextView ref = Ui.label(ReaderActivity.this, "", 12, R.color.cream);
            header.addView(emoji);
            header.addView(Ui.hspace(ReaderActivity.this, 8));
            header.addView(ref);
            box.addView(header);

            // columns container (rebuilt per bind for 1/2/3-col layout)
            LinearLayout cols = new LinearLayout(ReaderActivity.this);
            cols.setOrientation(LinearLayout.HORIZONTAL);
            cols.setGravity(Gravity.CENTER_VERTICAL);
            box.addView(cols, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(box, ref, emoji, cols);
        }

        @Override
        public void onBindViewHolder(VH h, int pos) {
            Ayah a = study.get(pos);
            h.ref.setText(a.fullRef());
            h.emoji.setText(Emoji.forAyah(a.english));
            h.cols.removeAllViews();

            LinearLayout.LayoutParams colLp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);

            if (colMode == 1) {
                h.cols.setOrientation(LinearLayout.VERTICAL);
                h.cols.addView(line(a.arabic, arFont, R.color.cream, true));
                h.cols.addView(line(a.translit, 14, R.color.text_secondary, false));
                h.cols.addView(line(a.english, 14, R.color.text_secondary, false));
                if (a.urdu != null && !a.urdu.trim().isEmpty())
                    h.cols.addView(line(a.urdu, 15, R.color.olive, true));
            } else if (colMode == 2) {
                h.cols.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout left = new LinearLayout(ReaderActivity.this);
                left.setOrientation(LinearLayout.VERTICAL);
                left.addView(line(a.arabic, arFont, R.color.cream, true));
                left.addView(line(a.translit, 14, R.color.text_secondary, false));
                LinearLayout right = new LinearLayout(ReaderActivity.this);
                right.setOrientation(LinearLayout.VERTICAL);
                right.addView(line(a.english, 14, R.color.text_secondary, false));
                if (a.urdu != null && !a.urdu.trim().isEmpty())
                    right.addView(line(a.urdu, 15, R.color.olive, true));
                h.cols.addView(left, colLp);
                h.cols.addView(right, colLp);
            } else {
                h.cols.setOrientation(LinearLayout.HORIZONTAL);
                h.cols.addView(line(a.arabic, arFont, R.color.cream, true), colLp);
                h.cols.addView(line(a.english, 14, R.color.text_secondary, false), colLp);
                h.cols.addView(line(a.urdu == null ? "" : a.urdu, 15, R.color.olive, true), colLp);
            }
        }

        private TextView line(String text, float size, int color, boolean rtl) {
            TextView t = new TextView(ReaderActivity.this);
            t.setText(text);
            t.setTextSize(size);
            t.setTextColor(Ui.color(ReaderActivity.this, color));
            t.setGravity(Gravity.CENTER);
            if (rtl) t.setTextDirection(View.TEXT_DIRECTION_RTL);
            return t;
        }

        @Override
        public int getItemCount() { return study.size(); }

        class VH extends RecyclerView.ViewHolder {
            TextView ref, emoji;
            LinearLayout cols;
            VH(View v, TextView r, TextView e, LinearLayout c) {
                super(v); ref = r; emoji = e; cols = c;
            }
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
