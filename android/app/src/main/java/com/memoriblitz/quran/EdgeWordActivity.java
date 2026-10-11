package com.memoriblitz.quran;

/** EDGE: recall the first or last Arabic word of the ayah. */
public class EdgeWordActivity extends QuizActivity {

    public EdgeWordActivity() { gameKey = "edgeword"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 2) { generate(); return; }
        boolean first = rnd.nextBoolean();
        String right = first ? w[0] : w[w.length - 1];
        prompt(target.arabic + "  ·  " + (first ? "first word?" : "last word?"));
        setOptions(right, distinct(arabicWords(), right, 3));
    }
}
