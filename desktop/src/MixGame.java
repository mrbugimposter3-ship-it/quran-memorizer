import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/**
 * MIX - every question is drawn from a randomly chosen mode, cycling through
 * the whole roster so you can't settle into one pattern. Each question reuses
 * the same generator logic as the dedicated games.
 */
public class MixGame extends QuizGame {

    private boolean promptIsArabic = false;

    public MixGame(MemoriBlitz app) {
        super(app);
        key = "mix";
        title = "MIX";
        blurb = "Cycles through every mode at random.";
        accent = Theme.CYAN;
    }

    @Override protected double startTime() { return 90; }

    @Override protected Font promptFont() {
        return promptIsArabic ? Theme.arabic(26) : Theme.display(22);
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        int t = rnd.nextInt(13);
        switch (t) {
            case 0:  ar2En(); break;
            case 1:  en2Ar(); break;
            case 2:  ur2En(); break;
            case 3:  en2Ur(); break;
            case 4:  ur2Ar(); break;
            case 5:  ar2Tr(); break;
            case 6:  partialQ(); break;
            case 7:  whereSurah(); break;
            case 8:  whereJuz(); break;
            case 9:  wherePage(); break;
            case 10: whereAyah(); break;
            case 11: wordCount(); break;
            default: edgeWord(); break;
        }
    }

    /* ---- question generators (set target, prompt, promptIsArabic, options) ---- */

    private boolean pick() {
        target = app.bank.randomAyah(rnd);
        return target != null;
    }

    private void ar2En() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        if (rnd.nextDouble() < 0.3) prompt = app.bank.partial(target.arabic, rnd);
        setOptions(target.english, app.bank.distractorMeanings(target.english, 3, rnd));
    }

    private void en2Ar() {
        if (!pick()) { finish(); return; }
        promptIsArabic = false;
        prompt = target.english;
        setOptions(target.arabic, distractorArabic(target.arabic, 3));
    }

    private void ur2En() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        setOptions(target.english, app.bank.distractorMeanings(target.english, 3, rnd));
    }

    private void en2Ur() {
        if (!pick()) { finish(); return; }
        promptIsArabic = false;
        prompt = target.english;
        String right = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        setOptions(right, app.bank.distractorUrdu(right, 3, rnd));
    }

    private void ur2Ar() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        setOptions(target.arabic, distractorArabic(target.arabic, 3));
    }

    private void ar2Tr() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        setOptions(target.translit, app.bank.distractorTranslits(target.translit, 3, rnd));
    }

    private void partialQ() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = app.bank.partial(target.arabic, rnd);
        setOptions(target.arabic, distractorArabic(target.arabic, 3));
    }

    private void whereSurah() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        String right = app.bank.quran.surah(target.surah).english;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int s = 1 + rnd.nextInt(app.bank.quran.surahs.size());
            String n = app.bank.quran.surah(s).english;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }

    private void whereJuz() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        String right = "Juz " + target.juz;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int j = 1 + rnd.nextInt(30);
            String n = "Juz " + j;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }

    private void wherePage() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        String right = "Page " + target.page;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int p = 1 + rnd.nextInt(604);
            String n = "Page " + p;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }

    private void whereAyah() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        prompt = target.arabic;
        String right = "Ayah " + target.ayah;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int ar = 1 + rnd.nextInt(286);
            String n = "Ayah " + ar;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }

    private void wordCount() {
        if (!pick()) { finish(); return; }
        promptIsArabic = true;
        int n = target.arabic.trim().split("\\s+").length;
        prompt = target.arabic + "  \u00b7  how many words?";
        List<String> wrong = new ArrayList<String>();
        int[] deltas = { -3, -2, -1, 1, 2, 3 };
        while (wrong.size() < 3) {
            int d = deltas[rnd.nextInt(deltas.length)];
            int v = n + d;
            String s = String.valueOf(v);
            if (v > 0 && v != n && !wrong.contains(s)) wrong.add(s);
        }
        setOptions(String.valueOf(n), wrong);
    }

    private void edgeWord() {
        if (!pick()) { finish(); return; }
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 2) { nextQuestion(); return; }
        promptIsArabic = true;
        boolean first = rnd.nextBoolean();
        String right = first ? w[0] : w[w.length - 1];
        prompt = target.arabic + "  \u00b7  " + (first ? "first word?" : "last word?");
        setOptions(right, distractorArabicWords(right, 3));
    }
}
