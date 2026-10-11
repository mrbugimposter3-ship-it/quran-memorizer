import java.awt.Font;
import java.util.List;

/** PARTIAL: identify the full ayah from a fragment of its Arabic text. */
public class PartialGame extends QuizGame {

    public PartialGame(MemoriBlitz app) {
        super(app);
        key = "partial";
        title = "PARTIAL AYAH";
        blurb = "Identify the full ayah from a fragment.";
        accent = Theme.CYAN;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = app.bank.partial(target.arabic, rnd);
        List<String> wrong = distractorArabic(target.arabic, 3);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(target.arabic, wrong);
    }
}
