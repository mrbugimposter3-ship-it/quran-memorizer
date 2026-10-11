import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * NUMBER STORY - each ayah is turned into a one-line "number story": its
 * surah, ayah, juz, hizb, page and position-on-page are encoded as famous-book
 * names (Lord of the Rings, Sherlock, Gatsby, Mockingbird, Lord of the Flies,
 * Geronimo Stilton, ...). One number is blanked each round and you recall it
 * from its book story.
 */
public class StoryGame extends QuizGame {

    private static final String[] FIELD = { "SURAH", "AYAH", "JUZ", "HIZB", "PAGE", "POS" };

    private String arabicText = "";
    private String storyText = "";
    private int questionField = 0;
    private int[] fieldVals = new int[6];

    private int[] juzMin = new int[31];
    private int[] juzMax = new int[31];
    private boolean juzInit = false;

    public StoryGame(MemoriBlitz app) {
        super(app);
        key = "story";
        title = "NUMBER STORY";
        blurb = "Each ayah as a number-story of famous books";
        accent = Theme.GOLD;
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        questionField = rnd.nextInt(6);
        fieldVals[0] = target.surah;
        fieldVals[1] = target.ayah;
        fieldVals[2] = target.juz;
        fieldVals[3] = hizb(target);
        fieldVals[4] = target.page;
        fieldVals[5] = posOnPage(target);
        arabicText = target.arabic;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if (i > 0) sb.append("   \u00b7   ");
            sb.append(FIELD[i]).append(' ');
            sb.append(i == questionField ? "?" : String.valueOf(fieldVals[i]));
            sb.append('=').append(Mnemonic.story(fieldVals[i]));
        }
        storyText = sb.toString();

        String right = String.valueOf(fieldVals[questionField]);
        List<String> wrong = buildWrong(questionField, fieldVals[questionField]);
        setOptions(right, wrong);
    }

    private int[] range(int field) {
        switch (field) {
            case 0: return new int[] { 1, 114 };   // surah
            case 1: return new int[] { 1, 286 };   // ayah
            case 2: return new int[] { 1, 30 };    // juz
            case 3: return new int[] { 1, 60 };    // hizb
            case 4: return new int[] { 1, 604 };   // page
            default: return new int[] { 1, 30 };   // position on page
        }
    }

    private List<String> buildWrong(int field, int v) {
        int[] r = range(field);
        List<String> out = new ArrayList<String>();
        int guard = 0;
        while (out.size() < 3 && guard++ < 300) {
            int d = rnd.nextInt(21) - 10;
            if (d == 0) d = 6;
            int cand = v + d;
            if (cand >= r[0] && cand <= r[1] && cand != v && !out.contains(String.valueOf(cand))) {
                out.add(String.valueOf(cand));
            }
        }
        return out;
    }

    /** 2 hizbs per juz, split at the midpoint of the juz's page span. */
    private int hizb(QuranData.Ayah a) {
        initJuz();
        int span = juzMax[a.juz] - juzMin[a.juz] + 1;
        boolean firstHalf = (a.page - juzMin[a.juz]) < span / 2.0;
        return (a.juz - 1) * 2 + (firstHalf ? 1 : 2);
    }

    /** 1-based position of the ayah on its page. */
    private int posOnPage(QuranData.Ayah a) {
        int c = 0;
        for (QuranData.Ayah x : app.bank.quran.ayahs) {
            if (x.page == a.page && x.global < a.global) c++;
        }
        return c + 1;
    }

    private void initJuz() {
        if (juzInit) return;
        juzInit = true;
        for (QuranData.Ayah a : app.bank.quran.ayahs) {
            if (juzMin[a.juz] == 0 || a.page < juzMin[a.juz]) juzMin[a.juz] = a.page;
            if (a.page > juzMax[a.juz]) juzMax[a.juz] = a.page;
        }
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        if (over) { for (int i = 0; i < 4; i++) optBtn[i].visible = false; return; }
        if (target == null) return;

        // header: emoji + surah:ayah (no juz/page spoiler)
        Theme.center(g, Emoji.forAyah(target.english) + "  " + target.surah + ":" + target.ayah,
                Theme.bold(12), Theme.GOLD, w / 2.0, h * 0.08);

        // card: Arabic ayah + the number story
        double cardW = Math.min(900, w * 0.86);
        double cardX = (w - cardW) / 2.0;
        double cardY = h * 0.115;
        double cardH = h * 0.42;

        Shape card = Theme.rr(cardX, cardY, cardW, cardH, 24);
        g.setColor(Theme.a(Theme.CARD, 230));
        g.fill(card);
        g.setColor(Theme.a(accent, 70));
        g.setStroke(new BasicStroke(1.6f));
        g.draw(card);

        // Arabic
        Font af = Theme.arabic(q(24));
        List<String> alines = Theme.wrap(g, arabicText, af, (int) (cardW - 80));
        if (alines == null) alines = Theme.wrap(g, arabicText, Theme.arabic(q(18)), (int) (cardW - 80));
        double ay = cardY + cardH * 0.22;
        for (String ln : alines) {
            Theme.shadowCenter(g, ln, af, Theme.GOLD, w / 2.0, ay);
            ay += q(30);
        }

        // story sentence
        Font sf = Theme.body(q(14));
        List<String> slines = Theme.wrap(g, storyText, sf, (int) (cardW - 80));
        if (slines == null) slines = Theme.wrap(g, storyText, Theme.body(q(12)), (int) (cardW - 80));
        double sy = cardY + cardH * 0.28 + q(40);
        for (String ln : slines) {
            Theme.center(g, ln, sf, Theme.INK, w / 2.0, sy);
            sy += q(20);
        }

        Theme.center(g, "What " + FIELD[questionField] + " is this ayah?", Theme.bold(q(13)), Theme.a(accent, 220), w / 2.0, cardY + cardH - 18);

        // 2x2 option grid
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
        }
    }

    @Override protected String footerHint() {
        return "Recall the blank number from its book story  ·  1-4 or click";
    }
}
