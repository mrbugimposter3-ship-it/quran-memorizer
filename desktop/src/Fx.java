import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A very small particle system: sparks, confetti, expanding rings,
 * floating score pop-ups and motion streaks, plus a full-screen flash.
 */
public class Fx {
    private static final Random R = new Random();

    private static final int SPARK = 0, CONF = 1, RING = 2, TEXT = 3, STREAK = 4;

    private static class P {
        double x, y, vx, vy, drag, grav;
        double life, max;
        double size, rot, vrot, extra;
        int kind;
        Color c;
        String text;
        Font font;
    }

    private final List<P> ps = new ArrayList<P>();
    private Color flashColor = Theme.INK;
    private float flashAmt;

    public void clear() {
        ps.clear();
        flashAmt = 0;
    }

    public int size() { return ps.size(); }

    public void flash(Color c) { flashColor = c; flashAmt = 1f; }

    /* ---------------- emitters ---------------- */

    public void burst(double x, double y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            P p = new P();
            p.kind = SPARK;
            p.x = x; p.y = y;
            double a = R.nextDouble() * Math.PI * 2;
            double sp = 70 + R.nextDouble() * 340;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp;
            p.max = p.life = 0.30 + R.nextDouble() * 0.45;
            p.size = 1.8 + R.nextDouble() * 4.0;
            p.c = c;
            p.grav = 210; p.drag = 2.3;
            ps.add(p);
        }
    }

    public void shatter(double x, double y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            P p = new P();
            p.kind = CONF;
            p.x = x; p.y = y;
            double a = R.nextDouble() * Math.PI * 2;
            double sp = 60 + R.nextDouble() * 330;
            p.vx = Math.cos(a) * sp;
            p.vy = Math.sin(a) * sp - 90;
            p.max = p.life = 0.7 + R.nextDouble() * 0.9;
            p.size = 3 + R.nextDouble() * 5.5;
            p.rot = R.nextDouble() * Math.PI;
            p.vrot = (R.nextDouble() - 0.5) * 16;
            p.c = R.nextBoolean() ? c : Theme.mix(c, Color.WHITE, R.nextDouble() * 0.6);
            p.grav = 620; p.drag = 0.85;
            ps.add(p);
        }
    }

    public void rain(double x, double y, Color c, int n) {
        for (int i = 0; i < n; i++) {
            P p = new P();
            p.kind = CONF;
            p.x = x + (R.nextDouble() - 0.5) * 60;
            p.y = y;
            p.vx = (R.nextDouble() - 0.5) * 120;
            p.vy = 40 + R.nextDouble() * 120;
            p.max = p.life = 1.6 + R.nextDouble() * 1.4;
            p.size = 3 + R.nextDouble() * 5;
            p.rot = R.nextDouble() * Math.PI;
            p.vrot = (R.nextDouble() - 0.5) * 12;
            p.c = R.nextBoolean() ? c : Theme.mix(c, Color.WHITE, R.nextDouble() * 0.7);
            p.grav = 300; p.drag = 0.4;
            ps.add(p);
        }
    }

    public void ring(double x, double y, Color c, double maxRadius, double stroke, double seconds) {
        P p = new P();
        p.kind = RING;
        p.x = x; p.y = y;
        p.max = p.life = seconds;
        p.size = maxRadius;
        p.extra = stroke;
        p.c = c;
        ps.add(p);
    }

    public void pop(double x, double y, String text, Color c, Font f) {
        P p = new P();
        p.kind = TEXT;
        p.x = x; p.y = y;
        p.vy = -95;
        p.vx = (R.nextDouble() - 0.5) * 40;
        p.max = p.life = 0.95;
        p.c = c;
        p.text = text;
        p.font = f;
        p.grav = 0; p.drag = 1.6;
        ps.add(p);
    }

    public void streak(double x, double y, Color c) {
        P p = new P();
        p.kind = STREAK;
        p.x = x; p.y = y;
        p.vx = (R.nextDouble() - 0.5) * 260;
        p.vy = (R.nextDouble() - 0.5) * 260;
        p.max = p.life = 0.25 + R.nextDouble() * 0.25;
        p.size = 1.5 + R.nextDouble() * 2.5;
        p.c = c;
        p.drag = 3.0; p.grav = 0;
        ps.add(p);
    }

    /** Confetti from the top edge of the screen. */
    public void celebrate(int w, int h) {
        for (int i = 0; i < 160; i++) {
            Color c = Theme.RAINBOW[R.nextInt(Theme.RAINBOW.length)];
            double x = R.nextDouble() * w;
            P p = new P();
            p.kind = CONF;
            p.x = x;
            p.y = -20 - R.nextDouble() * h * 0.5;
            p.vx = (R.nextDouble() - 0.5) * 90;
            p.vy = 120 + R.nextDouble() * 260;
            p.max = p.life = 2.2 + R.nextDouble() * 1.6;
            p.size = 4 + R.nextDouble() * 6;
            p.rot = R.nextDouble() * Math.PI;
            p.vrot = (R.nextDouble() - 0.5) * 14;
            p.c = c;
            p.grav = 180; p.drag = 0.25;
            ps.add(p);
        }
    }

    /* ---------------- simulation ---------------- */

    public void update(double dt) {
        for (int i = ps.size() - 1; i >= 0; i--) {
            P p = ps.get(i);
            p.life -= dt;
            if (p.life <= 0) { ps.remove(i); continue; }
            p.vy += p.grav * dt;
            double d = Math.exp(-p.drag * dt);
            p.vx *= d;
            p.vy *= d;
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.rot += p.vrot * dt;
        }
        if (flashAmt > 0) flashAmt = (float) Math.max(0, flashAmt - dt * 3.4);
    }

    /* ---------------- rendering ---------------- */

    public void draw(Graphics2D g, int w, int h) {
        Composite old = g.getComposite();
        Stroke oldStroke = g.getStroke();

        for (int i = 0; i < ps.size(); i++) {
            P p = ps.get(i);
            double t = Theme.clamp01(p.life / p.max);
            float al = (float) t;

            if (p.kind == SPARK) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, al));
                g.setColor(p.c);
                double s = p.size * (0.35 + t * 0.9);
                g.fill(new Ellipse2D.Double(p.x - s, p.y - s, s * 2, s * 2));

            } else if (p.kind == CONF) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, al));
                g.setColor(p.c);
                AffineTransform at = g.getTransform();
                g.translate(p.x, p.y);
                g.rotate(p.rot);
                g.fill(new Rectangle2D.Double(-p.size, -p.size * 0.42, p.size * 2, p.size * 0.84));
                g.setTransform(at);

            } else if (p.kind == RING) {
                double r = (1 - t) * p.size;
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, al * 0.9f));
                g.setColor(p.c);
                g.setStroke(new BasicStroke((float) p.extra, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Ellipse2D.Double(p.x - r, p.y - r, r * 2, r * 2));

            } else if (p.kind == TEXT) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, al));
                g.setFont(p.font);
                FontMetrics fm = g.getFontMetrics();
                float tx = (float) (p.x - fm.stringWidth(p.text) / 2.0);
                g.setColor(new Color(0, 0, 0, (int) (130 * al)));
                g.drawString(p.text, tx + 2, (float) p.y + 2);
                g.setColor(p.c);
                g.drawString(p.text, tx, (float) p.y);

            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, al * 0.85f));
                g.setColor(p.c);
                g.setStroke(new BasicStroke((float) p.size, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(p.x, p.y, p.x - p.vx * 0.035, p.y - p.vy * 0.035));
            }
        }

        g.setComposite(old);
        g.setStroke(oldStroke);

        if (flashAmt > 0.01f) {
            g.setColor(Theme.a(flashColor, (int) (78 * flashAmt)));
            g.fillRect(0, 0, w, h);
        }
    }
}
