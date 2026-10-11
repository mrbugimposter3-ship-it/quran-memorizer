import java.awt.Font;
import java.util.List;

/** URDU: see the Arabic, choose the correct Urdu meaning. */
public class UrduGame extends QuizGame {

    public UrduGame(MemoriBlitz app) {
        super(app);
        key = "urdu";
        title = "URDU";
        blurb = "See the Arabic, choose its Urdu meaning.";
        accent = Theme.ORANGE;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        String right = target.urdu == null ? target.english : target.urdu;
        List<String> wrong = app.bank.distractorUrdu(right, 3, rnd);
        setOptions(right, wrong);
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Urdu reading is sharp." : shaky + " ayah(s) still shaky.";
    }
}
