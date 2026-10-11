import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;

/**
 * Shared behaviour for every game: score, combo multiplier, level, lives or
 * countdown clock, the 3-2-1 count-in, toasts and the round-over card.
 */
public abstract class GameScreen extends Screen {

    public String key = "";
    public String title = "GAME";
    public String blurb = "";
    public Color accent = Theme.CYAN;

    public int score, combo, bestCombo, level, lives, correct, wrong;
    public double timeLeft = -1;            // negative = this game is untimed
    public boolean over, newRecord;
    public double overT;

    protected double countdown;
    protected double goT;

    protected String toast;
    protected double toastT;
    protected Color toastColor = Theme.LIME;

    protected final Btn btnMenu;
    protected final Btn btnAgain;
    protected final Btn btnQuit;
    protected final Btn btnSound;
    protected final Btn btnQuran;

    private static final double CARD_W = 600;
    private static final double CARD_H = 410;

    public GameScreen(MemoriBlitz app) {
        super(app);

        btnQuit = new Btn(18, 16, 104, 38, "< MENU", Theme.DIM, 1, new Runnable() {
            public void run() { GameScreen.this.app.goMenu(); }
        });

        btnSound = new Btn(0, 16, 124, 38, "SOUND ON", Theme.VIOLET, 1, new Runnable() {
            public void run() {
                Sound.toggle();
                GameScreen.this.app.bank.soundOn = Sound.isOn();
                GameScreen.this.app.bank.markDirty();
            }
        });

        btnAgain = new Btn(0, 0, 226, 60, "PLAY AGAIN", Theme.LIME, 0, new Runnable() {
            public void run() { GameScreen.this.begin(); }
        });
        btnAgain.key = KeyEvent.VK_R;

        btnMenu = new Btn(0, 0, 170, 60, "MENU", Theme.DIM, 1, new Runnable() {
            public void run() { GameScreen.this.app.goMenu(); }
        });

        btnQuran = new Btn(0, 16, 104, 38, "QURAN", Theme.GOLD, 1, new Runnable() {
            public void run() { GameScreen.this.app.openReaderFrom(key); }
        });
        btnQuran.key = KeyEvent.VK_Q;

        btnAgain.visible = false;
        btnMenu.visible = false;

        btns.add(btnQuit);
        btns.add(btnSound);
        btns.add(btnQuran);
        btns.add(btnAgain);
        btns.add(btnMenu);
    }

    /* ------------------------------------------------------------------ */
    /* lifecycle                                                            */
    /* ------------------------------------------------------------------ */

    public void begin() {
        score = 0;
        combo = 0;
        bestCombo = 0;
        level = 1;
        lives = startLives();
        correct = 0;
        wrong = 0;
        over = false;
        overT = 0;
        newRecord = false;
        toast = null;
        toastT = 0;
        countdown = 3.0;
        goT = 0;
        shake = 0;
        timeLeft = startTime();
        fx.clear();
        btnAgain.visible = false;
        btnMenu.visible = false;
        resetGame();
    }

    protected int startLives() { return 3; }
    protected double startTime() { return -1; }

    /** Subclass: (re)initialise per-round state. */
    protected abstract void resetGame();

    /** Subclass: per-frame gameplay. */
    protected abstract void round(double dt);

    /** Subclass: draw the playfield; HUD and overlays are handled here. */
    protected abstract void paintGame(Graphics2D g, int w, int h);

    public void finish() {
        if (over) return;
        over = true;
        overT = 0;
        newRecord = app.bank.submitScore(key, score);
        app.bank.save();
        onFinish();
        if (newRecord) {
            fx.celebrate(Math.max(getWidth(), 800), Math.max(getHeight(), 600));
            Sound.fanfare();
        } else {
            Sound.gameOver();
        }
    }

    /** Subclass: tidy up game-specific controls once the round has ended. */
    protected void onFinish() {}

    /** Subclass: optional one-line hint pinned to the bottom of the screen. */
    protected String footerHint() { return null; }

    /* ------------------------------------------------------------------ */
    /* scoring                                                              */
    /* ------------------------------------------------------------------ */

    /** Scale a question font size by the user's global question-scale setting. */
    protected int q(int base) {
        return (int) Math.round(base * app.bank.questionScale);
    }

    /** Combo multiplier, 1x .. 8x. */
    public int mult() {
        int m = 1 + combo / 4;
        return m > 8 ? 8 : m;
    }

    protected int award(int base, double factor) {
        int g = (int) Math.round(base * factor * mult());
        if (g < 1) g = 1;
        score += g;
        combo++;
        if (combo > bestCombo) bestCombo = combo;
        correct++;
        checkLevel();
        return g;
    }

    protected void penalize(int delta) {
        combo = 0;
        score = Math.max(0, score + delta);
        wrong++;
    }

    protected void checkLevel() {
        int want = 1 + correct / 5;
        if (want > level) {
            level = want;
            Sound.levelUp();
            toast("LEVEL " + level + "!", Theme.GOLD);
            fx.flash(accent);
        }
    }

    protected void toast(String msg, Color c) {
        toast = msg;
        toastColor = c;
        toastT = 1.1;
    }

    /* ------------------------------------------------------------------ */
    /* clock                                                                */
    /* ------------------------------------------------------------------ */

    protected void tick(double dt) {
        if (toastT > 0) toastT = Math.max(0, toastT - dt);

        if (over) {
            overT += dt;
            return;
        }

        if (countdown > 0) {
            double before = countdown;
            countdown -= dt;
            if ((int) before != (int) Math.max(0, countdown)) Sound.tick();
            if (countdown <= 0) {
                countdown = 0;
                goT = 0.55;
                Sound.pop();
            }
            return;
        }

        if (goT > 0) goT = Math.max(0, goT - dt);

        round(dt);

        if (timeLeft >= 0 && !over) {
            timeLeft -= dt;
            if (timeLeft <= 0) {
                timeLeft = 0;
                finish();
            }
        }
    }

    /* ------------------------------------------------------------------ */
    /* rendering                                                            */
    /* ------------------------------------------------------------------ */

    public void render(Graphics2D g, int w, int h) {
        btnQuit.visible = !over;
        btnSound.visible = !over;
        btnSound.label = Sound.isOn() ? "SOUND ON" : "SOUND OFF";
        btnSound.color = Sound.isOn() ? Theme.VIOLET : Theme.DIM;
        btnSound.x = w - 140;
        btnSound.y = 16;

        btnQuran.visible = !over;
        btnQuran.x = 130;
        btnQuran.y = 16;

        paintGame(g, w, h);

        if (over) {
            layoutOverButtons(w, h);
            overOverlay(g, w, h);
        } else {
            hud(g, w, h);
            if (goT > 0) goOverlay(g, w, h);
            else if (countdown > 0) countOverlay(g, w, h);
            if (toastT > 0 && toast != null) drawToast(g, w, h);
            String hint = footerHint();
            if (hint != null) {
                Theme.center(g, hint, Theme.bold(12), Theme.a(Theme.DIM, 150), w / 2.0, h - 20);
            }
        }
    }

    private void layoutOverButtons(int w, int h) {
        double cx = w / 2.0;
        double cy = h / 2.0;
        double y = cy + CARD_H / 2.0 - 80;
        btnAgain.x = cx - 226 - 10;
        btnAgain.y = y;
        btnMenu.x = cx + 10;
        btnMenu.y = y;
        boolean show = overT > 0.30;
        btnAgain.visible = show;
        btnMenu.visible = show;
    }

    protected void hud(Graphics2D g, int w, int h) {
        double cw = 128, ch = 44, gap = 10;
        String[] caps = { "SCORE", "COMBO", "LEVEL", timeLeft >= 0 ? "TIME" : "LIVES" };
        String[] vals = {
                String.valueOf(score),
                "x" + mult(),
                String.valueOf(level),
                timeLeft >= 0 ? String.valueOf((int) Math.ceil(timeLeft)) : String.valueOf(lives)
        };
        Color[] cols = {
                Theme.GOLD, Theme.PINK, Theme.CYAN,
                timeLeft >= 0 ? (timeLeft < 8 ? Theme.RED : Theme.LIME)
                              : (lives <= 1 ? Theme.RED : Theme.GOLD)
        };

        double total = caps.length * cw + (caps.length - 1) * gap;
        double startX = Math.max(150, (w - total) / 2.0);
        double y = 16;

        for (int i = 0; i < caps.length; i++) {
            UI.chip(g, startX + i * (cw + gap), y, cw, ch, caps[i], vals[i], cols[i]);
        }

        double barW = 240;
        double bx = (w - barW) / 2.0;
        double by = y + ch + 8;
        Theme.bar(g, bx, by, barW, 7, (combo % 4) / 4.0, Theme.PINK);
        Theme.center(g, "next x" + Math.min(8, mult() + 1),
                Theme.bold(10), Theme.a(Theme.DIM, 170), w / 2.0, by + 22);
    }

    private void countOverlay(Graphics2D g, int w, int h) {
        g.setColor(new Color(3, 5, 16, 165));
        g.fillRect(0, 0, w, h);
        int n = (int) Math.ceil(countdown);
        if (n < 1) n = 1;
        double frac = countdown - Math.floor(countdown);
        double pop = 1.0 - Theme.clamp01(frac);
        String s = String.valueOf(n);
        Font f = Theme.display((int) (110 + 50 * pop));
        Color c = Theme.RAINBOW[(3 - Math.min(3, n)) % Theme.RAINBOW.length];
        Theme.glowCenter(g, s, f, c, w / 2.0, h / 2.0 - 20);
        Theme.center(g, "GET READY", Theme.bold(20), Theme.DIM, w / 2.0, h / 2.0 + 90);
    }

    private void goOverlay(Graphics2D g, int w, int h) {
        double t = 1.0 - Theme.clamp01(goT / 0.55);
        Font f = Theme.display((int) (90 + 40 * (1 - t)));
        Theme.glowCenter(g, "GO!", f, Theme.LIME, w / 2.0, h / 2.0 - 20);
    }

    private void drawToast(Graphics2D g, int w, int h) {
        double a = Theme.clamp01(toastT / 1.1);
        double scale = 1.0 + (1 - a) * 0.15;
        Font f = Theme.display(30);
        g.setFont(f);
        int tw = g.getFontMetrics().stringWidth(toast);
        double pw = tw + 60;
        double ph = 56;
        double x = (w - pw) / 2.0;
        double y = h * 0.19;

        AffineTransform at = g.getTransform();
        g.translate(w / 2.0, y + ph / 2);
        g.scale(scale, scale);
        g.translate(-w / 2.0, -(y + ph / 2));

        Shape s = Theme.rr(x, y, pw, ph, ph / 2);
        g.setColor(Theme.a(Theme.DARK, (int) (215 * a)));
        g.fill(s);
        Theme.glow(g, s, toastColor, 1.6f, 5);
        g.setColor(Theme.a(toastColor, (int) (190 * a)));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(s);
        Theme.center(g, toast, f, Theme.a(toastColor, (int) (255 * a)), w / 2.0, y + ph / 2);

        g.setTransform(at);
    }

    private void overOverlay(Graphics2D g, int w, int h) {
        double a = Theme.ease(Math.min(1, overT / 0.35));
        g.setColor(new Color(3, 5, 16, (int) (205 * a)));
        g.fillRect(0, 0, w, h);

        double cx = w / 2.0;
        double cy = h / 2.0;
        double x = cx - CARD_W / 2.0;
        double y = cy - CARD_H / 2.0;

        double scale = 0.88 + 0.12 * a;
        AffineTransform at = g.getTransform();
        g.translate(cx, cy);
        g.scale(scale, scale);
        g.translate(-cx, -cy);

        Shape card = Theme.rr(x, y, CARD_W, CARD_H, 28);
        g.setColor(new Color(14, 17, 42, 246));
        g.fill(card);
        Theme.glow(g, card, newRecord ? Theme.GOLD : accent, 2.0f, 6);
        g.setColor(Theme.a(newRecord ? Theme.GOLD : accent, 165));
        g.setStroke(new BasicStroke(2f));
        g.draw(card);

        Theme.glowCenter(g, newRecord ? "NEW RECORD!" : "ROUND OVER", Theme.display(36),
                newRecord ? Theme.GOLD : accent, cx, y + 54);

        Theme.center(g, "FINAL SCORE", Theme.bold(13), Theme.DIM, cx, y + 100);
        Theme.glowCenter(g, String.valueOf(score), Theme.mono(62), Theme.INK, cx, y + 150);

        double rowY = y + 198;
        String[][] stats = {
                { "ACCURACY", accuracy() },
                { "BEST COMBO", "x" + bestCombo },
                { "RECORD", String.valueOf(Math.max(app.bank.bestOf(key), score)) }
        };
        Color[] cols = { Theme.CYAN, Theme.PINK, Theme.GOLD };
        for (int i = 0; i < 3; i++) {
            double sx = x + 60 + i * ((CARD_W - 120) / 3.0);
            Theme.center(g, stats[i][0], Theme.bold(11), Theme.DIM, sx, rowY);
            Theme.center(g, stats[i][1], Theme.mono(24), cols[i], sx, rowY + 30);
        }

        String advice = adviceLine();
        if (advice != null) {
            Theme.center(g, advice, Theme.body(13), Theme.a(Theme.DIM, 225), cx, rowY + 72);
        }

        g.setTransform(at);
    }

    protected String accuracy() {
        int total = correct + wrong;
        if (total == 0) return "0%";
        return Math.round(100.0 * correct / total) + "%";
    }

    /** Optional one-liner shown on the round-over card. */
    protected String adviceLine() { return null; }
}
