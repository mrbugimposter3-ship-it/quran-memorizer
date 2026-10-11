package com.memoriblitz.quran;

import java.util.ArrayList;
import java.util.List;

/** WHERE JUZ: shown the Arabic, name the juz. */
public class WhereJuzActivity extends QuizActivity {

    public WhereJuzActivity() { gameKey = "wherejuz"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String right = "Juz " + target.juz;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Juz " + (1 + rnd.nextInt(30));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }
}
