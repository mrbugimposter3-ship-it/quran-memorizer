import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** WHERE PAGE: shown the Arabic, name the page. */
public class WherePageGame extends QuizGame {

    public WherePageGame(MemoriBlitz app) {
        super(app);
        key = "wherepage";
        title = "WHICH PAGE";
        blurb = "Name the page of the ayah.";
        accent = Theme.VIOLET;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        String right = "Page " + target.page;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int p = 1 + rnd.nextInt(604);
            String n = "Page " + p;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }
}
