import java.util.List;

/** EN -> URDU: see the English, choose the Urdu meaning. */
public class En2UrGame extends QuizGame {

    public En2UrGame(MemoriBlitz app) {
        super(app);
        key = "en2ur";
        title = "EN \u2192 URDU";
        blurb = "See the English, choose its Urdu.";
        accent = Theme.ORANGE;
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.english;
        String right = (target.urdu == null || target.urdu.trim().isEmpty()) ? target.english : target.urdu;
        List<String> wrong = app.bank.distractorUrdu(right, 3, rnd);
        if (wrong.size() < 3) { nextQuestion(); return; }
        setOptions(right, wrong);
    }
}
