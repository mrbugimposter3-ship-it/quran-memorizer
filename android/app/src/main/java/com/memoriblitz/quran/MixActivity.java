package com.memoriblitz.quran;

import com.memoriblitz.quran.QuranData.Ayah;

import java.util.ArrayList;
import java.util.List;

/** MIX: each question is drawn from a random mode. */
public class MixActivity extends QuizActivity {

    public MixActivity() { gameKey = "mix"; }

    @Override protected void generate() {
        switch (rnd.nextInt(13)) {
            case 0: ar2en(); break;
            case 1: en2ar(); break;
            case 2: ur2en(); break;
            case 3: en2ur(); break;
            case 4: ur2ar(); break;
            case 5: ar2tr(); break;
            case 6: partialQ(); break;
            case 7: whereSurah(); break;
            case 8: whereJuz(); break;
            case 9: wherePage(); break;
            case 10: whereAyah(); break;
            case 11: wordCount(); break;
            default: edgeWord(); break;
        }
    }

    private Ayah pick() { return study.get(rnd.nextInt(study.size())); }

    private void ar2en() {
        target = pick();
        prompt(target.arabic);
        setOptions(target.english, distinct(meanings(), target.english, 3));
    }

    private void en2ar() {
        target = pick();
        prompt(target.english);
        setOptions(target.arabic, distinct(arabics(), target.arabic, 3));
    }

    private void ur2en() {
        target = pick();
        prompt(target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu);
        setOptions(target.english, distinct(meanings(), target.english, 3));
    }

    private void en2ur() {
        target = pick();
        String right = target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu;
        prompt(target.english);
        setOptions(right, distinct(urdus().size() >= 3 ? urdus() : meanings(), right, 3));
    }

    private void ur2ar() {
        target = pick();
        prompt(target.urdu == null || target.urdu.trim().isEmpty() ? target.english : target.urdu);
        setOptions(target.arabic, distinct(arabics(), target.arabic, 3));
    }

    private void ar2tr() {
        target = pick();
        prompt(target.arabic);
        setOptions(target.translit, distinct(translits(), target.translit, 3));
    }

    private void partialQ() {
        target = pick();
        prompt(ProgressStore.partial(target.arabic, rnd));
        setOptions(target.arabic, distinct(arabics(), target.arabic, 3));
    }

    private void whereSurah() {
        target = pick();
        String right = quran.surah(target.surah).english;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = quran.surah(1 + rnd.nextInt(quran.surahs.size())).english;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }

    private void whereJuz() {
        target = pick();
        String right = "Juz " + target.juz;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Juz " + (1 + rnd.nextInt(30));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }

    private void wherePage() {
        target = pick();
        String right = "Page " + target.page;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Page " + (1 + rnd.nextInt(604));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }

    private void whereAyah() {
        target = pick();
        String right = "Ayah " + target.ayah;
        List<String> wrong = new ArrayList<>();
        while (wrong.size() < 3) {
            String n = "Ayah " + (1 + rnd.nextInt(286));
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        prompt(target.arabic);
        setOptions(right, wrong);
    }

    private void wordCount() {
        target = pick();
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

    private void edgeWord() {
        target = pick();
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 2) { ar2en(); return; }
        boolean first = rnd.nextBoolean();
        String right = first ? w[0] : w[w.length - 1];
        prompt(target.arabic + "  ·  " + (first ? "first word?" : "last word?"));
        setOptions(right, distinct(arabicWords(), right, 3));
    }
}
