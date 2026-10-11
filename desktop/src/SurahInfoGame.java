import java.util.ArrayList;
import java.util.List;

/** SURAH TRIVIA: ayah count and revelation place of a surah. */
public class SurahInfoGame extends QuizGame {

    public SurahInfoGame(MemoriBlitz app) {
        super(app);
        key = "surahinfo";
        title = "SURAH TRIVIA";
        blurb = "Ayah counts and revelation place.";
        accent = Theme.GOLD;
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        QuranData.Surah s = app.bank.quran.surah(target.surah);
        List<String> wrong = new ArrayList<String>();
        String right;
        if (rnd.nextBoolean()) {
            right = String.valueOf(s.ayahCount);
            while (wrong.size() < 3) {
                int v = s.ayahCount + (rnd.nextInt(21) - 10);
                String n = String.valueOf(v);
                if (v > 0 && v != s.ayahCount && !wrong.contains(n)) wrong.add(n);
            }
            prompt = "How many ayahs are in " + s.english + "?";
        } else {
            right = s.type == null ? "Meccan" : s.type;
            boolean meccan = right.toLowerCase().contains("meccan");
            String alt = meccan ? "Medinan" : "Meccan";
            wrong.add(alt);
            wrong.add(meccan ? ("Medinan (" + s.translit + ")") : ("Meccan (" + s.translit + ")"));
            wrong.add(alt + " (revealed in the other city)");
            prompt = "Where was " + s.english + " revealed?";
        }
        setOptions(right, wrong);
    }
}
