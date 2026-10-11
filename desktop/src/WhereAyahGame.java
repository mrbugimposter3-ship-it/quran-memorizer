import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** WHERE AYAH: shown the Arabic, name the ayah number. */
public class WhereAyahGame extends QuizGame {

    public WhereAyahGame(MemoriBlitz app) {
        super(app);
        key = "whereayah";
        title = "AYAH NUMBER";
        blurb = "Name the ayah number.";
        accent = Theme.PINK;
    }

    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        prompt = target.arabic;
        String right = "Ayah " + target.ayah;
        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            int ar = 1 + rnd.nextInt(286);
            String n = "Ayah " + ar;
            if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
        }
        setOptions(right, wrong);
    }
}
