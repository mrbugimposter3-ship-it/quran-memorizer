import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** ORDER: tap the ayah's words in the correct sequence. */
public class OrderGame extends GameScreen {

    private final Random rnd = new Random();
    private QuranData.Ayah target;
    private String[] words;
    private final List<String> remaining = new ArrayList<String>();
    private final StringBuilder built = new StringBuilder();
    private final List<Btn> wordBtns = new ArrayList<Btn>();

    public OrderGame(MemoriBlitz app) {
        super(app);
        key = "order";
        title = "ORDER";
        blurb = "Tap the words in the correct order.";
        accent = Theme.GOLD;
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
        remaining.clear();
        Collections.addAll(remaining, words);
        Collections.shuffle(remaining, rnd);
        built.setLength(0);
        renderBank();
    }

    private void renderBank() {
        for (Btn b : wordBtns) btns.remove(b);
        wordBtns.clear();
        for (final String w : remaining) {
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
            remaining.remove(w);
            renderBank();
            if (remaining.isEmpty()) {
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

        Theme.center(g, built.length() == 0 ? "..." : built.toString(),
                Theme.body(q(20)), Theme.LIME, w / 2.0, h * 0.20);

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
        return "tap the words in order";
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Word order is solid." : shaky + " ayah(s) still shaky.";
    }
}
