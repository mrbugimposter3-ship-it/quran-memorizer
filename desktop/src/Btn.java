import java.awt.Color;

/**
 * A hand-drawn, hit-tested button. Deliberately not a Swing widget so it can
 * be animated and styled freely and looks identical on every platform
 * look-and-feel.
 */
class Btn {
    public double x, y, w, h;
    public String label;
    public String sub;          // optional second line (used by menu tiles)
    public String badge;        // optional small circle on the left (hotkey hint)
    public String corner;       // optional small caption in the top-right corner
    public Color color;
    public int style;           // 0 = solid pill, 1 = ghost, 2 = card / tile
    public boolean visible = true, enabled = true, hover, down;
    public float vis, flash;
    public Color flashColor = Color.WHITE;
    public Runnable action;
    public Object tag;
    public int key = -1;        // optional KeyEvent.VK_* shortcut
    public int fontPx = 0;      // > 0 overrides the auto-computed card label size
    public double pressT;

    public Btn(double x, double y, double w, double h, String label, Color color, int style, Runnable action) {
        this.x = x; this.y = y; this.w = w; this.h = h;
        this.label = label; this.color = color; this.style = style; this.action = action;
    }

    public Btn at(double nx, double ny) { this.x = nx; this.y = ny; return this; }
    public Btn size(double nw, double nh) { this.w = nw; this.h = nh; return this; }

    public boolean hit(double mx, double my) {
        if (!visible || !enabled) return false;
        double pad = style == 2 ? 2 : 3;
        return mx >= x - pad && mx <= x + w + pad && my >= y - pad && my <= y + h + pad;
    }

    public void press() {
        if (visible && enabled && action != null) {
            pressT = 1.0;
            action.run();
        }
    }

    public void flash(Color c) { flash = 1f; flashColor = c; }

    public void update(double dt) {
        vis += ((hover ? 1.0 : 0.0) - vis) * Math.min(1.0, dt * 15.0);
        if (flash > 0) flash = (float) Math.max(0, flash - dt * 2.8);
        if (pressT > 0) pressT = Math.max(0, pressT - dt * 5.0);
    }
}