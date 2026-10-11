package com.memoriblitz.quran;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;

/**
 * NUMBER STORY - each ayah is turned into a one-line "number story": its
 * surah, ayah, juz, hizb, page and position-on-page are encoded as famous-book
 * names. One number is blanked each round and you recall it from its story.
 */
public class StoryActivity extends QuizActivity {

    private static final String[] FIELD = { "SURAH", "AYAH", "JUZ", "HIZB", "PAGE", "POS" };
    private final int[] juzMin = new int[31];
    private final int[] juzMax = new int[31];
    private boolean juzInit = false;

    public StoryActivity() { gameKey = "story"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        int qf = rnd.nextInt(6);
        int[] vals = { target.surah, target.ayah, target.juz, hizb(target), target.page, posOnPage(target) };

        StringBuilder sb = new StringBuilder();
        sb.append(target.arabic).append("\n\n");
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append("   \u00b7   ");
            sb.append(FIELD[i]).append(' ')
              .append(i == qf ? "?" : String.valueOf(vals[i]))
              .append('=').append(Mnemonic.story(vals[i]));
        }
        sb.append("\n\nWhat ").append(FIELD[qf]).append(" is this ayah?");
        prompt(sb.toString());

        String right = String.valueOf(vals[qf]);
        int[] rng = range(qf);
        List<String> wrong = new ArrayList<>();
        int guard = 0;
        while (wrong.size() < 3 && guard++ < 300) {
            int d = rnd.nextInt(21) - 10;
            if (d == 0) d = 6;
            int v = vals[qf] + d;
            if (v >= rng[0] && v <= rng[1] && v != vals[qf] && !wrong.contains(String.valueOf(v))) {
                wrong.add(String.valueOf(v));
            }
        }
        setOptions(right, wrong);
    }

    private int[] range(int f) {
        switch (f) {
            case 0: return new int[]{1, 114};
            case 1: return new int[]{1, 286};
            case 2: return new int[]{1, 30};
            case 3: return new int[]{1, 60};
            case 4: return new int[]{1, 604};
            default: return new int[]{1, 30};
        }
    }

    private int hizb(Ayah a) {
        if (!juzInit) {
            juzInit = true;
            for (Ayah x : quran.ayahs) {
                if (juzMin[x.juz] == 0 || x.page < juzMin[x.juz]) juzMin[x.juz] = x.page;
                if (x.page > juzMax[x.juz]) juzMax[x.juz] = x.page;
            }
        }
        int span = juzMax[a.juz] - juzMin[a.juz] + 1;
        boolean first = (a.page - juzMin[a.juz]) < span / 2.0;
        return (a.juz - 1) * 2 + (first ? 1 : 2);
    }

    private int posOnPage(Ayah a) {
        int c = 0;
        for (Ayah x : quran.ayahs) {
            if (x.page == a.page && x.global < a.global) c++;
        }
        return c + 1;
    }
}
