package com.memoriblitz.quran;

/** URDU -> ARABIC: see the Urdu meaning, choose the Arabic ayah. */
public class Ur2ArActivity extends QuizActivity {

    public Ur2ArActivity() { gameKey = "ur2ar"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        prompt(target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu);
        setOptions(target.arabic, distinct(arabics(), target.arabic, 3));
    }
}
