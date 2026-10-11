import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared base for the four-option multiple-choice games: a prompt on a card
 * and a 2x2 grid of answer buttons. Subclasses supply the question via
 * {@link #nextQuestion()}.
 */
public abstract class QuizGame extends GameScreen {

    protected final Random rnd = new Random();
    protected final Btn[] optBtn = new Btn[4];
    protected final String[] optText = new String[4];
    protected final double[] optCx = new double[4];
    protected final double[] optCy = new double[4];

    protected QuranData.Ayah target;
    protected String prompt = "";
    protected int answerIdx = -1;
    protected double revealT;

    public QuizGame(MemoriBlitz app) {
        super(app);
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

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    /** Font used to draw the prompt; Arabic games override with Theme.arabic. */
    protected Font promptFont() { return Theme.display(22); }

    @Override protected void resetGame() {
        target = null;
        revealT = 0;
        nextQuestion();
    }

    protected abstract void nextQuestion();

    /** Shuffle a correct answer plus distractors into the four option slots. */
    protected void setOptions(String right, List<String> wrong) {
        List<String> pool = new ArrayList<String>();
        pool.add(right);
        pool.addAll(wrong);
        Collections.shuffle(pool, rnd);
        for (int i = 0; i < 4; i++) optText[i] = i < pool.size() ? pool.get(i) : null;
        answerIdx = pool.indexOf(right);
        if (answerIdx < 0) answerIdx = 0;
        for (int i = 0; i < 4; i++) optBtn[i].flash = 0;
    }

    /** Distinct Arabic ayahs (whole-text distractors). */
    protected List<String> distractorArabic(String answer, int n) {
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : app.bank.study) {
            String m = a.arabic;
            if (m == null || m.trim().length() == 0) continue;
            if (!pool.contains(m) && !m.equals(answer)) pool.add(m);
        }
        Collections.shuffle(pool, rnd);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    /** Distinct Arabic words (for fill-in-the-blank). */
    protected List<String> distractorArabicWords(String answer, int n) {
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : app.bank.study) {
            for (String w : a.arabic.split("\\s+")) {
                if (w.length() < 2) continue;
                if (!pool.contains(w) && !w.equals(answer)) pool.add(w);
            }
        }
        Collections.shuffle(pool, rnd);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    protected void choose(int idx) {
        if (over || countdown > 0) return;
        if (revealT > 0) { revealT = 0.0001; return; }
        if (idx < 0 || idx >= 4 || optText[idx] == null) return;
        answer(idx);
    }

    protected void answer(int idx) {
        boolean right = idx == answerIdx;
        if (right) {
            award(100, 1.0);
            Sound.good();
            if (combo > 0 && combo % 4 == 0) Sound.combo(mult());
            optBtn[idx].flash(Theme.LIME);
            app.bank.record(target, true);
            revealT = 0.20;
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            kick(1.1);
            if (idx >= 0) optBtn[idx].flash(Theme.RED);
            if (answerIdx >= 0 && answerIdx < 4) optBtn[answerIdx].flash(Theme.LIME);
            app.bank.record(target, false);
            revealT = 0.45;
        }
    }

    @Override protected void round(double dt) {
        if (target == null) return;
        if (revealT > 0) {
            revealT -= dt;
            if (revealT <= 0) {
                if (lives <= 0) finish();
                else nextQuestion();
            }
        }
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { for (int i = 0; i < 4; i++) optBtn[i].visible = false; return; }
        if (target == null) return;

        Theme.center(g, Emoji.forAyah(target.english) + "  " + target.fullRef(),
                Theme.bold(12), Theme.GOLD, w / 2.0, h * 0.15 - 10);

        double cardW = Math.min(900, w * 0.80);
        double cardX = (w - cardW) / 2.0;
        double cardY = h * 0.15;
        double cardH = Math.max(130, h * 0.28);

        Shape card = Theme.rr(cardX, cardY, cardW, cardH, 24);
        g.setColor(Theme.a(Theme.CARD, 230));
        g.fill(card);
        g.setColor(Theme.a(accent, 70));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(card);

        Font f = promptFont().deriveFont((float) (promptFont().getSize() * app.bank.questionScale));
        List<String> lines = Theme.wrap(g, prompt, f, (int) (cardW - 96));
        if (lines == null) lines = Theme.wrap(g, prompt, Theme.display(q(18)), (int) (cardW - 96));
        double lh = f.getSize() + 8;
        double ty = cardY + cardH / 2.0 - (lines.size() - 1) * lh / 2.0;
        for (int i = 0; i < lines.size(); i++) {
            Theme.glowCenter(g, lines.get(i), f, Theme.INK, w / 2.0, ty + i * lh);
        }

        double gridW = Math.min(940, w - 80);
        double gap = 16;
        double bw = (gridW - gap) / 2.0;
        double avail = h - (cardY + cardH) - 90;
        double bh = Math.max(52, Math.min(h * 0.15, (avail - gap) / 2.0));
        double gx = (w - gridW) / 2.0;
        double gy = cardY + cardH + 30;

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

    @Override protected void onFinish() {
        for (int i = 0; i < 4; i++) optBtn[i].visible = false;
    }

    @Override protected String footerHint() {
        return "1-4 or click to answer   .   tap again to skip the reveal";
    }

    @Override protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k >= KeyEvent.VK_NUMPAD1 && k <= KeyEvent.VK_NUMPAD4) choose(k - KeyEvent.VK_NUMPAD1);
    }
}
