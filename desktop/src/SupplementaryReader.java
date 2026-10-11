import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for a supplementary text (Qaseeda Burdah, Dalail al-Khayrat, ...).
 * Loads a pipe-delimited .data file and shows it as a scrollable list.
 */
public class SupplementaryReader extends Screen {

    private static class Item {
        String kind;      // "T" title, "C" chapter, "V" verse
        String arabic, translit, english;
    }

    private final Btn btnBack;
    private final String fileName;
    private final List<Item> items = new ArrayList<Item>();

    private double scroll = 0;
    private double totalH = 0;
    private double[] itemTop;
    private double[] itemH;
    private int lastW = -1;

    private static final double TOP = 110;
    private static final double BOTTOM = 70;

    public SupplementaryReader(MemoriBlitz app, String fileName) {
        super(app);
        this.fileName = fileName;
        repaintDivisor = 2;
        load();

        btnBack = new Btn(0, 0, 200, 46, "< BACK", Theme.DIM, 1, new Runnable() {
            public void run() { app.goMenu(); }
        });
        btns.add(btnBack);
    }

    private void load() {
        items.clear();
        BufferedReader br = null;
        try {
            for (String c : new String[] { "res/" + fileName, fileName, "res\\" + fileName }) {
                File f = new File(c);
                if (f.exists()) {
                    br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                    break;
                }
            }
            if (br == null) return;
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                Item it = new Item();
                it.kind = p[0];
                if (p[0].equals("T")) {
                    it.arabic = p.length > 1 ? p[1] : "";
                    it.translit = p.length > 2 ? p[2] : "";
                } else if (p[0].equals("C")) {
                    it.arabic = p.length > 1 ? p[1] : "";
                } else if (p[0].equals("V")) {
                    it.arabic = p.length > 1 ? p[1] : "";
                    it.translit = p.length > 2 ? p[2] : "";
                    it.english = p.length > 3 ? p[3] : "";
                }
                items.add(it);
            }
        } catch (Throwable t) {
        } finally {
            if (br != null) try { br.close(); } catch (Throwable ig) {}
        }
    }

    @Override
    public void enter() {
        scroll = 0;
        lastW = -1;
    }

    private void layout(Graphics2D g, int w) {
        if (w == lastW) return;
        lastW = w;
        int n = items.size();
        itemTop = new double[n];
        itemH = new double[n];
        double listW = Math.min(860, w - 80);
        double y = 0;
        for (int i = 0; i < n; i++) {
            Item it = items.get(i);
            itemTop[i] = y;
            double h = computeHeight(g, it, listW);
            itemH[i] = h;
            y += h + 12;
        }
        totalH = y;
    }

    private double computeHeight(Graphics2D g, Item it, double listW) {
        if (it.kind.equals("T")) return 40;
        if (it.kind.equals("C")) return 34;
        double h = 8;
        int as = 24;
        List<String> al = Theme.wrap(g, it.arabic, Theme.arabic(as), (int) (listW - 48));
        h += al.size() * (as * 1.5);
        int ts = 17;
        List<String> tl = Theme.wrap(g, it.translit, Theme.display(ts), (int) (listW - 48));
        h += tl.size() * (ts * 1.22);
        int es = 14;
        List<String> el = Theme.wrap(g, it.english, Theme.body(es), (int) (listW - 48));
        h += el.size() * (es * 1.35) + 10;
        return h;
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
    }

    @Override
    protected void render(Graphics2D g, int w, int h) {
        layout(g, w);

        String title = items.isEmpty() ? fileName : items.get(0).arabic;
        Theme.center(g, title, Theme.bold(20), Theme.INK, w / 2.0, 40);
        if (items.size() > 1 && items.get(0).kind.equals("T") && items.get(0).translit.length() > 0) {
            Theme.center(g, items.get(0).translit, Theme.bold(12), Theme.a(Theme.GOLD, 210), w / 2.0, 62);
        }

        double listH = h - TOP - BOTTOM;
        double listW = Math.min(860, w - 80);
        double lx = (w - listW) / 2.0;

        g.setClip((int) lx, (int) TOP, (int) listW, (int) listH);
        for (int i = 0; i < items.size(); i++) {
            double y = TOP + itemTop[i] - scroll;
            if (y + itemH[i] < TOP || y > TOP + listH) continue;
            drawItem(g, items.get(i), i, lx, y, listW);
        }
        g.setClip(null);

        Theme.center(g, "scroll with the mouse wheel  ·  \u2191\u2193 keys",
                Theme.bold(11), Theme.a(Theme.DIM, 170), w / 2.0, h - 84);

        btnBack.x = w / 2.0 - 100;
        btnBack.y = h - 56;
    }

    private void drawItem(Graphics2D g, Item it, int idx, double x, double y, double w) {
        double cx = x + w / 2.0;
        if (it.kind.equals("T")) {
            Theme.center(g, it.arabic, Theme.bold(22), Theme.GOLD, cx, y + 24);
            return;
        }
        if (it.kind.equals("C")) {
            Shape s = Theme.rr(x + 6, y + 2, w - 12, 30, 12);
            g.setColor(Theme.a(Theme.CARD_HI, 230));
            g.fill(s);
            Theme.center(g, it.arabic, Theme.bold(14), Theme.CYAN, cx, y + 21);
            return;
        }
        // verse
        Shape s = Theme.rr(x + 6, y + 4, w - 12, itemH[idx], 14);
        g.setColor(Theme.a(Theme.CARD, 230));
        g.fill(s);
        g.setColor(Theme.a(Theme.CYAN, 60));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(s);

        double ty = y + 24;
        int as = 24;
        List<String> al = Theme.wrap(g, it.arabic, Theme.arabic(as), (int) (w - 48));
        for (String ln : al) {
            Theme.shadowCenter(g, ln, Theme.arabic(as), Theme.GOLD, cx, ty + as * 0.36);
            ty += as * 1.5;
        }
        ty += 4;
        int ts = 17;
        List<String> tl = Theme.wrap(g, it.translit, Theme.display(ts), (int) (w - 48));
        for (String ln : tl) {
            Theme.shadowCenter(g, ln, Theme.display(ts), Theme.INK, cx, ty + ts * 0.36);
            ty += ts * 1.22;
        }
        ty += 4;
        int es = 14;
        List<String> el = Theme.wrap(g, it.english, Theme.body(es), (int) (w - 48));
        for (String ln : el) {
            Theme.shadowCenter(g, ln, Theme.body(es), Theme.DIM, cx, ty + es * 0.36);
            ty += es * 1.35;
        }
    }
}
