import java.awt.Font;
import java.util.List;

/** NEXT WORD: the ayah with its final word hidden - supply that word. */
public class NextWordGame extends QuizGame {

    public NextWordGame(MemoriBlitz app) {
        super(app);
        key = "nextword";
        title = "NEXT WORD";
        blurb = "Supply the final word of the ayah.";
        accent = Theme.ORANGE;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        String[] w = target.arabic.trim().split("\\s+");
        if (w.length < 3) { nextQuestion(); return; }
        String right = w[w.length - 1];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length - 1; i++) { if (i > 0) sb.append(' '); sb.append(w[i]); }
        prompt = sb.toString() + "  \u00b7  ____";
        List<String> wrong = distractorArabicWords(right, 3);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(right, wrong);
    }
}
