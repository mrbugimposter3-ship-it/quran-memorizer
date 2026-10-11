import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * WORD WALK - step through the entire study set (e.g. a surah) one Arabic
 * word at a time. See each word's transliteration and a rough gloss, plus the
 * words immediately before and after, with a short auto-advance timer, pop
 * animations and tick sounds so you can absorb a surah fast.
 */
public class WordGame extends GameScreen {

    private static class Step {
        String word;      // arabic word
        String translit;  // approx-aligned transliteration
        String gloss;     // approx english gloss
        String ayahEn;    // full english of the containing ayah
        String ref;       // "Surah X : Y"
    }

    private final List<Step> steps = new ArrayList<Step>();

    private int idx = 0;
    private boolean showMeaning = true;
    private boolean autoPlay = false;
    private double wordLeft = 3.0;
    private double wordLimit = 3.0;   // seconds per word
    private double wordPop = 0.0;

    private final Btn btnPrev;
    private final Btn btnNext;
    private final Btn btnMeaning;
    private final Btn btnAuto;
    private final Btn btnSlower;
    private final Btn btnFaster;

    public WordGame(MemoriBlitz app) {
        super(app);
        key = "word";
        title = "WORD WALK";
        blurb = "Walk a surah word by word with meaning and context";
        accent = Theme.GOLD;

        btnPrev = new Btn(0, 0, 120, 50, "< PREV", Theme.CYAN, 1, new Runnable() {
            public void run() { advance(-1); }
        });
        btnPrev.key = KeyEvent.VK_LEFT;
        btns.add(btnPrev);

        btnNext = new Btn(0, 0, 140, 54, "NEXT >", Theme.GOLD, 0, new Runnable() {
            public void run() { advance(1); }
        });
        btnNext.key = KeyEvent.VK_RIGHT;
        btns.add(btnNext);

        btnMeaning = new Btn(0, 0, 150, 50, "MEANING ON", Theme.VIOLET, 1, new Runnable() {
            public void run() { showMeaning = !showMeaning; }
        });
        btnMeaning.key = KeyEvent.VK_M;
        btns.add(btnMeaning);

        btnAuto = new Btn(0, 0, 130, 50, "AUTO OFF", Theme.LIME, 1, new Runnable() {
            public void run() {
                autoPlay = !autoPlay;
                wordLeft = wordLimit;
            }
        });
        btnAuto.key = KeyEvent.VK_A;
        btns.add(btnAuto);

        btnSlower = new Btn(0, 0, 56, 50, "-", Theme.VIOLET, 1, new Runnable() {
            public void run() { setSpeed(-1); }
        });
        btnSlower.key = KeyEvent.VK_COMMA;
        btns.add(btnSlower);

        btnFaster = new Btn(0, 0, 56, 50, "+", Theme.VIOLET, 1, new Runnable() {
            public void run() { setSpeed(1); }
        });
        btnFaster.key = KeyEvent.VK_PERIOD;
        btns.add(btnFaster);
    }

    private void setSpeed(int d) {
        wordLimit = Math.max(1, Math.min(10, wordLimit + d));
        wordLeft = wordLimit;
        Sound.tick();
    }

    @Override
    protected int startLives() { return 1; }

    @Override
    protected double startTime() { return -1; }   // untimed, self-paced

    @Override
    protected void resetGame() {
        steps.clear();
        for (QuranData.Ayah a : app.bank.study) {
            String[] ar = a.arabic.trim().split("\\s+");
            String[] tr = a.translit.trim().split("\\s+");
            String[] en = a.english.replaceAll("[^\\p{L}\\p{N}'\\s-]", " ").trim().split("\\s+");
            int n = ar.length;
            for (int i = 0; i < n; i++) {
                Step s = new Step();
                s.word = ar[i];
                s.translit = map(tr, i, n);
                s.gloss = map(en, i, n);
                s.ayahEn = a.english;
                s.ref = "Surah " + a.surah + " : " + a.ayah;
                steps.add(s);
            }
        }
        idx = 0;
        wordPop = 0;
        wordLeft = wordLimit;
        score = 0;
    }

    /** Proportional word mapping (robust to Arabic vs translit/English drift). */
    private static String map(String[] words, int i, int n) {
        if (words == null || words.length == 0) return "";
        int j = (int) Math.round(i * (words.length - 1) / (double) Math.max(1, n - 1));
        if (j < 0) j = 0;
        if (j >= words.length) j = words.length - 1;
        return words[j];
    }

    private void advance(int d) {
        if (steps.isEmpty()) return;
        int ni = idx + d;
        if (ni < 0) { ni = 0; Sound.wrong(); return; }
        if (ni >= steps.size()) { finish(); return; }
        idx = ni;
        wordLeft = wordLimit;
        wordPop = 0.22;
        score++;
        combo++;
        if (combo > bestCombo) bestCombo = combo;
        Sound.tick();
        fx.burst(getWidth() / 2.0, getHeight() * 0.34, Theme.GOLD, 12);
        fx.pop(getWidth() / 2.0, getHeight() * 0.34 - 60, steps.get(idx).translit, Theme.CYAN, Theme.body(14));
        btnNext.flash(Theme.GOLD);
    }

    @Override
    protected void round(double dt) {
        if (wordPop > 0) wordPop = Math.max(0, wordPop - dt);
        if (autoPlay && !over && idx < steps.size() - 1) {
            wordLeft -= dt;
            if (wordLeft <= 0) advance(1);
        }
    }

    @Override
    protected void hud(Graphics2D g, int w, int h) {
        double frac = steps.isEmpty() ? 0 : idx / (double) steps.size();
        String mode = autoPlay ? ("AUTO " + (int) Math.ceil(wordLeft) + "s  ·  " + (int) wordLimit + "s/word")
                               : ("tap NEXT or SPACE  ·  " + (int) wordLimit + "s/word");
        Theme.center(g, "WORD " + (idx + 1) + " / " + steps.size() + "   ·   " + mode,
                Theme.bold(12), Theme.a(Theme.GOLD, 230), w / 2.0, 28);
        Theme.bar(g, (w - 420) / 2.0, 38, 420, 7, frac, Theme.GOLD);
    }

    @Override
    protected void paintGame(Graphics2D g, int w, int h) {
        if (steps.isEmpty()) {
            Theme.center(g, "No ayahs selected", Theme.bold(18), Theme.DIM, w / 2.0, h / 2.0);
            return;
        }
        Step s = steps.get(idx);
        double cx = w / 2.0;

        // wordPop scale
        double pop = wordPop > 0 ? (wordPop / 0.22) : 0;
        double scale = 1.0 + 0.22 * pop;

        // reference
        Theme.center(g, s.ref, Theme.bold(11), Theme.a(Theme.CYAN, 190), cx, h * 0.14);

        // current word (big, pop-animated)
        int ws = q(54);
        Font wf = Theme.arabic(ws);
        // shrink if the word is very long
        while (ws > 20 && g.getFontMetrics(wf).stringWidth(s.word) > w - 120) {
            ws -= 4;
            wf = Theme.arabic(ws);
        }
        double wordY = h * 0.34;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(cx, wordY);
        g2.scale(scale, scale);
        g2.translate(-cx, -wordY);
        Theme.glowCenter(g2, s.word, wf, Theme.GOLD, cx, wordY);
        g2.dispose();

        // context: words before ... words after
        String prev1 = idx >= 1 ? steps.get(idx - 1).word : "";
        String prev2 = idx >= 2 ? steps.get(idx - 2).word : "";
        String next1 = idx + 1 < steps.size() ? steps.get(idx + 1).word : "";
        String next2 = idx + 2 < steps.size() ? steps.get(idx + 2).word : "";
        StringBuilder ctx = new StringBuilder();
        if (!prev2.isEmpty()) ctx.append(prev2).append(" ");
        if (!prev1.isEmpty()) ctx.append(prev1).append(" ");
        ctx.append("  ··  ");
        if (!next1.isEmpty()) ctx.append(next1).append(" ");
        if (!next2.isEmpty()) ctx.append(next2).append(" ");
        if (ctx.length() > 6) {
            Theme.center(g, ctx.toString().trim(), Theme.arabic(q(16)), Theme.a(Theme.DIM, 170), cx, h * 0.42);
        }

        // transliteration of the current word
        Theme.glowCenter(g, s.translit, Theme.display(q(24)), Theme.INK, cx, h * 0.49);

        // gloss + full-ayah meaning
        if (showMeaning) {
            if (!s.gloss.isEmpty()) {
                Theme.center(g, "\u2248 " + s.gloss, Theme.bold(q(15)), Theme.PINK, cx, h * 0.55);
            }
            List<String> el = Theme.wrap(g, s.ayahEn, Theme.body(14), (int) Math.min(760, w - 120));
            double ly = h * 0.60;
            for (String ln : el) {
                if (ly > h - 120) break;
                Theme.center(g, ln, Theme.body(14), Theme.a(Theme.DIM, 185), cx, ly);
                ly += 20;
            }
        }

        // bottom button row
        double y = h - 64;
        double total = 120 + 140 + 56 + 56 + 124 + 130 + 5 * 12;
        double bx = (w - total) / 2.0;
        btnPrev.x = bx; btnPrev.y = y; btnPrev.w = 120; btnPrev.h = 50;
        btnMeaning.x = bx + 120 + 12; btnMeaning.y = y; btnMeaning.w = 140; btnMeaning.h = 50;
        btnMeaning.label = showMeaning ? "MEANING ON" : "MEANING OFF";
        btnSlower.x = btnMeaning.x + 140 + 12; btnSlower.y = y;
        btnFaster.x = btnSlower.x + 56 + 12; btnFaster.y = y;
        btnAuto.x = btnFaster.x + 56 + 12; btnAuto.y = y; btnAuto.w = 124; btnAuto.h = 50;
        btnAuto.label = autoPlay ? "AUTO ON" : "AUTO OFF";
        btnAuto.color = autoPlay ? Theme.LIME : Theme.DIM;
        btnNext.x = btnAuto.x + 124 + 12; btnNext.y = y; btnNext.w = 130; btnNext.h = 54;

        btnPrev.enabled = idx > 0;
        btnNext.visible = true;
    }

    @Override
    protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_SPACE || k == KeyEvent.VK_NUMPAD6) { advance(1); }
        else if (k == KeyEvent.VK_NUMPAD4) { advance(-1); }
        else if (k == KeyEvent.VK_COMMA) { setSpeed(-1); }
        else if (k == KeyEvent.VK_PERIOD) { setSpeed(1); }
    }

    @Override
    protected String footerHint() {
        return "SPACE / \u2192 next   .   \u2190 back   .   M meaning   .   A auto   .   -/+ speed";
    }
}
