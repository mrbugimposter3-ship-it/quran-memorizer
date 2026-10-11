import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** WHERE JUZ: shown the Arabic, name the juz. */
public class WhereJuzGame extends QuizGame {

    public WhereJuzGame(MemoriBlitz app) {
        super(app);
        key = "wherejuz";
        title = "WHICH JUZ";
        blurb = "Name the juz of the ayah.";
        accent = Theme.GOLD;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        String right = "Juz " + target.juz;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int j = 1 + rnd.nextInt(30);
            String n = "Juz " + j;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }
}
