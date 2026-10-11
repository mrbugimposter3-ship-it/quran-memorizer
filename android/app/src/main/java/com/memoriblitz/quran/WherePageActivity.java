package com.memoriblitz.quran;

import java.util.ArrayList;
import java.util.List;

/** WHERE PAGE: shown the Arabic, name the page. */
public class WherePageActivity extends QuizActivity {

    public WherePageActivity() { gameKey = "wherepage"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String right = "Page " + target.page;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Page " + (1 + rnd.nextInt(604));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }
}
