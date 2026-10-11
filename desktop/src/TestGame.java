import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * TEST - a transliteration (or its English meaning) appears and you smash the
 * matching option before the bar drains. Speed feeds the score, correct
 * answers build a combo multiplier, three mistakes end the run.
 */
public class TestGame extends GameScreen {

    private final Random rnd = new Random();
    private final Btn[] optBtn = new Btn[4];
    private final String[] optText = new String[4];
    private final double[] optCx = new double[4];
    private final double[] optCy = new double[4];

    private QuranData.Ayah target;
    private boolean askPhrase;
    private String prompt = "";
    private int answerIdx = -1;
    private double limit = 8, left = 8;
    private double revealT;
    private int lastGain;

    public TestGame(MemoriBlitz app) {
        super(app);
        key = "test";
        title = "TEST";
        blurb = "Match the ayah to its meaning before the bar drains.";
        accent = Theme.CYAN;

        String[] hot = { "1", "2", "3", "4" };
        int[] vk = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4 };
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optBtn[i] = new Btn(0, 0, 10, 10, "", Theme.CYAN, 2, new Runnable() {
                public void run() { choose(idx); }
            });
            optBtn[i].badge = hot[i];
            optBtn[i].key = vk[i];
            optBtn[i].visible = false;
            btns.add(optBtn[i]);
        }
    }

    @Override
    protected int startLives() { return 3; }

    @Override
    protected double startTime() { return -1; }

    @Override
    protected void resetGame() {
        target = null;
        revealT = 0;
        nextQuestion();
    }

    private void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        askPhrase = rnd.nextBoolean();

        String right;
        List<String> wrong = new ArrayList<String>();
        if (askPhrase) {
            prompt = target.translit;
            if (rnd.nextDouble() < 0.35) prompt = app.bank.partial(target.translit, rnd);
            right = target.english;
            wrong.addAll(app.bank.distractorMeanings(right, 3, rnd));
        } else {
            prompt = target.english;
            right = target.translit;
            wrong.addAll(app.bank.distractorTranslits(right, 3, rnd));
        }

        List<String> pool = new ArrayList<String>();
        pool.add(right);
        pool.addAll(wrong);
        Collections.shuffle(pool, rnd);

        for (int i = 0; i < 4; i++) optText[i] = i < pool.size() ? pool.get(i) : null;
        answerIdx = pool.indexOf(right);
        if (answerIdx < 0) answerIdx = 0;

        for (int i = 0; i < 4; i++) optBtn[i].flash = 0;

        limit = Math.max(4.0, 9.0 - level * 0.5);
        left = limit;
    }

    private void choose(int idx) {
        if (over || countdown > 0) return;
        if (revealT > 0) { revealT = 0.0001; return; }
        if (idx < 0 || idx >= 4 || optText[idx] == null) return;
        answer(idx);
    }

    private void answer(int idx) {
        boolean right = idx == answerIdx;
        if (right) {
            double speed = 0.35 + 0.65 * Theme.clamp01(limit > 0 ? left / limit : 0);
            lastGain = award(100, speed);
            Sound.good();
            if (combo > 0 && combo % 4 == 0) Sound.combo(mult());
            fx.burst(optCx[idx], optCy[idx], Theme.LIME, 34);
            fx.ring(optCx[idx], optCy[idx], Theme.LIME, 100, 4, 0.45);
            fx.pop(optCx[idx], optCy[idx] - 26, "+" + lastGain, Theme.LIME, Theme.display(30));
            optBtn[idx].flash(Theme.LIME);
            app.bank.record(target, true);
            revealT = 0.20;
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            kick(1.1);
            fx.flash(Theme.RED);
            if (idx >= 0) {
                optBtn[idx].flash(Theme.RED);
                fx.burst(optCx[idx], optCy[idx], Theme.RED, 22);
            }
            if (answerIdx >= 0 && answerIdx < 4) {
                optBtn[answerIdx].flash(Theme.LIME);
                fx.ring(optCx[answerIdx], optCy[answerIdx], Theme.LIME, 80, 3, 0.6);
            }
            app.bank.record(target, false);
            revealT = 0.45;
        }
    }

    @Override
    protected void round(double dt) {
        if (target == null) return;
        if (revealT > 0) {
            revealT -= dt;
            if (revealT <= 0) {
                if (lives <= 0) finish();
                else nextQuestion();
            }
            return;
        }
        left -= dt;
        if (left <= 0) {
            left = 0;
            answer(-1);
        }
    }

    @Override
    protected void paintGame(Graphics2D g, int w, int h) {
        if (over) {
            for (int i = 0; i < 4; i++) optBtn[i].visible = false;
            return;
        }
        if (target == null) return;

        double cardW = Math.min(900, w * 0.80);
        double cardX = (w - cardW) / 2.0;
        double cardY = h * 0.175;
        double cardH = Math.max(130, h * 0.265);

        String label = askPhrase ? "WHAT DOES THIS MEAN?" : "WHICH AYAH IS THIS?";
        Theme.center(g, label, Theme.bold(13), Theme.a(Theme.DIM, 230), w / 2.0, cardY - 14);
        Theme.center(g, Emoji.forAyah(target.english) + "  " + target.fullRef(), Theme.bold(11), Theme.a(Theme.GOLD, 200), w / 2.0, cardY - 2);

        UI.panel(g, cardX, cardY, cardW, cardH, 24, accent);

        double frac = limit > 0 ? Theme.clamp01(left / limit) : 0;
        Color barCol = frac < 0.3 ? Theme.RED : (frac < 0.6 ? Theme.GOLD : accent);
        Theme.bar(g, cardX + 26, cardY + cardH - 20, cardW - 52, 10, frac, barCol);

        int size = q(46);
        List<String> lines = null;
        while (size > 14) {
            Font f = Theme.display(size);
            lines = Theme.wrap(g, prompt, f, (int) (cardW - 96));
            if (lines.size() <= 3 && lines.size() * (size + 8) < cardH - 66) break;
            size -= 2;
        }
        if (lines == null) lines = Theme.wrap(g, prompt, Theme.display(q(18)), (int) (cardW - 96));

        Font pf = Theme.display(size);
        double lh = size * 1.18;
        double midY = cardY + (cardH - 24) / 2.0;
        double ty = midY - (lines.size() - 1) * lh / 2.0 + size * 0.34;
        for (int i = 0; i < lines.size(); i++) {
            Theme.glowCenter(g, lines.get(i), pf, Theme.INK, w / 2.0, ty + i * lh);
        }

        double gridW = Math.min(940, w - 80);
        double gap = 16;
        double bw = (gridW - gap) / 2.0;
        double avail = h - (cardY + cardH) - 104;
        double bh = Math.max(52, Math.min(h * 0.15, (avail - gap) / 2.0));
        double gx = (w - gridW) / 2.0;
        double gy = cardY + cardH + 34;

        boolean locked = revealT > 0;
        for (int i = 0; i < 4; i++) {
            Btn b = optBtn[i];
            int col = i % 2;
            int row = i / 2;
            b.x = gx + col * (bw + gap);
            b.y = gy + row * (bh + gap);
            b.w = bw;
            b.h = bh;
            b.label = optText[i] == null ? "" : optText[i];
            b.visible = optText[i] != null;
            b.enabled = !locked;
            b.color = (i == answerIdx && locked) ? Theme.LIME : accent;
            b.fontPx = q((int) Math.max(15, Math.min(bh * 0.30, 24)));
            optCx[i] = b.x + bw / 2.0;
            optCy[i] = b.y + bh / 2.0;
        }
    }

    @Override
    protected void onFinish() {
        for (int i = 0; i < 4; i++) optBtn[i].visible = false;
    }

    @Override
    protected String footerHint() {
        return "1-4 or click to answer   .   speed feeds the multiplier   .   tap again to skip the reveal";
    }

    @Override
    protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k >= KeyEvent.VK_NUMPAD1 && k <= KeyEvent.VK_NUMPAD4) {
            choose(k - KeyEvent.VK_NUMPAD1);
        }
    }

    @Override
    protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        if (shaky == 0) return "Everything is solid - push for a longer combo.";
        return shaky + " ayah" + (shaky == 1 ? "" : "s") + " still shaky. Try FLASH.";
    }
}
