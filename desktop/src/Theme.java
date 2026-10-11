import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Central palette, fonts and painting helpers for QuranMemorizer.
 *
 * The look is a calm "green + coffee" theme: deep espresso-to-forest
 * backgrounds with fresh leaf-greens for success and warm cream / caramel /
 * terracotta for warmth. Everything is drawn by hand on Graphics2D.
 */
public final class Theme {
    private Theme() {}

    /* ---- background gradient (espresso -> forest) ---- */
    public static final Color BG_A = new Color(32, 24, 16);   // espresso
    public static final Color BG_B = new Color(26, 42, 28);   // dark green
    public static final Color BG_C = new Color(15, 30, 20);   // deep forest

    /* ---- green + coffee accent palette ---- */
    public static final Color CYAN   = new Color(63, 191, 127);  // emerald (primary)
    public static final Color PINK   = new Color(192, 122, 90);   // caramel
    public static final Color LIME   = new Color(134, 217, 76);   // leaf green (correct)
    public static final Color GOLD   = new Color(231, 200, 115);  // coffee cream
    public static final Color VIOLET = new Color(143, 163, 90);    // olive / sage
    public static final Color ORANGE = new Color(201, 138, 91);   // terracotta
    public static final Color RED    = new Color(224, 90, 78);     // warm red (wrong)

    /* ---- neutrals ---- */
    public static final Color INK     = new Color(242, 232, 213);  // cream text
    public static final Color DIM     = new Color(166, 151, 124);  // tan
    public static final Color CARD    = new Color(43, 35, 24);     // dark coffee card
    public static final Color CARD_HI = new Color(60, 50, 34);     // lighter coffee
    public static final Color DARK    = new Color(23, 16, 9);      // near-black coffee

    /* green/coffee cycle used for cycling effects */
    public static final Color[] RAINBOW = { CYAN, PINK, LIME, GOLD, VIOLET, ORANGE };

    /* ---- fonts, resolved against what the machine actually has ---- */
    public static final String F_DISPLAY;
    public static final String F_BODY;
    public static final String F_MONO;
    public static final String F_ARABIC;

    static {
        Set<String> have = new HashSet<String>();
        String[] fam = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (int i = 0; i < fam.length; i++) have.add(fam[i].toLowerCase());
        F_DISPLAY = pick(have, "Arial Black", "Segoe UI Black", "Impact", "Verdana", "Tahoma", "Dialog");
        F_BODY    = pick(have, "Segoe UI", "Tahoma", "Verdana", "Dialog");
        F_MONO    = pick(have, "Consolas", "Lucida Console", "Courier New", "Monospaced");
        F_ARABIC  = pick(have, "Traditional Arabic", "Arabic Typesetting", "Amiri", "Segoe UI", "Tahoma", "Arial", "Dialog");
    }

    private static String pick(Set<String> have, String... names) {
        for (int i = 0; i < names.length; i++) {
            if (have.contains(names[i].toLowerCase())) return names[i];
        }
        return Font.SANS_SERIF;
    }

    public static Font display(int s) { return new Font(F_DISPLAY, Font.BOLD, s); }
    public static Font body(int s)    { return new Font(F_BODY, Font.PLAIN, s); }
    public static Font bold(int s)    { return new Font(F_BODY, Font.BOLD, s); }
    public static Font mono(int s)    { return new Font(F_MONO, Font.BOLD, s); }
    public static Font arabic(int s)  { return new Font(F_ARABIC, Font.PLAIN, s); }

    /* ---- small math helpers ---- */
    public static int clamp(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }
    public static double clamp01(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    public static double ease(double t) { t = clamp01(t); return 1 - Math.pow(1 - t, 3); }

    public static Color a(Color c, int alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), clamp(alpha));
    }

    public static Color mix(Color a, Color b, double t) {
        t = clamp01(t);
        return new Color(
                (int) (a.getRed()   + (b.getRed()   - a.getRed())   * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue()  + (b.getBlue()  - a.getBlue())  * t));
    }

    /* ---- painting ---- */
    public static void backdrop(Graphics2D g, int w, int h) {
        g.setPaint(new LinearGradientPaint(0f, 0f, w * 0.75f, h,
                new float[] { 0f, 0.52f, 1f },
                new Color[] { BG_A, BG_B, BG_C }));
        g.fillRect(0, 0, w, h);
    }

    public static Shape rr(double x, double y, double w, double h, double r) {
        double d = Math.min(r, Math.min(w, h) / 2.0);
        if (d < 0) d = 0;
        return new RoundRectangle2D.Double(x, y, w, h, d * 2, d * 2);
    }

    public static void fillRR(Graphics2D g, Color c, double x, double y, double w, double h, double r) {
        g.setColor(c);
        g.fill(rr(x, y, w, h, r));
    }

    /** Cheap fake bloom: stroke the same shape several times, widening + fading. */
    public static void glow(Graphics2D g, Shape s, Color c, float base, int layers) {
        Composite old = g.getComposite();
        for (int i = layers; i >= 1; i--) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.11f / i));
            g.setStroke(new BasicStroke(base + i * 2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.setColor(c);
            g.draw(s);
        }
        g.setComposite(old);
    }

    /** align: 0 = left, 1 = centre, 2 = right. y is the baseline. */
    public static void text(Graphics2D g, String s, Font f, Color c, double x, double y, int align) {
        g.setFont(f);
        g.setColor(c);
        FontMetrics fm = g.getFontMetrics();
        float dx = 0;
        if (align == 1) dx = -fm.stringWidth(s) / 2f;
        else if (align == 2) dx = -fm.stringWidth(s);
        g.drawString(s, (float) x + dx, (float) y);
    }

    /** Vertically + horizontally centred single line. */
    public static void center(Graphics2D g, String s, Font f, Color c, double cx, double cy) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float x = (float) cx - fm.stringWidth(s) / 2f;
        float y = (float) cy + (fm.getAscent() - fm.getDescent()) / 2f;
        g.setColor(c);
        g.drawString(s, x, y);
    }

    /** Centred text with a soft halo. */
    public static void glowCenter(Graphics2D g, String s, Font f, Color c, double cx, double cy) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float x = (float) cx - fm.stringWidth(s) / 2f;
        float y = (float) cy + (fm.getAscent() - fm.getDescent()) / 2f;
        Composite old = g.getComposite();
        g.setColor(c);
        for (int r = 5; r >= 1; r--) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.055f));
            for (int k = 0; k < 8; k++) {
                double ang = k * Math.PI / 4.0;
                g.drawString(s, x + (float) (Math.cos(ang) * r * 1.5), y + (float) (Math.sin(ang) * r * 1.5));
            }
        }
        g.setComposite(old);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    /** Centred drop-shadowed text, for busy backgrounds. */
    public static void shadowCenter(Graphics2D g, String s, Font f, Color c, double cx, double cy) {
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        float x = (float) cx - fm.stringWidth(s) / 2f;
        float y = (float) cy + (fm.getAscent() - fm.getDescent()) / 2f;
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(s, x + 2, y + 2);
        g.setColor(c);
        g.drawString(s, x, y);
    }

    /** Greedy word wrap for long phrases. */
    public static List<String> wrap(Graphics2D g, String s, Font f, int maxW) {
        List<String> out = new ArrayList<String>();
        if (s == null) return out;
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        String[] parts = s.trim().split("\\s+");
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String test = cur.length() == 0 ? parts[i] : cur.toString() + " " + parts[i];
            if (fm.stringWidth(test) > maxW && cur.length() > 0) {
                out.add(cur.toString());
                cur = new StringBuilder(parts[i]);
            } else {
                cur = new StringBuilder(test);
            }
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }

    /** Largest font size in [lo, hi] whose rendered width still fits maxW. */
    public static int fitFont(Graphics2D g, String s, int maxW, int hi, int lo, int style) {
        if (s == null) return lo;
        for (int size = hi; size > lo; size--) {
            if (g.getFontMetrics(new Font(F_BODY, style, size)).stringWidth(s) <= maxW) return size;
        }
        return lo;
    }

    /** Same, but using the display face. */
    public static int fitDisplay(Graphics2D g, String s, int maxW, int hi, int lo) {
        if (s == null) return lo;
        for (int size = hi; size > lo; size--) {
            if (g.getFontMetrics(new Font(F_DISPLAY, Font.BOLD, size)).stringWidth(s) <= maxW) return size;
        }
        return lo;
    }

    /** Chunky progress / timer bar. */
    public static void bar(Graphics2D g, double x, double y, double w, double h, double frac, Color c) {
        frac = clamp01(frac);
        fillRR(g, a(Color.WHITE, 20), x, y, w, h, h / 2);
        if (frac > 0.002) {
            Shape s = rr(x, y, Math.max(h, w * frac), h, h / 2);
            g.setPaint(new GradientPaint((float) x, (float) y, c,
                    (float) (x + w), (float) y, mix(c, Color.WHITE, 0.45)));
            g.fill(s);
            glow(g, s, c, 1.5f, 4);
        }
    }

    /** Expands a centred multi-line block of text, returns the total height. */
    public static double block(Graphics2D g, String s, Font f, Color c, double cx, double topY, int maxW, double lineGap) {
        List<String> lines = wrap(g, s, f, maxW);
        double y = topY;
        for (int i = 0; i < lines.size(); i++) {
            shadowCenter(g, lines.get(i), f, c, cx, y);
            y += lineGap;
        }
        return y - topY;
    }
}
