import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;


/**
 * READ - browse the current study set ayah by ayah. Each ayah is shown as a
 * card whose layout can be toggled between three modes:
 *   1 = stacked top-down (Arabic / translit / English / Urdu)
 *   2 = two columns (Arabic+translit | English+Urdu)
 *   3 = three columns (Arabic | English | Urdu) side by side
 * A surah navigator lets you jump between surahs (which also becomes the
 * active study set for the games), and the last scroll position is preserved
 * across visits.
 */
public class Reader extends Screen {

    private final Btn btnBack;
    private final Btn btnPrevSurah;
    private final Btn btnNextSurah;
    private final Btn btnSelect;
    private final Btn btnCols;

    private double scroll = 0;
    private double totalH = 0;
    private double[] ayahTop;
    private double[] ayahH;
    private int lastW = -1;
    private double fontScale = 1.0;
    private boolean autoScroll = false;
    private int speedMin = 15;   // minutes per juz
    private int currentSurah = 1;
    private int colMode = 3;     // 1 stacked, 2 two-col, 3 three-col

    private static final double TOP = 140;
    private static final double BOTTOM = 70;
    private static final double GAP = 14;       // gap between the columns
    private static final double COL_PAD = 24;   // left/right padding inside a card
    private static final double HEADER_H = 40;  // ref + emoji header
    private static final double PAD_BOTTOM = 16;

    public Reader(MemoriBlitz app) {
        super(app);
        repaintDivisor = 2;

        btnBack = new Btn(0, 0, 200, 46, "< BACK", Theme.DIM, 1, new Runnable() {
            public void run() {
                if (app.readerReturnKey != null) app.resumeGame(app.readerReturnKey);
                else app.goMenu();
            }
        });
        btns.add(btnBack);

        btnPrevSurah = new Btn(0, 0, 150, 40, "< PREV SURAH", Theme.CYAN, 1, new Runnable() {
            public void run() { goSurah(currentSurah - 1); }
        });
        btnPrevSurah.key = KeyEvent.VK_LEFT;
        btns.add(btnPrevSurah);

        btnNextSurah = new Btn(0, 0, 150, 40, "NEXT SURAH >", Theme.CYAN, 1, new Runnable() {
            public void run() { goSurah(currentSurah + 1); }
        });
        btnNextSurah.key = KeyEvent.VK_RIGHT;
        btns.add(btnNextSurah);

        btnSelect = new Btn(0, 0, 170, 40, "SELECT", Theme.LIME, 1, new Runnable() {
            public void run() { app.goSelect(); }
        });
        btnSelect.key = KeyEvent.VK_S;
        btns.add(btnSelect);

        btnCols = new Btn(0, 0, 150, 40, "COLS: 3", Theme.GOLD, 1, new Runnable() {
            public void run() { cycleCols(); }
        });
        btnCols.key = KeyEvent.VK_C;
        btns.add(btnCols);
    }

    private void cycleCols() {
        colMode = colMode >= 3 ? 1 : colMode + 1;
        lastW = -1;
        Sound.tick();
    }

    private void goSurah(int n) {
        if (n < 1 || n > app.bank.quran.surahs.size()) return;
        currentSurah = n;
        QuranData.Surah s = app.bank.quran.surah(n);
        app.bank.setStudy(app.bank.quran.ayahsForSurah(n),
                "Surah " + n + " " + s.english + " (" + s.ayahCount + " ayahs)");
        scroll = 0;
        lastW = -1;
        Sound.good();
    }

    @Override
    public void enter() {
        // keep the previous scroll position so re-opening the reader (e.g. from
        // a game's QURAN peek) returns to the same ayah instead of the top.
        lastW = -1;
        if (!app.bank.study.isEmpty()) currentSurah = app.bank.study.get(0).surah;
    }

    private void layout(Graphics2D g, int w) {
        if (w == lastW) return;
        lastW = w;
        int n = app.bank.size();
        ayahTop = new double[n];
        ayahH = new double[n];
        double listW = Math.min(900, w - 80);
        double y = 0;
        for (int i = 0; i < n; i++) {
            QuranData.Ayah a = app.bank.study.get(i);
            ayahTop[i] = y;
            double h = computeHeight(g, a, listW);
            ayahH[i] = h;
            y += h + 14;
        }
        totalH = y;
    }

    private double computeHeight(Graphics2D g, QuranData.Ayah a, double listW) {
        int as = (int) (26 * fontScale);
        int es = 15;
        int us = 15;
        boolean hasUrdu = a.urdu != null && !a.urdu.trim().isEmpty();
        double innerW = listW - 2 * COL_PAD - 8;
        if (colMode == 1) {
            // stacked: Arabic + translit + English + Urdu top-down
            double h = 0;
            h += Theme.wrap(g, a.arabic, Theme.arabic(as), (int) innerW).size() * as * 1.5;
            h += Theme.wrap(g, a.translit, Theme.body(13), (int) innerW).size() * 13 * 1.35;
            h += Theme.wrap(g, a.english, Theme.body(es), (int) innerW).size() * es * 1.35;
            if (hasUrdu) h += Theme.wrap(g, a.urdu, Theme.body(us), (int) innerW).size() * us * 1.35;
            return HEADER_H + h + PAD_BOTTOM;
        } else if (colMode == 2) {
            // two columns: Arabic+translit | English+Urdu
            double colW = (listW - 2 * COL_PAD - GAP) / 2.0;
            double left = 0, right = 0;
            left += Theme.wrap(g, a.arabic, Theme.arabic(as), (int) (colW - 8)).size() * as * 1.5;
            left += Theme.wrap(g, a.translit, Theme.body(13), (int) (colW - 8)).size() * 13 * 1.35;
            right += Theme.wrap(g, a.english, Theme.body(es), (int) (colW - 8)).size() * es * 1.35;
            if (hasUrdu) right += Theme.wrap(g, a.urdu, Theme.body(us), (int) (colW - 8)).size() * us * 1.35;
            return HEADER_H + Math.max(left, right) + PAD_BOTTOM;
        }
        // three columns (default)
        double colW = (listW - 2 * COL_PAD - 2 * GAP) / 3.0;
        int arLines = Theme.wrap(g, a.arabic, Theme.arabic(as), (int) (colW - 8)).size();
        int enLines = Theme.wrap(g, a.english, Theme.body(es), (int) (colW - 8)).size();
        int urLines = hasUrdu ? Theme.wrap(g, a.urdu, Theme.body(us), (int) (colW - 8)).size() : 0;
        double arH = arLines * as * 1.5;
        double enH = enLines * es * 1.35;
        double urH = urLines * us * 1.35;
        return HEADER_H + Math.max(arH, Math.max(enH, urH)) + PAD_BOTTOM;
    }

    private void clampScroll() {
        double listH = getHeight() - TOP - BOTTOM;
        double max = Math.max(0, totalH - listH);
        if (scroll < 0) scroll = 0;
        if (scroll > max) scroll = max;
    }

    @Override
    protected void onWheel(int notches) {
        scroll += notches * 60;
        clampScroll();
    }

    @Override
    protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_DOWN) { scroll += 60; clampScroll(); }
        else if (k == KeyEvent.VK_UP) { scroll -= 60; clampScroll(); }
        else if (k == KeyEvent.VK_PAGE_DOWN) { scroll += getHeight() * 0.8; clampScroll(); }
        else if (k == KeyEvent.VK_PAGE_UP) { scroll -= getHeight() * 0.8; clampScroll(); }
        else if (k == KeyEvent.VK_OPEN_BRACKET) { fontScale = Math.max(0.6, fontScale - 0.1); lastW = -1; }
        else if (k == KeyEvent.VK_CLOSE_BRACKET) { fontScale = Math.min(2.0, fontScale + 0.1); lastW = -1; }
        else if (k == KeyEvent.VK_A) { autoScroll = !autoScroll; }
        else if (k == KeyEvent.VK_COMMA) { speedMin = Math.max(5, speedMin - 5); }
        else if (k == KeyEvent.VK_PERIOD) { speedMin = Math.min(60, speedMin + 5); }
        else if (k == KeyEvent.VK_C) { cycleCols(); }
    }

    @Override
    protected void tick(double dt) {
        if (autoScroll) {
            double rate = 30000.0 / (speedMin * 60.0);
            scroll += rate * dt;
            clampScroll();
        }
    }

    @Override
    protected void render(Graphics2D g, int w, int h) {
        layout(g, w);
        clampScroll();

        Theme.center(g, "READ  ·  " + app.bank.studyLabel, Theme.bold(18), Theme.INK, w / 2.0, 38);
        Theme.center(g, app.bank.size() + " ayahs   ·   " + colLabel(), Theme.bold(12),
                Theme.a(Theme.GOLD, 210), w / 2.0, 60);

        // surah navigator + columns row
        double total = 150 + 150 + 170 + 150 + 3 * 14;
        double nx = (w - total) / 2.0;
        double ny = 78;
        btnPrevSurah.x = nx; btnPrevSurah.y = ny;
        btnNextSurah.x = nx + 150 + 14; btnNextSurah.y = ny;
        btnSelect.x = nx + 150 + 14 + 150 + 14; btnSelect.y = ny;
        btnCols.x = nx + 150 + 14 + 150 + 14 + 170 + 14; btnCols.y = ny;
        btnCols.label = "COLS: " + colMode;
        btnPrevSurah.enabled = currentSurah > 1;
        btnNextSurah.enabled = currentSurah < app.bank.quran.surahs.size();
        QuranData.Surah cs = app.bank.quran.surah(currentSurah);
        Theme.center(g, cs == null ? "" : cs.label(), Theme.bold(11), Theme.a(Theme.DIM, 180), w / 2.0, 128);

        double listH = h - TOP - BOTTOM;
        double listW = Math.min(900, w - 80);
        double lx = (w - listW) / 2.0;

        g.setClip((int) lx, (int) TOP, (int) listW, (int) listH);

        int n = app.bank.size();
        for (int i = 0; i < n; i++) {
            double y = TOP + ayahTop[i] - scroll;
            if (y + ayahH[i] < TOP || y > TOP + listH) continue;
            drawAyah(g, app.bank.study.get(i), i, lx, y, listW);
        }
        g.setClip(null);

        Theme.center(g, "scroll: wheel / \u2191\u2193   ·   font: [ ]   ·   autoscroll: A   ·   speed: , .   ·   surah: \u2190 \u2192   ·   cols: C",
                Theme.bold(11), Theme.a(Theme.DIM, 170), w / 2.0, h - 84);

        btnBack.x = w / 2.0 - 100;
        btnBack.y = h - 56;
    }

    private String colLabel() {
        switch (colMode) {
            case 1: return "Stacked  ·  Ar/Tr/En/Ur";
            case 2: return "2 columns  ·  Ar+Tr | En+Ur";
            default: return "3 columns  ·  Ar | En | Ur";
        }
    }

    private void drawAyah(Graphics2D g, QuranData.Ayah a, int i, double x, double y, double w) {
        double h = ayahH[i];
        Shape s = Theme.rr(x + 6, y + 4, w - 12, h, 14);
        g.setColor(Theme.a(Theme.CARD, 230));
        g.fill(s);
        g.setColor(Theme.a(Theme.CYAN, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(s);

        double cx = x + w / 2.0;
        Theme.center(g, Emoji.forAyah(a.english) + "  " + a.fullRef(), Theme.bold(12), Theme.GOLD, cx, y + 22);

        double bodyTop = y + HEADER_H;
        double bodyH = h - HEADER_H - PAD_BOTTOM;
        int as = (int) (26 * fontScale);
        int es = 15, us = 15;
        boolean hasUrdu = a.urdu != null && !a.urdu.trim().isEmpty();

        if (colMode == 1) {
            double innerW = w - 2 * COL_PAD - 8;
            double cur = bodyTop;
            int n = drawTop(g, a.arabic, Theme.arabic(as), Theme.GOLD, cx, innerW, cur, bodyTop + bodyH, as * 1.5);
            cur += n * as * 1.5;
            n = drawTop(g, a.translit, Theme.body(13), Theme.CYAN, cx, innerW, cur, bodyTop + bodyH, 13 * 1.35);
            cur += n * 13 * 1.35;
            n = drawTop(g, a.english, Theme.body(es), Theme.INK, cx, innerW, cur, bodyTop + bodyH, es * 1.35);
            cur += n * es * 1.35;
            if (hasUrdu) drawTop(g, a.urdu, Theme.body(us), Theme.PINK, cx, innerW, cur, bodyTop + bodyH, us * 1.35);
            return;
        } else if (colMode == 2) {
            double colW = (w - 2 * COL_PAD - GAP) / 2.0;
            double c1 = x + COL_PAD;
            double c2 = c1 + colW + GAP;
            // left column: Arabic (top) + translit (below)
            double cur = bodyTop;
            int n1 = drawTop(g, a.arabic, Theme.arabic(as), Theme.GOLD, c1 + colW / 2.0, colW - 8, cur, bodyTop + bodyH, as * 1.5);
            cur += n1 * as * 1.5;
            drawTop(g, a.translit, Theme.body(13), Theme.CYAN, c1 + colW / 2.0, colW - 8, cur, bodyTop + bodyH, 13 * 1.35);
            // right column: English (top) + Urdu (below)
            cur = bodyTop;
            int n2 = drawTop(g, a.english, Theme.body(es), Theme.INK, c2 + colW / 2.0, colW - 8, cur, bodyTop + bodyH, es * 1.35);
            cur += n2 * es * 1.35;
            if (hasUrdu) drawTop(g, a.urdu, Theme.body(us), Theme.PINK, c2 + colW / 2.0, colW - 8, cur, bodyTop + bodyH, us * 1.35);
            return;
        }

        // three columns (default, vertically centred)
        double colW = (w - 2 * COL_PAD - 2 * GAP) / 3.0;
        double col1 = x + COL_PAD;
        double col2 = col1 + colW + GAP;
        double col3 = col2 + colW + GAP;
        drawColumn(g, a.arabic, Theme.arabic(as), Theme.GOLD, col1 + colW / 2.0, colW, bodyTop, bodyH, as * 1.5);
        drawColumn(g, a.english, Theme.body(es), Theme.INK, col2 + colW / 2.0, colW, bodyTop, bodyH, es * 1.35);
        if (hasUrdu) {
            drawColumn(g, a.urdu, Theme.body(us), Theme.PINK, col3 + colW / 2.0, colW, bodyTop, bodyH, us * 1.35);
        }
    }

    /** Draw wrapped, centred lines starting at `top` (top-anchored). Returns lines drawn. */
    private int drawTop(Graphics2D g, String text, Font f, Color c, double cx, double maxW,
                        double top, double bottom, double lh) {
        if (text == null || text.trim().isEmpty()) return 0;
        List<String> lines = Theme.wrap(g, text, f, (int) maxW);
        double ty = top + lh * 0.34;
        int drawn = 0;
        for (String ln : lines) {
            if (top + drawn * lh > bottom) break;
            Theme.shadowCenter(g, ln, f, c, cx, ty);
            ty += lh;
            drawn++;
        }
        return drawn;
    }

    private void drawColumn(Graphics2D g, String text, Font f, Color c, double cx, double colW,
                            double top, double bodyH, double lh) {
        if (text == null) return;
        List<String> lines = Theme.wrap(g, text, f, (int) (colW - 8));
        double totalH = lines.size() * lh;
        double ty = top + (bodyH - totalH) / 2.0 + lh * 0.34;
        for (String ln : lines) {
            Theme.shadowCenter(g, ln, f, c, cx, ty);
            ty += lh;
        }
    }
}
