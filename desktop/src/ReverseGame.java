import java.util.List;

/** REVERSE: see the English meaning, choose the correct Arabic text. */
public class ReverseGame extends QuizGame {

    public ReverseGame(MemoriBlitz app) {
        super(app);
        key = "reverse";
        title = "REVERSE";
        blurb = "See the meaning, choose the Arabic.";
        accent = Theme.PINK;
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.english;
        List<String> wrong = distractorArabic(target.arabic, 3);
        setOptions(target.arabic, wrong);
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Recall is sharp." : shaky + " ayah(s) still shaky.";
    }
}
