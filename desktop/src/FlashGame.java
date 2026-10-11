import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Random;


/**
 * FLASH - self-graded flashcards drawn from an adaptive deck, so ayahs you
 * keep missing come back far more often. Reveal the meaning, then grade
 * yourself honestly.
 */
public class FlashGame extends GameScreen {

    private final Random rnd = new Random();

    private final Btn btnReveal;
    private final Btn btnGot;
    private final Btn btnMiss;

    private QuranData.Ayah current;
    private boolean revealed;

    public FlashGame(MemoriBlitz app) {
        super(app);
        key = "flash";
        title = "FLASH";
        blurb = "Self-graded cards for the ayahs you keep missing.";
        accent = Theme.VIOLET;

        btnReveal = new Btn(0, 0, 260, 54, "REVEAL MEANING", Theme.GOLD, 0, new Runnable() {
            public void run() { reveal(); }
        });
        btnReveal.key = KeyEvent.VK_SPACE;
        btns.add(btnReveal);

        btnGot = new Btn(0, 0, 220, 54, "GOT IT", Theme.LIME, 0, new Runnable() {
            public void run() { grade(true); }
        });
        btnGot.key = KeyEvent.VK_RIGHT;
        btns.add(btnGot);

        btnMiss = new Btn(0, 0, 220, 54, "MISS", Theme.RED, 1, new Runnable() {
            public void run() { grade(false); }
        });
        btnMiss.key = KeyEvent.VK_LEFT;
        btns.add(btnMiss);
    }

    @Override
    protected int startLives() { return 3; }

    @Override
    protected double startTime() { return -1; }

    @Override
    protected void resetGame() {
        current = null;
        revealed = false;
        nextCard();
    }

    private void nextCard() {
        current = app.bank.adaptiveAyah(rnd);
        revealed = false;
    }

    private void reveal() {
        if (over || countdown > 0) return;
        if (current == null) return;
        if (!revealed) { revealed = true; Sound.pop(); }
    }

    private void grade(boolean gotIt) {
        if (over || countdown > 0) return;
        if (current == null) return;
        if (!revealed) { revealed = true; return; }  // must reveal first
        if (gotIt) {
            int g = award(100, 1.0);
            Sound.good();
            if (combo > 0 && combo % 4 == 0) Sound.combo(mult());
            fx.burst(getWidth() / 2.0, getHeight() * 0.5, Theme.LIME, 30);
            fx.pop(getWidth() / 2.0, getHeight() * 0.42, "+" + g, Theme.LIME, Theme.display(30));
            app.bank.record(current, true);
        } else {
            lives--;
            penalize(-20);
            Sound.wrong();
            kick(0.8);
            fx.flash(Theme.RED);
            app.bank.record(current, false);
            if (lives <= 0) finish();
            else nextCard();
            return;
        }
        nextCard();
    }

    @Override
    protected void round(double dt) {
        // no per-frame timer; self-paced
    }

    @Override
    protected void paintGame(Graphics2D g, int w, int h) {
        if (over) {
            btnReveal.visible = false; btnGot.visible = false; btnMiss.visible = false;
            return;
        }
        if (current == null) return;

        Theme.center(g, "FLASH CARDS", Theme.bold(13), Theme.a(Theme.DIM, 230), w / 2.0, 74);
        Theme.center(g, current.fullRef(), Theme.bold(11), Theme.a(Theme.GOLD, 190), w / 2.0, 92);

        double cardW = Math.min(880, w * 0.80);
        double cardX = (w - cardW) / 2.0;
        double cardY = h * 0.20;
        double cardH = h * 0.46;
        UI.panel(g, cardX, cardY, cardW, cardH, 24, accent);

        // arabic (front)
        Theme.center(g, "ARABIC", Theme.bold(11), Theme.a(Theme.DIM, 200), w / 2.0, cardY + 24);
        int as = q(30);
        List<String> alines = null;
        while (as > 16) {
            Font f = Theme.arabic(as);
            alines = Theme.wrap(g, current.arabic, f, (int) (cardW - 80));
            if (alines.size() <= 2 && alines.size() * (as * 1.4) < cardH * 0.26) break;
            as -= 2;
        }
        if (alines == null) alines = Theme.wrap(g, current.arabic, Theme.arabic(q(18)), (int) (cardW - 80));
        double alh = as * 1.45;
        double aty = cardY + 48 - (alines.size() - 1) * alh / 2.0 + as * 0.36;
        for (int i = 0; i < alines.size(); i++) {
            Theme.shadowCenter(g, alines.get(i), Theme.arabic(as), Theme.GOLD, w / 2.0, aty + i * alh);
        }

        // transliteration (below arabic)
        double tsy = cardY + cardH * 0.30;
        Theme.center(g, "TRANSLITERATION", Theme.bold(10), Theme.a(Theme.DIM, 180), w / 2.0, tsy);
        int ts = q(24);
        List<String> tlines = null;
        while (ts > 12) {
            Font f = Theme.display(ts);
            tlines = Theme.wrap(g, current.translit, f, (int) (cardW - 80));
            if (tlines.size() <= 2 && tlines.size() * (ts + 6) < cardH * 0.16) break;
            ts -= 2;
        }
        if (tlines == null) tlines = Theme.wrap(g, current.translit, Theme.display(q(13)), (int) (cardW - 80));
        double tlh = ts * 1.18;
        double tty = tsy + 18 - (tlines.size() - 1) * tlh / 2.0 + ts * 0.34;
        for (int i = 0; i < tlines.size(); i++) {
            Theme.glowCenter(g, tlines.get(i), Theme.display(ts), Theme.INK, w / 2.0, tty + i * tlh);
        }

        // divider + meaning
        double divY = cardY + cardH * 0.52;
        g.setColor(Theme.a(accent, 70));
        g.drawLine((int) (cardX + 30), (int) divY, (int) (cardX + cardW - 30), (int) divY);

        if (revealed) {
            Theme.center(g, "MEANING", Theme.bold(11), Theme.a(Theme.DIM, 200), w / 2.0, divY + 24);
            int ms = q(24);
            List<String> mlines = null;
            while (ms > 12) {
                Font f = Theme.body(ms);
                mlines = Theme.wrap(g, current.english, f, (int) (cardW - 80));
                if (mlines.size() <= 4 && mlines.size() * (ms + 6) < cardH * 0.44) break;
                ms -= 2;
            }
            if (mlines == null) mlines = Theme.wrap(g, current.english, Theme.body(13), (int) (cardW - 80));
            double mlh = ms * 1.22;
            double mty = divY + 48 - (mlines.size() - 1) * mlh / 2.0 + ms * 0.34;
            for (int i = 0; i < mlines.size(); i++) {
                Theme.shadowCenter(g, mlines.get(i), Theme.body(ms), Theme.GOLD, w / 2.0, mty + i * mlh);
            }
        } else {
            Theme.center(g, "think it through, then reveal", Theme.bold(13), Theme.a(Theme.DIM, 180), w / 2.0, divY + 40);
        }

        // buttons
        double by = cardY + cardH + 30;
        if (!revealed) {
            btnReveal.x = w / 2.0 - 130;
            btnReveal.y = by;
            btnReveal.visible = true;
            btnGot.visible = false;
            btnMiss.visible = false;
        } else {
            btnReveal.visible = false;
            btnGot.x = w / 2.0 - 220 - 12;
            btnGot.y = by;
            btnGot.visible = true;
            btnMiss.x = w / 2.0 + 12;
            btnMiss.y = by;
            btnMiss.visible = true;
        }
    }

    @Override
    protected void onFinish() {
        btnReveal.visible = false; btnGot.visible = false; btnMiss.visible = false;
    }

    @Override
    protected String footerHint() {
        return "SPACE reveal   .   \u2192 GOT IT   .   \u2190 MISS   .   be honest - weak ayahs return more often";
    }

    @Override
    protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        if (shaky == 0) return "Nothing shaky left - raise the pace.";
        return shaky + " ayah" + (shaky == 1 ? "" : "s") + " still shaky - keep going.";
    }
}
