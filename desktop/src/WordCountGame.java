import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** WORD COUNT: how many Arabic words does this ayah have? */
public class WordCountGame extends QuizGame {

    public WordCountGame(MemoriBlitz app) {
        super(app);
        key = "wordcount";
        title = "WORD COUNT";
        blurb = "Count the Arabic words of the ayah.";
        accent = Theme.CYAN;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        int n = target.arabic.trim().split("\\s+").length;
        prompt = target.arabic + "  \u00b7  how many words?";
        List<String> wrong = new ArrayList<String>();
        int[] deltas = { -3, -2, -1, 1, 2, 3 };
        while (wrong.size() < 3) {
            int d = deltas[rnd.nextInt(deltas.length)];
            int v = n + d;
            String s = String.valueOf(v);
            if (v > 0 && v != n && !wrong.contains(s)) wrong.add(s);
        }
        setOptions(String.valueOf(n), wrong);
    }
}
