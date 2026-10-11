import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * REVEAL - photographic-memory drill. An ayah's Arabic is masked, then words
 * are revealed one by one at random. Press SPACE as soon as you can recall the
 * whole ayah: the fewer words revealed, the more points you earn.
 */
public class RevealGame extends GameScreen {

    private final Random rnd = new Random();

    private QuranData.Ayah target;
    private String[] words;
    private boolean[] revealed;
    private int revealedCount;
    private double revealTimer;
    private double revealInterval = 1.3;

    public RevealGame(MemoriBlitz app) {
        super(app);
        key = "reveal";
        title = "REVEAL";
        blurb = "An ayah is revealed word by word - recall it before it's fully shown.";
        accent = Theme.VIOLET;
    }

    @Override protected int startLives() { return 3; }
    @Override protected double startTime() { return -1; }

    @Override protected void resetGame() {
        target = null;
        nextCard();
    }

    private void nextCard() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        words = target.arabic.split("\\s+");
        revealed = new boolean[words.length];
        revealedCount = 0;
        revealTimer = 0;
        if (words.length > 0) revealRandom();
    }

    private void revealRandom() {
        List<Integer> hidden = new ArrayList<Integer>();
        for (int i = 0; i < words.length; i++) if (!revealed[i]) hidden.add(i);
        if (hidden.isEmpty()) return;
        int i = hidden.get(rnd.nextInt(hidden.size()));
        revealed[i] = true;
        revealedCount++;
    }

    @Override protected void round(double dt) {
        if (target == null) return;
        revealTimer += dt;
        while (revealTimer >= revealInterval && revealedCount < words.length) {
            revealTimer -= revealInterval;
            revealRandom();
        }
        if (revealedCount >= words.length) {
            award(20, 1.0);
            app.bank.record(target, false);
            nextCard();
        }
    }

    private void claimRecall() {
        if (over || countdown > 0 || target == null) return;
        double frac = words.length == 0 ? 1.0 : (double) revealedCount / words.length;
        int g = (int) Math.round((1.0 - frac) * 150) + 20;
        award(g, 1.0);
        Sound.good();
        app.bank.record(target, true);
        nextCard();
    }

    @Override protected void onKey(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) claimRecall();
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) return;
        if (target == null) return;

        Theme.center(g, target.fullRef(), Theme.bold(12), Theme.GOLD, w / 2.0, h * 0.16);

        int size = q(30);
        Font f = Theme.arabic(size);
        List<String> lines = Theme.wrap(g, displayText(), f, (int) (Math.min(900, w - 80) - 48));
        double lh = size * 1.7;
        double cy = h * 0.40;
        double ty = cy - (lines.size() - 1) * lh / 2.0;
        for (int i = 0; i < lines.size(); i++) {
            Theme.shadowCenter(g, lines.get(i), f, Theme.GOLD, w / 2.0, ty + i * lh + size * 0.36);
        }

        double frac = words.length == 0 ? 1.0 : (double) revealedCount / words.length;
        Theme.bar(g, (w - 400) / 2.0, h * 0.66, 400, 10, frac, Theme.CYAN);
        Theme.center(g, revealedCount + "/" + words.length + " words revealed",
                Theme.bold(12), Theme.a(Theme.DIM, 210), w / 2.0, h * 0.66 + 28);
    }

    private String displayText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            if (i > 0) sb.append(' ');
            if (revealed[i]) sb.append(words[i]);
            else sb.append(mask(words[i]));
        }
        return sb.toString();
    }

    private String mask(String w) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < w.length(); i++) sb.append('\u00b7');
        return sb.toString();
    }

    @Override protected String footerHint() {
        return "press SPACE as soon as you can recall the ayah  -  fewer revealed words = more points";
    }
}
