package com.memoriblitz.quran;

/** ARABIC -> TRANSLIT: see the Arabic, choose the transliteration. */
public class Ar2TrActivity extends QuizActivity {

    public Ar2TrActivity() { gameKey = "ar2tr"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        prompt(target.arabic);
        setOptions(target.translit, distinct(translits(), target.translit, 3));
    }
}
