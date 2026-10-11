import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** PAIR: match Arabic phrases to their transliterations. */
public class PairGame extends GameScreen {

    private static class Card {
        QuranData.Ayah ayah;
        boolean arabic;
        String text;
        Btn btn;
        boolean done;
    }

    private final Random rnd = new Random();
    private final List<Card> cards = new ArrayList<Card>();
    private Card selected;

    public PairGame(MemoriBlitz app) {
        super(app);
        key = "pair";
        title = "PAIR";
        blurb = "Match Arabic phrases to their transliterations.";
        accent = Theme.PINK;
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    @Override protected void resetGame() {
        selected = null;
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
            Card tr = new Card(); tr.ayah = a; tr.arabic = false; tr.text = a.translit;
            cards.add(ar); cards.add(tr);
        }
        Collections.shuffle(cards, rnd);
        for (final Card c : cards) {
            c.btn = new Btn(0, 0, 10, 10, c.text, accent, 2, new Runnable() {
                public void run() { tap(c); }
            });
            c.btn.visible = false;
            btns.add(c.btn);
        }
        selected = null;
    }

    private void tap(Card c) {
        if (over || countdown > 0 || c.done) return;
        if (selected == null) { selected = c; return; }
        if (selected == c) { selected = null; return; }
        if (selected.ayah == c.ayah && selected.arabic != c.arabic) {
            selected.done = true;
            c.done = true;
            selected = null;
            award(100, 1.0);
            Sound.good();
            app.bank.record(c.ayah, true);
            boolean allDone = true;
            for (Card x : cards) if (!x.done) allDone = false;
            if (allDone) newRound();
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            app.bank.record(c.ayah, false);
            selected = null;
            if (lives <= 0) finish();
        }
    }

    @Override protected void round(double dt) {}

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { for (Card c : cards) c.btn.visible = false; return; }
        double gridW = Math.min(900, w - 60);
        double gx = (w - gridW) / 2.0;
        double gy = h * 0.30;
        double gap = 12;
        int cols = 2;
        double bw = (gridW - gap * (cols - 1)) / cols;
        double bh = Math.min(70, h * 0.11);
        for (int i = 0; i < cards.size(); i++) {
            Card c = cards.get(i);
            int col = i % cols;
            int row = i / cols;
            c.btn.x = gx + col * (bw + gap);
            c.btn.y = gy + row * (bh + gap);
            c.btn.w = bw;
            c.btn.h = bh;
            c.btn.visible = true;
            c.btn.fontPx = q((int) Math.max(13, Math.min(bh * 0.30, 18)));
            if (c.done) c.btn.color = Theme.LIME;
            else if (c == selected) c.btn.color = Theme.GOLD;
            else c.btn.color = accent;
        }
    }

    @Override protected void onFinish() {
        for (Card c : cards) c.btn.visible = false;
    }

    @Override protected String footerHint() {
        return "tap an Arabic phrase, then its transliteration";
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Pairing is solid." : shaky + " ayah(s) still shaky.";
    }
}
