import java.awt.Font;
import java.util.List;

/** ARABIC -> TRANSLIT: see the Arabic, choose the transliteration. */
public class Ar2TrGame extends QuizGame {

    public Ar2TrGame(MemoriBlitz app) {
        super(app);
        key = "ar2tr";
        title = "AR \u2192 TRANSLIT";
        blurb = "See the Arabic, choose its transliteration.";
        accent = Theme.VIOLET;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        List<String> wrong = app.bank.distractorTranslits(target.translit, 3, rnd);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(target.translit, wrong);
    }
}
