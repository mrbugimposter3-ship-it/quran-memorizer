package com.memoriblitz.quran;

import java.util.ArrayList;
import java.util.List;

/** WHERE AYAH: shown the Arabic, name the ayah number. */
public class WhereAyahActivity extends QuizActivity {

    public WhereAyahActivity() { gameKey = "whereayah"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String right = "Ayah " + target.ayah;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Ayah " + (1 + rnd.nextInt(286));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }
}
