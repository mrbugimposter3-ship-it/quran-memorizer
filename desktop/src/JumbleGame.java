import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** JUMBLE: unscramble the scrambled letters of a word. */
public class JumbleGame extends QuizGame {

    public JumbleGame(MemoriBlitz app) {
        super(app);
        key = "jumble";
        title = "JUMBLE";
        blurb = "Unscramble the letters.";
        accent = Theme.GOLD;
    }

    @Override protected Font promptFont() { return Theme.mono(30); }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        String[] words = target.translit.split("\\s+");
        List<String> candidates = new ArrayList<String>();
        for (String w : words) if (w.length() >= 4) candidates.add(w);
        if (candidates.isEmpty()) { nextQuestion(); return; }
        String correctWord = candidates.get(rnd.nextInt(candidates.size()));
        prompt = scramble(correctWord);

        List<String> wrong = new ArrayList<String>();
        while (wrong.size() < 3) {
            QuranData.Ayah a = app.bank.randomAyah(rnd);
            if (a == null) break;
            String[] aw = a.translit.split("\\s+");
            if (aw.length == 0) continue;
            String w = aw[rnd.nextInt(aw.length)];
            if (!w.equals(correctWord) && !wrong.contains(w)) wrong.add(w);
        }
        setOptions(correctWord, wrong);
    }

    private String scramble(String w) {
        List<Character> chars = new ArrayList<Character>();
        for (char c : w.toCharArray()) chars.add(c);
        String s;
        do {
            Collections.shuffle(chars, rnd);
            StringBuilder sb = new StringBuilder();
            for (char c : chars) sb.append(c);
            s = sb.toString();
        } while (s.equals(w));
        return s;
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Spelling is sharp." : shaky + " ayah(s) still shaky.";
    }
}
