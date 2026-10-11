package com.memoriblitz.quran;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;

/** WORD COUNT: count the Arabic words of the ayah. */
public class WordCountActivity extends QuizActivity {

    public WordCountActivity() { gameKey = "wordcount"; }

    @Override protected void generate() {
        target = study.get(rnd.nextInt(study.size()));
        int n = target.arabic.trim().split("\\s+").length;
        int[] deltas = {-3, -2, -1, 1, 2, 3};
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            int v = n + deltas[rnd.nextInt(deltas.length)];
            String s = String.valueOf(v);
            if (v > 0 && v != n && !wrong.contains(s)) wrong.add(s);
        }
        prompt(target.arabic + "  ·  how many words?");
        setOptions(String.valueOf(n), wrong);
    }
}
