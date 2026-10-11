package com.memoriblitz.quran;

import java.util.ArrayList;
import java.util.List;

/** WHERE SURAH: shown the Arabic, name the surah. */
public class WhereSurahActivity extends QuizActivity {

    public WhereSurahActivity() { gameKey = "wheresurah"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        String right = quran.surah(target.surah).english;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = quran.surah(1 + rnd.nextInt(quran.surahs.size())).english;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }
}
