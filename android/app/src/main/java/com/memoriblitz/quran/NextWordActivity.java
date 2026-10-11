package com.memoriblitz.quran;

/** NEXT WORD: supply the final word of the ayah. */
public class NextWordActivity extends QuizActivity {

    public NextWordActivity() { gameKey = "nextword"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 3) { generate(); return; }
        String right = w[w.length - 1];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length - 1; i++) {
            if (i > 0) sb.append(' ');
            sb.append(w[i]);
        }
        prompt(sb.toString() + "  ·  ____");
        setOptions(right, distinct(arabicWords(), right, 3));
    }
}
