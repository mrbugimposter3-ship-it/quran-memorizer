import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * FILL - an ayah's transliteration appears with one word blanked out. Pick the
 * missing word from four options. Tests word-level recall inside the ayah.
 */
public class FillGame extends GameScreen {

    private final Random rnd = new Random();
    private final Btn[] optBtn = new Btn[4];
    private final String[] optText = new String[4];
    private final double[] optCx = new double[4];
    private final double[] optCy = new double[4];

    private QuranData.Ayah target;
    private String[] words;
    private int blankIdx;
    private String correctWord;
    private String displayText = "";
    private int answerIdx = -1;
    private double limit = 8, left = 8;
    private double revealT;
    private int lastGain;

    public FillGame(MemoriBlitz app) {
        super(app);
        key = "fill";
        title = "FILL";
        blurb = "Supply the missing word of the ayah.";
        accent = Theme.ORANGE;

        String[] hot = { "1", "2", "3", "4" };
        int[] vk = { KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4 };
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            optBtn[i] = new Btn(0, 0, 10, 10, "", Theme.ORANGE, 2, new Runnable() {
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
        // find an ayah with at least 3 words
        for (int attempt = 0; attempt < 40; attempt++) {
            QuranData.Ayah a = app.bank.randomAyah(rnd);
            if (a == null) break;
            String[] w = a.translit.trim().split("\\s+");
            if (w.length >= 3) {
                target = a;
                words = w;
                blankIdx = rnd.nextInt(w.length);
                correctWord = w[blankIdx];
                break;
            }
        }
        if (target == null) { finish(); return; }

        // build display text with blank
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            if (i == blankIdx) sb.append("______");
            else sb.append(words[i]);
        }
        displayText = sb.toString();

        // options: correct word + 3 distractors (words from other ayahs)
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : app.bank.study) {
            if (a == target) continue;
            for (String w : a.translit.trim().split("\\s+")) {
                if (!pool.contains(w) && !w.equalsIgnoreCase(correctWord)) pool.add(w);
            }
        }
        Collections.shuffle(pool, rnd);
        List<String> wrong = new ArrayList<String>();
        for (int i = 0; i < pool.size() && wrong.size() < 3; i++) wrong.add(pool.get(i));

        List<String> opts = new ArrayList<String>();
        opts.add(correctWord);
        opts.addAll(wrong);
        Collections.shuffle(opts, rnd);

        for (int i = 0; i < 4; i++) optText[i] = i < opts.size() ? opts.get(i) : null;
        answerIdx = opts.indexOf(correctWord);
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
            fx.burst(optCx[idx], optCy[idx], Theme.LIME, 30);
            fx.pop(optCx[idx], optCy[idx] - 24, "+" + lastGain, Theme.LIME, Theme.display(28));
            optBtn[idx].flash(Theme.LIME);
            app.bank.record(target, true);
            revealT = 0.34;
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            kick(1.0);
            fx.flash(Theme.RED);
            if (idx >= 0) optBtn[idx].flash(Theme.RED);
            if (answerIdx >= 0 && answerIdx < 4) optBtn[answerIdx].flash(Theme.LIME);
            app.bank.record(target, false);
            revealT = 0.9;
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
        if (left <= 0) { left = 0; answer(-1); }
    }

    @Override
    protected void paintGame(Graphics2D g, int w, int h) {
        if (over) {
            for (int i = 0; i < 4; i++) optBtn[i].visible = false;
            return;
        }
        if (target == null) return;

        Theme.center(g, "WHICH WORD IS MISSING?", Theme.bold(13), Theme.a(Theme.DIM, 230), w / 2.0, 76);
        Theme.center(g, target.fullRef(), Theme.bold(11), Theme.a(Theme.GOLD, 190), w / 2.0, 94);

        double cardW = Math.min(900, w * 0.80);
        double cardX = (w - cardW) / 2.0;
        double cardY = h * 0.17;
        double cardH = Math.max(130, h * 0.27);
        UI.panel(g, cardX, cardY, cardW, cardH, 24, accent);

        double frac = limit > 0 ? Theme.clamp01(left / limit) : 0;
        Color barCol = frac < 0.3 ? Theme.RED : (frac < 0.6 ? Theme.GOLD : accent);
        Theme.bar(g, cardX + 26, cardY + cardH - 20, cardW - 52, 10, frac, barCol);

        int size = q(40);
        List<String> lines = null;
        while (size > 14) {
            Font f = Theme.display(size);
            lines = Theme.wrap(g, displayText, f, (int) (cardW - 96));
            if (lines.size() <= 3 && lines.size() * (size + 8) < cardH - 66) break;
            size -= 2;
        }
        if (lines == null) lines = Theme.wrap(g, displayText, Theme.display(q(16)), (int) (cardW - 96));

        Font pf = Theme.display(size);
        double lh = size * 1.18;
        double midY = cardY + (cardH - 24) / 2.0;
        double ty = midY - (lines.size() - 1) * lh / 2.0 + size * 0.34;
        for (int i = 0; i < lines.size(); i++) {
            String ln = lines.get(i);
            if (ln.contains("______")) {
                // draw the blank segment highlighted
                int bi = ln.indexOf("______");
                String pre = ln.substring(0, bi);
                String post = ln.substring(bi + 6);
                FontMetrics fm = g.getFontMetrics(pf);
                float cx = (float) (w / 2.0 - fm.stringWidth(ln) / 2.0);
                float yy = (float) (ty + i * lh);
                g.setFont(pf);
                g.setColor(Theme.INK);
                g.drawString(pre, cx, yy);
                float bx = cx + fm.stringWidth(pre);
                g.setColor(Theme.GOLD);
                g.drawString("______", bx, yy);
                g.setColor(Theme.INK);
                g.drawString(post, bx + fm.stringWidth("______"), yy);
            } else {
                Theme.glowCenter(g, ln, pf, Theme.INK, w / 2.0, ty + i * lh);
            }
        }

        double gridW = Math.min(940, w - 80);
        double gap = 16;
        double bw = (gridW - gap) / 2.0;
        double avail = h - (cardY + cardH) - 104;
        double bh = Math.max(52, Math.min(h * 0.14, (avail - gap) / 2.0));
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
        return "1-4 or click to answer   .   speed feeds the multiplier";
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
        return "Filling the gap forces you to recall the exact word - the heart of memorising.";
    }
}
