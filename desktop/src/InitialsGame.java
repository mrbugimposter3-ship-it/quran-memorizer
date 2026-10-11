import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Random;

/** INITIALS: recall the full ayah from the first letter of each word. */
public class InitialsGame extends GameScreen {

    private final Random rnd = new Random();
    private QuranData.Ayah target;
    private final StringBuilder typed = new StringBuilder();
    private String feedback = "";
    private Color feedbackColor = Theme.DIM;
    private double feedbackT;
    private final Btn btnReveal;

    public InitialsGame(MemoriBlitz app) {
        super(app);
        key = "initials";
        title = "INITIALS";
        blurb = "Recall the ayah from first letters.";
        accent = Theme.LIME;
        btnReveal = new Btn(0, 0, 200, 46, "REVEAL", Theme.DIM, 1, new Runnable() {
            public void run() { reveal(); }
        });
        btnReveal.visible = false;
        btns.add(btnReveal);
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    @Override protected void resetGame() {
        target = null;
        typed.setLength(0);
        feedback = "";
        feedbackT = 0;
        nextCard();
    }

    private void nextCard() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        typed.setLength(0);
    }

    private String initials(String s) {
        StringBuilder sb = new StringBuilder();
        for (String w : s.split("\\s+")) {
            if (w.length() > 0) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(w.charAt(0));
            }
        }
        return sb.toString();
    }

    private String norm(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9 ]", "").trim();
    }

    private void showFeedback(String msg, Color c) {
        feedback = msg;
        feedbackColor = c;
        feedbackT = 2.0;
    }

    private void submit() {
        if (target == null || over || countdown > 0) return;
        String got = norm(typed.toString());
        if (got.isEmpty()) return;
        if (got.equals(norm(target.translit))) {
            award(100, 1.0);
            Sound.good();
            app.bank.record(target, true);
            showFeedback("CORRECT", Theme.LIME);
            nextCard();
        } else {
            lives--;
            penalize(-45);
            Sound.wrong();
            app.bank.record(target, false);
            showFeedback("Correct: " + target.translit, Theme.RED);
            if (lives <= 0) { finish(); return; }
            nextCard();
        }
    }

    private void reveal() {
        if (target == null || over || countdown > 0) return;
        lives--;
        app.bank.record(target, false);
        showFeedback(target.arabic + "  " + target.translit, Theme.DIM);
        if (lives <= 0) { finish(); return; }
        nextCard();
    }

    @Override protected void round(double dt) {
        if (feedbackT > 0) {
            feedbackT -= dt;
            if (feedbackT <= 0) feedback = "";
        }
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { btnReveal.visible = false; return; }
        if (target == null) return;
        btnReveal.visible = true;
        btnReveal.x = w / 2.0 - 100;
        btnReveal.y = h - 88;
        btnReveal.w = 200;
        btnReveal.h = 46;

        Theme.glowCenter(g, initials(target.translit), Theme.mono(q(40)), Theme.LIME, w / 2.0, h * 0.20);

        double boxW = Math.min(760, w * 0.7);
        double boxX = (w - boxW) / 2.0;
        double boxY = h * 0.40;
        double boxH = 64;
        Shape box = Theme.rr(boxX, boxY, boxW, boxH, 16);
        g.setColor(Theme.a(Theme.CARD, 220));
        g.fill(box);
        g.setColor(Theme.a(accent, 90));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(box);
        String shown = typed.length() == 0 ? "_" : typed.toString();
        Theme.center(g, shown, Theme.mono(24), Theme.INK, w / 2.0, boxY + boxH / 2.0);

        if (!feedback.isEmpty()) {
            Theme.center(g, feedback, Theme.body(15), feedbackColor, w / 2.0, h * 0.56);
        }
        Theme.center(g, "type the full ayah, then press ENTER", Theme.body(12), Theme.DIM, w / 2.0, h * 0.62);
    }

    @Override protected String footerHint() {
        return "type the transliteration   .   ENTER to check   .   click REVEAL to give up";
    }

    @Override protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_BACK_SPACE) {
            if (typed.length() > 0) typed.setLength(typed.length() - 1);
        } else if (k == KeyEvent.VK_ENTER) {
            submit();
        } else {
            char c = e.getKeyChar();
            if (c >= 32 && c < 127) typed.append(c);
        }
    }

    @Override protected String adviceLine() {
        int shaky = app.bank.shakyCount();
        return shaky == 0 ? "Recall is strong." : shaky + " ayah(s) still shaky.";
    }
}
