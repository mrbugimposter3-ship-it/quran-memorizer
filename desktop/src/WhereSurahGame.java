import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** WHERE SURAH: shown the Arabic, name the surah. */
public class WhereSurahGame extends QuizGame {

    public WhereSurahGame(MemoriBlitz app) {
        super(app);
        key = "wheresurah";
        title = "WHICH SURAH";
        blurb = "Name the surah of the ayah.";
        accent = Theme.CYAN;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
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
}
