package com.memoriblitz.quran;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;

/** ODD ONE OUT: three ayahs share a surah, one is from elsewhere. */
public class OddOneActivity extends QuizActivity {

    public OddOneActivity() { gameKey = "oddone"; }

    @Override protected void generate() {
        List<Integer> surahs = new ArrayList<>();
        for (Ayah a : study) if (!surahs.contains(a.surah)) surahs.add(a.surah);
        int fam = -1;
        for (int s : surahs) { if (count(s) >= 3) { fam = s; break; } }
        if (fam < 0) { fallback(); return; }
        List<String> wrong = new ArrayList<>();
        for (Ayah a : study) if (a.surah == fam && wrong.size() < 3) wrong.add(a.english);
        Ayah odd = null;
        for (Ayah a : study) if (a.surah != fam) { odd = a; break; }
        if (wrong.size() < 3 || odd == null) { fallback(); return; }
        target = odd;
        prompt("Pick the ayah NOT from " + quran.surah(fam).english + ".");
        setOptions(odd.english, wrong);
    }

    private int count(int s) { int c = 0; for (Ayah a : study) if (a.surah == s) c++; return c; }

    private void fallback() {
        target = study.get(rnd.nextInt(study.size()));
        prompt(target.arabic);
        setOptions(target.english, distinct(meanings(), target.english, 3));
    }
}
