import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** FILL-AR: supply the missing Arabic word. */
public class FillArGame extends QuizGame {

    public FillArGame(MemoriBlitz app) {
        super(app);
        key = "fillar";
        title = "FILL-AR";
        blurb = "Supply the missing Arabic word.";
        accent = Theme.ORANGE;
    }

    @Override protected Font promptFont() { return Theme.arabic(24); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        String[] words = target.arabic.split("\\s+");
        List<Integer> candidates = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) if (words[i].length() >= 2) candidates.add(i);
        if (candidates.isEmpty()) { nextQuestion(); return; }
        int blank = candidates.get(rnd.nextInt(candidates.size()));
        String correctWord = words[blank];

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(i == blank ? "____" : words[i]);
        }
        prompt = sb.toString();
        List<String> wrong = distractorArabicWords(correctWord, 3);
        setOptions(correctWord, wrong);
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Vocabulary is solid." : shaky + " ayah(s) still shaky.";
    }
}
