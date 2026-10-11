import java.awt.Font;
import java.util.List;

/** ARABIC: see the Arabic, choose the correct English meaning. */
public class ArabicGame extends QuizGame {

    public ArabicGame(MemoriBlitz app) {
        super(app);
        key = "arabic";
        title = "ARABIC";
        blurb = "See the Arabic, choose its meaning.";
        accent = Theme.ORANGE;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        if (rnd.nextDouble() < 0.35) prompt = app.bank.partial(target.arabic, rnd);
        List<String> wrong = app.bank.distractorMeanings(target.english, 3, rnd);
        setOptions(target.english, wrong);
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Reading is sharp." : shaky + " ayah(s) still shaky.";
    }
}
