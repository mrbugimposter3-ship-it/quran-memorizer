package com.memoriblitz.quran;

/** URDU -> EN: see the Urdu, choose the English meaning. */
public class Ur2EnActivity extends QuizActivity {

    public Ur2EnActivity() { gameKey = "ur2en"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        prompt(target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu);
        setOptions(target.english, distinct(meanings(), target.english, 3));
    }
}
