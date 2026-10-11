package com.memoriblitz.quran;

/** PARTIAL: identify the full ayah from a fragment of its Arabic. */
public class PartialActivity extends QuizActivity {

    public PartialActivity() { gameKey = "partial"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        prompt(ProgressStore.partial(target.arabic, rnd));
        setOptions(target.arabic, distinct(arabics(), target.arabic, 3));
    }
}
