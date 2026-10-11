import java.awt.*;
import java.util.List;
import java.util.Random;

/** SPEED: rapid-fire true/false on translation accuracy, against the clock. */
public class SpeedGame extends GameScreen {

    private final Random rnd = new Random();
    private QuranData.Ayah target;
    private boolean shownCorrect;
    private String shownTrans = "";
    private final Btn btnYes, btnNo;

    public SpeedGame(MemoriBlitz app) {
        super(app);
        key = "speed";
        title = "SPEED";
        blurb = "Rapid true/false on translation accuracy.";
        accent = Theme.RED;
        btnYes = new Btn(0, 0, 10, 10, "CORRECT", Theme.LIME, 0, new Runnable() {
            public void run() { judge(true); }
        });
        btnNo = new Btn(0, 0, 10, 10, "WRONG", Theme.RED, 0, new Runnable() {
            public void run() { judge(false); }
        });
        btnYes.visible = false;
        btnNo.visible = false;
        btns.add(btnYes);
        btns.add(btnNo);
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return 60; }

    @Override protected void resetGame() {
        target = null;
        nextQuestion();
    }

    private void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        shownCorrect = rnd.nextBoolean();
        if (shownCorrect) {
            shownTrans = target.english;
        } else {
            QuranData.Ayah a = app.bank.randomAyah(rnd);
            shownTrans = a == null ? target.english : a.english;
        }
    }

    private void judge(boolean saidCorrect) {
        if (over || countdown > 0) return;
        if (saidCorrect == shownCorrect) {
            award(100, 1.0);
            Sound.good();
            app.bank.record(target, true);
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            app.bank.record(target, false);
            if (lives <= 0) { finish(); return; }
        }
        nextQuestion();
    }

    @Override protected void round(double dt) {}

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { btnYes.visible = false; btnNo.visible = false; return; }
        if (target == null) return;

        Font af = Theme.arabic(q(28));
        List<String> alines = Theme.wrap(g, target.arabic, af, (int) (w * 0.8));
        if (alines == null) alines = Theme.wrap(g, target.arabic, Theme.arabic(q(22)), (int) (w * 0.8));
        double lh = 38;
        double ty = h * 0.20 - (alines.size() - 1) * lh / 2.0;
        for (int i = 0; i < alines.size(); i++) {
            Theme.glowCenter(g, alines.get(i), af, Theme.INK, w / 2.0, ty + i * lh);
        }

        Font tf = Theme.body(q(16));
        List<String> tlines = Theme.wrap(g, shownTrans, tf, (int) (w * 0.8));
        if (tlines == null) tlines = Theme.wrap(g, shownTrans, Theme.body(q(14)), (int) (w * 0.8));
        double tlh = 24;
        double tty = h * 0.40 - (tlines.size() - 1) * tlh / 2.0;
        for (int i = 0; i < tlines.size(); i++) {
            Theme.center(g, tlines.get(i), tf, Theme.DIM, w / 2.0, tty + i * tlh);
        }

        double bw = 220;
        double bh = 60;
        double gy = h * 0.56;
        double gap = 40;
        btnYes.x = w / 2.0 - bw - gap / 2.0;
        btnYes.y = gy;
        btnYes.w = bw;
        btnYes.h = bh;
        btnYes.visible = true;
        btnNo.x = w / 2.0 + gap / 2.0;
        btnNo.y = gy;
        btnNo.w = bw;
        btnNo.h = bh;
        btnNo.visible = true;
    }

    @Override protected void onFinish() {
        btnYes.visible = false;
        btnNo.visible = false;
    }

    @Override protected String footerHint() {
        return "is the translation correct?   .   click CORRECT or WRONG";
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Fast and accurate." : shaky + " ayah(s) still shaky.";
    }
}
