package com.memoriblitz.quran;

/** EN -> URDU: see the English, choose the Urdu meaning. */
public class En2UrActivity extends QuizActivity {

    public En2UrActivity() { gameKey = "en2ur"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String right = target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu;
        prompt(target.english);
        setOptions(right, distinct(urdus().size() >= 3 ? urdus() : meanings(), right, 3));
    }
}
