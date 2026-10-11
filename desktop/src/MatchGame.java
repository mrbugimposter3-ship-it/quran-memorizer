import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * MATCH - a board of transliterations on the left and meanings on the right,
 * shuffled. Tap a left tile then its matching right tile. Clear the board
 * before the round timer runs out; each cleared board adds a pair.
 */
public class MatchGame extends GameScreen {

    private static final int MAX_PAIRS = 6;

    private final Random rnd = new Random();

    private final Btn[] leftBtn = new Btn[MAX_PAIRS];
    private final Btn[] rightBtn = new Btn[MAX_PAIRS];

    private QuranData.Ayah[] pairs = new QuranData.Ayah[MAX_PAIRS];
    private int[] leftOrder = new int[MAX_PAIRS];
    private int[] rightOrder = new int[MAX_PAIRS];
    private boolean[] leftDone = new boolean[MAX_PAIRS];
    private boolean[] rightDone = new boolean[MAX_PAIRS];

    private int pairCount = 4;
    private int selectedLeft = -1;
    private double roundLeft = 30;
    private double roundLimit = 30;
    private int lastGain;

    public MatchGame(MemoriBlitz app) {
        super(app);
        key = "match";
        title = "MATCH";
        blurb = "Pair each transliteration with its meaning.";
        accent = Theme.GOLD;

        for (int i = 0; i < MAX_PAIRS; i++) {
            final int li = i;
            leftBtn[i] = new Btn(0, 0, 10, 10, "", Theme.CYAN, 2, new Runnable() {
                public void run() { tapLeft(li); }
            });
            leftBtn[i].visible = false;
            btns.add(leftBtn[i]);

            final int ri = i;
            rightBtn[i] = new Btn(0, 0, 10, 10, "", Theme.GOLD, 2, new Runnable() {
                public void run() { tapRight(ri); }
            });
            rightBtn[i].visible = false;
            btns.add(rightBtn[i]);
        }
    }

    @Override
    protected int startLives() { return 3; }

    @Override
    protected double startTime() { return -1; }

    @Override
    protected void resetGame() {
        pairCount = 4;
        roundLimit = 30;
        roundLeft = roundLimit;
        selectedLeft = -1;
        newBoard();
    }

    private void newBoard() {
        if (app.bank.size() < pairCount) {
            pairCount = Math.max(2, app.bank.size());
        }
        // pick distinct ayahs
        List<QuranData.Ayah> pool = new ArrayList<QuranData.Ayah>(app.bank.study);
        Collections.shuffle(pool, rnd);
        for (int i = 0; i < pairCount; i++) {
            pairs[i] = pool.get(i);
            leftOrder[i] = i;
            rightOrder[i] = i;
            leftDone[i] = false;
            rightDone[i] = false;
        }
        shuffle(leftOrder, pairCount);
        shuffle(rightOrder, pairCount);
        selectedLeft = -1;
        roundLeft = roundLimit;
    }

    private void shuffle(int[] a, int n) {
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    private void tapLeft(int i) {
        if (over || countdown > 0) return;
        if (leftDone[i]) return;
        selectedLeft = i;
        Sound.click();
    }

    private void tapRight(int j) {
        if (over || countdown > 0) return;
        if (rightDone[j]) return;
        if (selectedLeft < 0) { Sound.hover(); return; }

        int li = selectedLeft;
        if (leftOrder[li] == rightOrder[j]) {
            // match!
            leftDone[li] = true;
            rightDone[j] = true;
            selectedLeft = -1;
            lastGain = award(150, 0.5 + 0.5 * Theme.clamp01(roundLeft / roundLimit));
            Sound.good();
            if (combo > 0 && combo % 4 == 0) Sound.combo(mult());
            fx.burst(leftBtn[li].x + leftBtn[li].w / 2, leftBtn[li].y + leftBtn[li].h / 2, Theme.LIME, 26);
            fx.burst(rightBtn[j].x + rightBtn[j].w / 2, rightBtn[j].y + rightBtn[j].h / 2, Theme.LIME, 26);
            fx.pop(rightBtn[j].x + rightBtn[j].w / 2, rightBtn[j].y - 10, "+" + lastGain, Theme.LIME, Theme.display(26));
            app.bank.record(pairs[li], true);
            if (boardClear()) {
                Sound.levelUp();
                toast("BOARD CLEARED!", Theme.GOLD);
                fx.flash(accent);
                pairCount = Math.min(MAX_PAIRS, pairCount + 1);
                roundLimit = Math.max(16, roundLimit - 2);
                newBoard();
            }
        } else {
            // mismatch
            lives--;
            penalize(-30);
            Sound.wrong();
            kick(0.8);
            fx.flash(Theme.RED);
            leftBtn[li].flash(Theme.RED);
            rightBtn[j].flash(Theme.RED);
            app.bank.record(pairs[li], false);
            selectedLeft = -1;
            if (lives <= 0) finish();
        }
    }

    private boolean boardClear() {
        for (int i = 0; i < pairCount; i++) if (!leftDone[i]) return false;
        return true;
    }

    @Override
    protected void round(double dt) {
        roundLeft -= dt;
        if (roundLeft <= 0) {
            roundLeft = 0;
            finish();
        }
    }

    @Override
    protected void paintGame(Graphics2D g, int w, int h) {
        if (over) {
            for (int i = 0; i < MAX_PAIRS; i++) { leftBtn[i].visible = false; rightBtn[i].visible = false; }
            return;
        }

        Theme.center(g, "MATCH THE PAIRS", Theme.bold(13), Theme.a(Theme.DIM, 230), w / 2.0, 78);
        Theme.center(g, app.bank.studyLabel, Theme.bold(11), Theme.a(Theme.GOLD, 190), w / 2.0, 96);

        double gap = 14;
        double margin = 40;
        double midGap = 60;
        double colW = (w - margin * 2 - midGap) / 2.0;
        double rowH = (h - 140 - (pairCount - 1) * gap) / pairCount;
        rowH = Math.min(rowH, 120);
        double topY = 126;

        for (int i = 0; i < pairCount; i++) {
            double x = margin;
            double y = topY + i * (rowH + gap);
            leftBtn[i].x = x;
            leftBtn[i].y = y;
            leftBtn[i].w = colW;
            leftBtn[i].h = rowH;
            leftBtn[i].label = pairs[leftOrder[i]].translit;
            leftBtn[i].visible = !leftDone[i];
            leftBtn[i].enabled = !leftDone[i];
            leftBtn[i].color = (selectedLeft == i) ? Theme.LIME : Theme.CYAN;
            leftBtn[i].fontPx = q((int) Math.max(13, Math.min(rowH * 0.26, 20)));

            double rx = margin + colW + midGap;
            rightBtn[i].x = rx;
            rightBtn[i].y = y;
            rightBtn[i].w = colW;
            rightBtn[i].h = rowH;
            rightBtn[i].label = pairs[rightOrder[i]].english;
            rightBtn[i].visible = !rightDone[i];
            rightBtn[i].enabled = !rightDone[i];
            rightBtn[i].color = Theme.GOLD;
            rightBtn[i].fontPx = q((int) Math.max(12, Math.min(rowH * 0.24, 18)));
        }

        // round timer bar
        double barW = Math.min(500, w - 200);
        double frac = roundLimit > 0 ? Theme.clamp01(roundLeft / roundLimit) : 0;
        Color bc = frac < 0.25 ? Theme.RED : (frac < 0.5 ? Theme.GOLD : Theme.LIME);
        Theme.bar(g, (w - barW) / 2.0, h - 46, barW, 10, frac, bc);
        Theme.center(g, "pairs: " + pairCount + "   ·   tap left then right",
                Theme.bold(11), Theme.a(Theme.DIM, 190), w / 2.0, h - 58);
    }

    @Override
    protected void onFinish() {
        for (int i = 0; i < MAX_PAIRS; i++) { leftBtn[i].visible = false; rightBtn[i].visible = false; }
    }

    @Override
    protected String footerHint() {
        return "tap a transliteration, then its meaning   .   clear the board before time runs out";
    }

    @Override
    protected String adviceLine() {
        return "Pairing transliteration to meaning builds the link you need to recall.";
    }
}
