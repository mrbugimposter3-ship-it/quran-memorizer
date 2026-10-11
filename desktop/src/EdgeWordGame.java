import java.awt.Font;
import java.util.List;

/** EDGE: recall the first or last Arabic word of the ayah. */
public class EdgeWordGame extends QuizGame {

    private boolean first;

    public EdgeWordGame(MemoriBlitz app) {
        super(app);
        key = "edgeword";
        title = "FIRST / LAST";
        blurb = "Recall the first or last word of the ayah.";
        accent = Theme.GOLD;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 2) { nextQuestion(); return; }
        first = rnd.nextBoolean();
        String right = first ? w[0] : w[w.length - 1];
        prompt = target.arabic + "  \u00b7  " + (first ? "first word?" : "last word?");
        List<String> wrong = distractorArabicWords(right, 3);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(right, wrong);
    }
}
