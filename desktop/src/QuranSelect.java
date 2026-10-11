import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * QURAN SELECT - choose what to memorise: a surah, a juz, a page, or the
 * whole Qur'an. The chosen range becomes the active study set.
 */
public class QuranSelect extends Screen {

    private static final int MODE_SURAH = 0, MODE_JUZ = 1, MODE_PAGE = 2;

    private final Btn btnSurah;
    private final Btn btnJuz;
    private final Btn btnPage;
    private final Btn btnFull;
    private final Btn btnBack;

    private int mode = MODE_SURAH;
    private double scroll = 0;
    private int selectedRow = -1;

    private static final double ROW_H = 46;
    private static final double LIST_TOP = 150;
    private static final double LIST_BOTTOM_PAD = 96;

    public QuranSelect(MemoriBlitz app) {
        super(app);
        repaintDivisor = 2;

        btnSurah = new Btn(0, 0, 150, 44, "SURAH", Theme.CYAN, 1, new Runnable() {
            public void run() { setMode(MODE_SURAH); }
        });
        btnJuz = new Btn(0, 0, 150, 44, "JUZ", Theme.GOLD, 1, new Runnable() {
            public void run() { setMode(MODE_JUZ); }
        });
        btnPage = new Btn(0, 0, 150, 44, "PAGE", Theme.ORANGE, 1, new Runnable() {
            public void run() { setMode(MODE_PAGE); }
        });
        btnFull = new Btn(0, 0, 220, 46, "FULL QUR'AN", Theme.LIME, 0, new Runnable() {
            public void run() {
                app.bank.setStudy(app.bank.quran.ayahs, "Full Qur'an (6236 ayahs)");
                app.goMenu();
            }
        });
        btnBack = new Btn(0, 0, 200, 46, "< BACK", Theme.DIM, 1, new Runnable() {
            public void run() { app.goMenu(); }
        });

        btns.add(btnSurah);
        btns.add(btnJuz);
        btns.add(btnPage);
        btns.add(btnFull);
        btns.add(btnBack);
    }

    private void setMode(int m) {
        mode = m;
        scroll = 0;
        selectedRow = -1;
    }

    private int rowCount() {
        if (mode == MODE_SURAH) return app.bank.quran.surahs.size();
        if (mode == MODE_JUZ) return 30;
        return 604;
    }

    private String rowLabel(int i) {
        if (mode == MODE_SURAH) {
            QuranData.Surah s = app.bank.quran.surah(i + 1);
            return i + 1 + ". " + s.english + "  ·  " + s.translit + "  ·  " + s.ayahCount + " ayahs  ·  " + s.type;
        }
        if (mode == MODE_JUZ) return app.bank.quran.juzLabel(i + 1);
        return app.bank.quran.pageLabel(i + 1);
    }

    private void select(int row) {
        List<QuranData.Ayah> list;
        String label;
        if (mode == MODE_SURAH) {
            list = app.bank.quran.ayahsForSurah(row + 1);
            QuranData.Surah s = app.bank.quran.surah(row + 1);
            label = "Surah " + (row + 1) + " " + s.english;
        } else if (mode == MODE_JUZ) {
            list = app.bank.quran.ayahsForJuz(row + 1);
            label = "Juz " + (row + 1);
        } else {
            list = app.bank.quran.ayahsForPage(row + 1);
            label = "Page " + (row + 1);
        }
        selectedRow = row;
        app.bank.setStudy(list, label + " (" + list.size() + " ayahs)");
        Sound.good();
        fx.burst(getWidth() / 2.0, getHeight() / 2.0, Theme.LIME, 30);
        app.goMenu();
    }

    @Override
    public void enter() {
        scroll = 0;
        selectedRow = -1;
    }

    @Override
    protected void onWheel(int notches) {
        scroll += notches * 44;
        clampScroll();
    }

    private void clampScroll() {
        double listH = getHeight() - LIST_TOP - LIST_BOTTOM_PAD;
        double max = Math.max(0, rowCount() * ROW_H - listH);
        if (scroll < 0) scroll = 0;
        if (scroll > max) scroll = max;
    }

    @Override
    protected boolean onPress(int x, int y) {
        if (y < LIST_TOP) return false;
        double listH = getHeight() - LIST_TOP - LIST_BOTTOM_PAD;
        if (y > LIST_TOP + listH) return false;
        int row = (int) ((y - LIST_TOP + scroll) / ROW_H);
        if (row >= 0 && row < rowCount()) {
            select(row);
            return true;
        }
        return false;
    }

    @Override
    protected void onKey(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_DOWN) { scroll += ROW_H; clampScroll(); }
        else if (k == KeyEvent.VK_UP) { scroll -= ROW_H; clampScroll(); }
        else if (k == KeyEvent.VK_PAGE_DOWN) { scroll += getHeight() * 0.8; clampScroll(); }
        else if (k == KeyEvent.VK_PAGE_UP) { scroll -= getHeight() * 0.8; clampScroll(); }
    }

    @Override
    protected void render(Graphics2D g, int w, int h) {
        Theme.center(g, "SELECT WHAT TO MEMORISE", Theme.bold(18), Theme.INK, w / 2.0, 40);
        Theme.center(g, "current: " + app.bank.studyLabel + "  ·  " + app.bank.size() + " ayahs",
                Theme.bold(12), Theme.a(Theme.GOLD, 210), w / 2.0, 62);

        // mode buttons
        double total = 3 * 150 + 2 * 14;
        double bx = (w - total) / 2.0;
        double by = 82;
        btnSurah.x = bx; btnSurah.y = by;
        btnJuz.x = bx + 150 + 14; btnJuz.y = by;
        btnPage.x = bx + 2 * (150 + 14); btnPage.y = by;
        btnSurah.color = mode == MODE_SURAH ? Theme.CYAN : Theme.DIM;
        btnJuz.color = mode == MODE_JUZ ? Theme.GOLD : Theme.DIM;
        btnPage.color = mode == MODE_PAGE ? Theme.ORANGE : Theme.DIM;

        // list
        double listH = h - LIST_TOP - LIST_BOTTOM_PAD;
        double listW = Math.min(860, w - 80);
        double lx = (w - listW) / 2.0;

        g.setClip((int) lx, (int) LIST_TOP, (int) listW, (int) listH);
        int first = (int) (scroll / ROW_H);
        int last = (int) ((scroll + listH) / ROW_H) + 1;
        for (int i = first; i <= last && i < rowCount(); i++) {
            double y = LIST_TOP + i * ROW_H - scroll;
            boolean sel = i == selectedRow;
            Color c = sel ? Theme.LIME : Theme.CYAN;
            Shape s = Theme.rr(lx + 6, y + 4, listW - 12, ROW_H - 8, 12);
            g.setColor(sel ? Theme.a(Theme.LIME, 40) : Theme.a(Theme.CARD, 220));
            g.fill(s);
            g.setColor(Theme.a(c, sel ? 200 : 70));
            g.setStroke(new BasicStroke(1.3f));
            g.draw(s);
            Theme.text(g, rowLabel(i), Theme.bold(15), sel ? Theme.LIME : Theme.INK, lx + 24, y + ROW_H / 2 + 5, 0);
        }
        g.setClip(null);

        // scroll hint
        Theme.center(g, "scroll with the mouse wheel  ·  \u2191\u2193 keys  ·  click to select",
                Theme.bold(11), Theme.a(Theme.DIM, 170), w / 2.0, h - 70);

        // bottom buttons
        btnFull.x = w / 2.0 - 220 - 10;
        btnFull.y = h - 56;
        btnBack.x = w / 2.0 + 10;
        btnBack.y = h - 56;
    }
}
