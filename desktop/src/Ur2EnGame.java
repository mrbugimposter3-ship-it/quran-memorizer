import java.awt.Font;
import java.util.List;

/** URDU -> EN: see the Urdu, choose the English meaning. */
public class Ur2EnGame extends QuizGame {

    public Ur2EnGame(MemoriBlitz app) {
        super(app);
        key = "ur2en";
        title = "URDU \u2192 EN";
        blurb = "See the Urdu, choose its English.";
        accent = Theme.PINK;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        List<String> wrong = app.bank.distractorMeanings(target.english, 3, rnd);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(target.english, wrong);
    }
}
