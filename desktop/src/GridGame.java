import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** GRID: classic memory game — flip cards to match Arabic with its meaning. */
public class GridGame extends GameScreen {

    private static class Card {
        QuranData.Ayah ayah;
        boolean arabic;
        String text;
        Btn btn;
        boolean matched;
    }

    private final Random rnd = new Random();
    private final List<Card> cards = new ArrayList<Card>();
    private Card first;
    private boolean locked;
    private double lockT;

    public GridGame(MemoriBlitz app) {
        super(app);
        key = "grid";
        title = "GRID";
        blurb = "Memory game: match Arabic with its meaning.";
        accent = Theme.VIOLET;
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    @Override protected void resetGame() {
        first = null;
        locked = false;
        lockT = 0;
        newRound();
    }

    private void newRound() {
        if (app.bank.size() < 2) { finish(); return; }
        int n = Math.min(4, app.bank.size() / 2);
        List<QuranData.Ayah> picked = new ArrayList<QuranData.Ayah>();
        while (picked.size() < n) {
            QuranData.Ayah a = app.bank.randomAyah(rnd);
            if (a != null && !picked.contains(a)) picked.add(a);
        }
        for (Card c : cards) btns.remove(c.btn);
        cards.clear();
        for (QuranData.Ayah a : picked) {
            Card ar = new Card(); ar.ayah = a; ar.arabic = true; ar.text = a.arabic;
            Card en = new Card(); en.ayah = a; en.arabic = false; en.text = a.english;
            cards.add(ar); cards.add(en);
        }
        Collections.shuffle(cards, rnd);
        for (final Card c : cards) {
            c.btn = new Btn(0, 0, 10, 10, "?", accent, 2, new Runnable() {
                public void run() { tap(c); }
            });
            c.btn.visible = false;
            btns.add(c.btn);
        }
        first = null;
        locked = false;
    }

    private void tap(Card c) {
        if (over || countdown > 0 || locked || c.matched || c == first) return;
        if (first == null) {
            first = c;
            return;
        }
        locked = true;
        lockT = 0.7;
        if (first.ayah == c.ayah && first.arabic != c.arabic) {
            first.matched = true;
            c.matched = true;
            first = null;
            locked = false;
            lockT = 0;
            award(100, 1.0);
            Sound.good();
            app.bank.record(c.ayah, true);
            boolean allDone = true;
            for (Card x : cards) if (!x.matched) allDone = false;
            if (allDone) newRound();
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            app.bank.record(c.ayah, false);
        }
    }

    @Override protected void round(double dt) {
        if (locked && lockT > 0) {
            lockT -= dt;
            if (lockT <= 0) {
                first = null;
                locked = false;
                if (lives <= 0) finish();
            }
        }
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { for (Card c : cards) c.btn.visible = false; return; }
        double gridW = Math.min(900, w - 60);
        double gx = (w - gridW) / 2.0;
        double gy = h * 0.28;
        double gap = 12;
        int cols = 4;
        double bw = (gridW - gap * (cols - 1)) / cols;
        double bh = Math.min(90, h * 0.14);
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int col = i % cols;
            int row = i / cols;
            c.btn.x = gx + col * (bw + gap);
            c.btn.y = gy + row * (bh + gap);
            c.btn.w = bw;
            c.btn.h = bh;
            c.btn.visible = true;
            c.btn.fontPx = q((int) Math.max(12, Math.min(bh * 0.26, 16)));
            boolean faceUp = c.matched || c == first || (locked && c == first);
            c.btn.label = (c.matched || c == first) ? c.text : "?";
            if (c.matched) c.btn.color = Theme.LIME;
            else if (c == first) c.btn.color = Theme.GOLD;
            else c.btn.color = accent;
        }
    }

    @Override protected void onFinish() {
        for (Card c : cards) c.btn.visible = false;
    }

    @Override protected String footerHint() {
        return "flip two cards to match Arabic with its meaning";
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Memory is sharp." : shaky + " ayah(s) still shaky.";
    }
}
