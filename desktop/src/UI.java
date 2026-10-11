import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.util.List;

/** Drawing helpers for buttons, HUD chrome and shared page furniture. */
public final class UI {
    private UI() {}

    public static void drawBtn(Graphics2D g, Btn b) {
        if (!b.visible) return;
        float v = b.vis;
        double lift = -4.0 * v;
        double grow = 3.0 * v;
        double squash = -2.5 * b.pressT;
        double x = b.x - grow;
        double y = b.y - grow + lift - squash;
        double w = b.w + grow * 2;
        double h = b.h + grow * 2 + squash;

        if (b.style == 0) {
            /* solid neon pill */
            Shape s = Theme.rr(x, y, w, h, h / 2.0);
            if (b.enabled) Theme.glow(g, s, b.color, 2.0f, 6);
            Color top = b.enabled ? Theme.mix(b.color, Color.WHITE, 0.28 + 0.18 * v) : Theme.a(Theme.DIM, 110);
            Color bot = b.enabled ? Theme.mix(b.color, Theme.BG_A, 0.28 + 0.14 * v) : Theme.a(Theme.DIM, 70);
            g.setPaint(new GradientPaint((float) x, (float) y, top, (float) x, (float) (y + h), bot));
            g.fill(s);
            g.setColor(Theme.a(Color.WHITE, (int) (110 + 110 * v)));
            g.setStroke(new BasicStroke(1.6f));
            g.draw(s);
            Font f = Theme.display((int) Math.max(12, Math.min(h * 0.42, 30)));
            Theme.center(g, b.label, f, Theme.DARK, x + w / 2, y + h / 2);

        } else if (b.style == 1) {
            /* ghost / outline */
            Shape s = Theme.rr(x, y, w, h, h / 2.0);
            g.setColor(Theme.a(b.color, (int) (22 + 45 * v)));
            g.fill(s);
            g.setColor(Theme.a(b.color, (int) (140 + 115 * v)));
            g.setStroke(new BasicStroke(2f));
            g.draw(s);
            if (v > 0.05f) Theme.glow(g, s, b.color, 1.2f, 4);
            Font f = Theme.bold((int) Math.max(12, Math.min(h * 0.40, 24)));
            Theme.center(g, b.label, f, b.color, x + w / 2, y + h / 2);

        } else {
            /* dark card / menu tile */
            double rad = Math.min(18, h * 0.20);
            Shape s = Theme.rr(x, y, w, h, rad);
            double tint = b.enabled ? 1.0 : 0.62;
            g.setPaint(new GradientPaint(
                    (float) x, (float) y, Theme.mix(Theme.CARD, b.color, (0.05 + 0.13 * v) * tint),
                    (float) x, (float) (y + h), Theme.mix(Theme.DARK, b.color, (0.03 + 0.10 * v) * tint)));
            g.fill(s);
            g.setColor(Theme.a(b.color, (int) ((65 + 190 * v) * tint)));
            g.setStroke(new BasicStroke(1.5f + 1.4f * v));
            g.draw(s);

            Shape acc = Theme.rr(x + 5, y + 7, 4, h - 14, 2);
            g.setColor(Theme.a(b.color, (int) ((130 + 125 * v) * tint)));
            g.fill(acc);

            /* Badge first, plus a matching left gutter for the label, so the
               number can never be drawn on top of the option text. */
            if (b.badge != null) {
                double br = Math.min(15, h * 0.19);
                double bx = x + br + 10;
                double by = y + h / 2;
                g.setColor(Theme.a(b.color, 210));
                g.fill(new Ellipse2D.Double(bx - br, by - br, br * 2, br * 2));
                Theme.center(g, b.badge, Theme.bold((int) (br * 1.15)), Theme.DARK, bx, by);
            }

            if (b.sub == null) {
                Color tc = b.enabled ? Theme.mix(Theme.INK, b.color, 0.45 * v) : Theme.a(Theme.INK, 215);
                double gutter = b.badge != null ? Math.min(48, w * 0.14) : 10;
                double textCx = x + gutter + (w - gutter - 12) / 2.0;
                int avail = (int) Math.max(40, w - gutter - 24);

                /* Largest size at which the label fits, wrapping onto at most
                   two lines rather than shrinking into unreadable dust. */
                int fs = b.fontPx > 0 ? b.fontPx : (int) Math.max(11, Math.min(h * 0.42, w * 0.15));
                String[] draw = null;
                while (fs > 9) {
                    Font cand = Theme.bold(fs);
                    List<String> ls = Theme.wrap(g, b.label, cand, avail);
                    if (ls.size() <= 1) {
                        draw = new String[] { ls.isEmpty() ? b.label : ls.get(0) };
                    } else {
                        StringBuilder rest = new StringBuilder();
                        for (int i = 1; i < ls.size(); i++) {
                            if (i > 1) rest.append(' ');
                            rest.append(ls.get(i));
                        }
                        draw = new String[] { ls.get(0), rest.toString() };
                    }
                    FontMetrics fm = g.getFontMetrics(cand);
                    int widest = 0;
                    for (int i = 0; i < draw.length; i++) widest = Math.max(widest, fm.stringWidth(draw[i]));
                    if (widest <= avail && draw.length * (fs + 5) <= h - 4) break;
                    fs--;
                }
                if (draw == null) draw = new String[] { b.label };

                Font lf = Theme.bold(fs);
                double lh = fs * 1.14;
                double ty = y + h / 2 - (draw.length - 1) * lh / 2.0;
                for (int i = 0; i < draw.length; i++) {
                    Theme.center(g, draw[i], lf, tc, textCx, ty + i * lh);
                }
            } else {
                Font ft = Theme.display((int) Math.max(11, Math.min(h * 0.22, w * 0.10)));
                Font fs = Theme.body(Theme.fitFont(g, b.sub, (int) (w - 46),
                        (int) Math.max(10, Math.min(h * 0.125, w * 0.055)), 9, Font.PLAIN));
                Color tc = b.enabled ? Theme.mix(Theme.INK, b.color, 0.40 * v) : Theme.a(Theme.DIM, 190);
                Color sc = b.enabled ? Theme.mix(Theme.DIM, b.color, 0.50 * v) : Theme.a(Theme.DIM, 150);
                double tileCx = x + w / 2 + 5;
                Theme.center(g, b.label, ft, tc, tileCx, y + h * 0.37);
                Theme.center(g, b.sub, fs, sc, tileCx, y + h * 0.63);
            }

            if (b.corner != null) {
                g.setFont(Theme.mono(11));
                FontMetrics fm = g.getFontMetrics();
                g.setColor(Theme.a(b.color, (int) (110 + 110 * v)));
                g.drawString(b.corner,
                        (float) (x + w - fm.stringWidth(b.corner) - 13),
                        (float) (y + 15 + fm.getAscent()));
            }
        }

        if (b.flash > 0.01f) {
            Shape s = Theme.rr(x, y, w, h, Math.min(18, h * 0.30));
            g.setColor(Theme.a(b.flashColor, (int) (190 * b.flash)));
            g.fill(s);
        }
    }

    /** Slightly translucent glass panel with a coloured rim. */
    public static void panel(Graphics2D g, double x, double y, double w, double h, double r, Color rim) {
        Shape s = Theme.rr(x, y, w, h, r);
        g.setColor(new Color(10, 13, 32, 195));
        g.fill(s);
        g.setColor(Theme.a(rim, 55));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(s);
    }

    /** Small pill-shaped stat readout used along the top of the games. */
    public static void chip(Graphics2D g, double x, double y, double w, double h,
                            String caption, String value, Color c) {
        Shape s = Theme.rr(x, y, w, h, h / 2);
        g.setColor(Theme.a(Theme.DARK, 200));
        g.fill(s);
        g.setColor(Theme.a(c, 90));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(s);

        Font fc = Theme.bold(11);
        Font fv = Theme.mono(18);
        g.setFont(fc);
        int cw = g.getFontMetrics().stringWidth(caption);
        g.setFont(fv);
        int vw = g.getFontMetrics().stringWidth(value);
        double total = cw + 8 + vw;
        double tx = x + (w - total) / 2.0;

        g.setFont(fc);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(Theme.DIM);
        g.drawString(caption, (float) tx, (float) (y + h / 2 + fm.getAscent() / 2.0 - 2));
        g.setFont(fv);
        g.setColor(c);
        g.drawString(value, (float) (tx + cw + 8), (float) (y + h / 2 + 6));
    }

    /** Circular countdown ring. frac 1 = full, 0 = empty. */
    public static void ring(Graphics2D g, double cx, double cy, double r, double frac, Color c, float thickness) {
        frac = Theme.clamp01(frac);
        Shape full = new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2);
        g.setColor(Theme.a(Color.WHITE, 20));
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(full);

        Shape arc = new Arc2D.Double(cx - r, cy - r, r * 2, r * 2, 90, -360.0 * frac, Arc2D.OPEN);
        Color hot = frac < 0.28 ? Theme.RED : (frac < 0.55 ? Theme.GOLD : c);
        Theme.glow(g, arc, hot, thickness, 5);
        g.setColor(hot);
        g.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(arc);
    }
}
