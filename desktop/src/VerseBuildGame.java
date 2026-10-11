import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** VERSE BUILD: build the ayah word-by-word from a pool (with distractors). */
public class VerseBuildGame extends GameScreen {

    private final Random rnd = new Random();
    private QuranData.Ayah target;
    private String[] words;
    private final List<String> pool = new ArrayList<String>();
    private final StringBuilder built = new StringBuilder();
    private final List<Btn> wordBtns = new ArrayList<Btn>();

    public VerseBuildGame(MemoriBlitz app) {
        super(app);
        key = "versebuild";
        title = "VERSE BUILD";
        blurb = "Build the ayah word by word.";
        accent = Theme.LIME;
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    @Override protected void resetGame() {
        target = null;
        nextCard();
    }

    private void nextCard() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        words = target.translit.split("\\s+");
        if (words.length > 16) { nextCard(); return; }
        pool.clear();
        Collections.addAll(pool, words);
        int need = Math.min(6, words.length + 4);
        while (pool.size() < need) {
            QuranData.Ayah a = app.bank.randomAyah(rnd);
            if (a == null) break;
            String[] aw = a.translit.split("\\s+");
            if (aw.length == 0) continue;
            String w = aw[rnd.nextInt(aw.length)];
            if (!pool.contains(w)) pool.add(w);
        }
        Collections.shuffle(pool, rnd);
        built.setLength(0);
        renderBank();
    }

    private void renderBank() {
        for (Btn b : wordBtns) btns.remove(b);
        wordBtns.clear();
        for (final String w : pool) {
            Btn b = new Btn(0, 0, 10, 10, w, accent, 2, new Runnable() {
                public void run() { tap(w); }
            });
            b.visible = false;
            btns.add(b);
            wordBtns.add(b);
        }
    }

    private int wordIndex() {
        return built.length() == 0 ? 0 : built.toString().split("\\s+").length;
    }

    private void tap(String w) {
        if (over || countdown > 0) return;
        String expect = words[wordIndex()];
        if (w.equals(expect)) {
            if (built.length() > 0) built.append(' ');
            built.append(w);
            if (wordIndex() == words.length) {
                award(100, 1.0);
                Sound.good();
                app.bank.record(target, true);
                nextCard();
            }
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            app.bank.record(target, false);
            if (lives <= 0) { finish(); return; }
        }
    }

    @Override protected void round(double dt) {}

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { for (Btn b : wordBtns) b.visible = false; return; }
        if (target == null) return;

        Font f = Theme.body(q(16));
        List<String> lines = Theme.wrap(g, target.english, f, (int) (w * 0.7));
        if (lines == null) lines = Theme.wrap(g, target.english, Theme.body(q(14)), (int) (w * 0.7));
        double lh = 24;
        double ty = h * 0.16 - (lines.size() - 1) * lh / 2.0;
        for (int i = 0; i < lines.size(); i++) {
            Theme.center(g, lines.get(i), f, Theme.DIM, w / 2.0, ty + i * lh);
        }

        Theme.center(g, built.length() == 0 ? "..." : built.toString(),
                Theme.body(q(20)), Theme.LIME, w / 2.0, h * 0.24);

        double gridW = Math.min(900, w - 60);
        double gx = (w - gridW) / 2.0;
        double gy = h * 0.32;
        double gap = 12;
        int cols = 4;
        double bw = (gridW - gap * (cols - 1)) / cols;
        double bh = 52;
        for (int i = 0; i < wordBtns.size(); i++) {
            Btn b = wordBtns.get(i);
            int col = i % cols;
            int row = i / cols;
            b.x = gx + col * (bw + gap);
            b.y = gy + row * (bh + gap);
            b.w = bw;
            b.h = bh;
            b.visible = true;
            b.fontPx = q((int) Math.max(14, Math.min(bh * 0.32, 20)));
        }
    }

    @Override protected void onFinish() {
        for (Btn b : wordBtns) b.visible = false;
    }

    @Override protected String footerHint() {
        return "tap the words to build the ayah in order";
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Building is solid." : shaky + " ayah(s) still shaky.";
    }
}
