import java.awt.Font;
import java.util.List;

/** NEXT: shown an ayah, pick the ayah that follows it. */
public class NextGame extends QuizGame {

    public NextGame(MemoriBlitz app) {
        super(app);
        key = "next";
        title = "NEXT";
        blurb = "Pick the ayah that follows.";
        accent = Theme.VIOLET;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        QuranData.Ayah next = nextAyah(target);
        prompt = target.arabic;
        List<String> wrong = app.bank.distractorTranslits(next.translit, 3, rnd);
        setOptions(next.translit, wrong);
    }

    private QuranData.Ayah nextAyah(QuranData.Ayah a) {
        List<QuranData.Ayah> all = app.bank.quran.ayahs;
        int g = a.global + 1;
        if (g >= all.size()) g = 0;
        return all.get(g);
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Sequencing is solid." : shaky + " ayah(s) still shaky.";
    }
}
