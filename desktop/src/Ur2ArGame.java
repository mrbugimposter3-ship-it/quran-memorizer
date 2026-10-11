import java.awt.Font;
import java.util.List;

/** URDU -> ARABIC: see the Urdu meaning, choose the Arabic ayah. */
public class Ur2ArGame extends QuizGame {

    public Ur2ArGame(MemoriBlitz app) {
        super(app);
        key = "ur2ar";
        title = "URDU \u2192 ARABIC";
        blurb = "See the Urdu, choose the Arabic.";
        accent = Theme.LIME;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        List<String> wrong = distractorArabic(target.arabic, 3);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(target.arabic, wrong);
    }
}
