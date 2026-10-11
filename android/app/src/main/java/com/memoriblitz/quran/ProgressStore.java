package com.memoriblitz.quran;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import com.memoriblitz.quran.QuranData.Ayah;

/**
 * Progress + selection persistence backed by SharedPreferences.
 */
public class ProgressStore {

    private static final String PREFS = "quranmemorizer";

    private static ProgressStore instance;
    private final SharedPreferences sp;

    public static synchronized ProgressStore get(Context ctx) {
        if (instance == null) instance = new ProgressStore(ctx.getApplicationContext());
        return instance;
    }

    private ProgressStore(Context ctx) {
        sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    /* ---- passcode ---- */
    /** Current passcode (6463 or 25524), re-chosen every 60 minutes. */
    public String passcode() {
        long now = System.currentTimeMillis();
        long t = sp.getLong("pc_t", 0);
        String code = sp.getString("pc", null);
        if (code == null || now - t > 60 * 60 * 1000L) {
            code = new java.util.Random().nextBoolean() ? "6463" : "25524";
            sp.edit().putString("pc", code).putLong("pc_t", now).apply();
        }
        return code;
    }

    /* ---- best scores ---- */
    public int bestOf(String game) { return sp.getInt("best_" + game, 0); }

    public boolean submitScore(String game, int score) {
        if (score > bestOf(game)) {
            sp.edit().putInt("best_" + game, score).apply();
            return true;
        }
        return false;
    }

    /* ---- selection ---- */
    public String selectionSpec() { return sp.getString("spec", "juz:30"); }
    public String selectionLabel() { return sp.getString("label", "Juz 30 (Amma)"); }

    public void setSelection(String spec, String label) {
        sp.edit().putString("spec", spec).putString("label", label).apply();
    }

    /** The current study set, resolved from the stored spec. */
    public List<Ayah> study(QuranData q) {
        return q.ayahsFor(selectionSpec());
    }

    /* ---- per-ayah progress ---- */
    public int[] progress(int global) {
        String s = sp.getString("p_" + global, null);
        if (s == null) return new int[]{0, 0};
        String[] parts = s.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
    }

    public void record(Ayah a, boolean hit) {
        if (a == null) return;
        int[] p = progress(a.global);
        if (hit) p[0]++; else p[1]++;
        sp.edit().putString("p_" + a.global, p[0] + "," + p[1]).apply();
    }

    public int totalCorrect() {
        int t = 0;
        for (String key : sp.getAll().keySet()) {
            if (key.startsWith("p_")) {
                String s = sp.getString(key, "0,0");
                try { t += Integer.parseInt(s.split(",")[0]); } catch (Throwable ig) {}
            }
        }
        return t;
    }

    public int shakyCount(List<Ayah> study) {
        int t = 0;
        for (Ayah a : study) {
            int[] p = progress(a.global);
            if (p[1] > p[0]) t++;
        }
        return t;
    }

    public void resetStats() {
        SharedPreferences.Editor e = sp.edit();
        for (String key : new ArrayList<>(sp.getAll().keySet())) {
            if (key.startsWith("p_")) e.remove(key);
        }
        e.apply();
    }

    /* ---- question font scale (0.7 .. 1.6) ---- */
    public double questionScale() {
        return (double) sp.getFloat("qscale", 1.0f);
    }

    public void setQuestionScale(double s) {
        sp.edit().putFloat("qscale", (float) Math.max(0.7, Math.min(1.6, s))).apply();
    }

    /* ---- reader scroll position (last-viewed ayah) ---- */
    public void saveReaderScroll(int pos, int offset) {
        sp.edit().putInt("reader_pos", pos).putInt("reader_offset", offset).apply();
    }

    public int[] readerScroll() {
        return new int[]{ sp.getInt("reader_pos", 0), sp.getInt("reader_offset", 0) };
    }

    /* ---- reminder popup interval (seconds; 0 = off) ---- */
    public int popupInterval() { return sp.getInt("popup_interval", 30); }
    public void setPopupInterval(int sec) {
        sp.edit().putInt("popup_interval", Math.max(0, Math.min(3600, sec))).apply();
    }

    /** Scale a question font size by the user's global question-scale setting. */
    public int q(int base) {
        return (int) Math.round(base * questionScale());
    }

    /** A contiguous fragment of a text (about half its words), for partial-ayah questions. */
    public static String partial(String text, java.util.Random r) {
        if (text == null) return "";
        String[] w = text.split("\\s+");
        if (w.length <= 3) return text;
        int len = Math.max(2, w.length / 2);
        int start = r.nextInt(w.length - len + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + len; i++) {
            if (i > start) sb.append(' ');
            sb.append(w[i]);
        }
        return sb.toString();
    }
}
