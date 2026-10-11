package com.memoriblitz.quran;

import com.memoriblitz.quran.QuranData.Surah;

import java.util.ArrayList;
import java.util.List;

/** SURAH TRIVIA: ayah count and revelation place of a surah. */
public class SurahInfoActivity extends QuizActivity {

    public SurahInfoActivity() { gameKey = "surahinfo"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        Surah s = quran.surah(target.surah);
        List<String> wrong = new ArrayList<>();
        String right;
        if (rnd.nextBoolean()) {
            right = String.valueOf(s.ayahCount);
            while (wrong.size() < 3) {
                int v = s.ayahCount + (rnd.nextInt(21) - 10);
                String n = String.valueOf(v);
                if (v > 0 && v != s.ayahCount && !wrong.contains(n)) wrong.add(n);
            }
            prompt("How many ayahs are in " + s.english + "?");
        } else {
            right = s.type == null ? "Meccan" : s.type;
            boolean meccan = right.toLowerCase().contains("meccan");
            String alt = meccan ? "Medinan" : "Meccan";
            wrong.add(alt);
            wrong.add(alt + " (alternate)");
            wrong.add(alt + " (in the other city)");
            prompt("Where was " + s.english + " revealed?");
        }
        setOptions(right, wrong);
    }
}
