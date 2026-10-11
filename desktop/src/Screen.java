import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Base class for every page in the app.
 *
 * Owns a fixed 60 Hz clock (16 ms Swing timer), the button list, the particle
 * field and the screen-shake. Subclasses only implement {@link #render} and
 * {@link #tick}. Timers for off-screen pages are parked so only the visible
 * page costs anything.
 */
public abstract class Screen extends JPanel implements ActionListener {

    protected final MemoriBlitz app;
    protected final List<Btn> btns = new ArrayList<Btn>();
    protected final Fx fx = new Fx();
    private final Timer clock;

    private long prevNs;
    protected double time;
    protected double shake;
    protected int mx = -1, my = -1;

    /** Repaint every Nth frame. Screens full of real Swing widgets raise this. */
    protected int repaintDivisor = 1;
    private int frameCount;

    public Screen(MemoriBlitz app) {
        this.app = app;
        setOpaque(true);
        setBackground(Theme.BG_A);
        setFocusable(true);
        setLayout(null);

        clock = new Timer(16, this);
        clock.start();

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (!isShowing()) return;
                mx = e.getX(); my = e.getY();
                boolean onButton = false;
                for (int i = 0; i < btns.size(); i++) {
                    Btn b = btns.get(i);
                    if (b.hit(mx, my)) { b.down = true; onButton = true; }
                }
                if (!onButton) onPress(mx, my);
                requestFocusInWindow();
            }

            public void mouseReleased(MouseEvent e) {
                if (!isShowing()) return;
                for (int i = 0; i < btns.size(); i++) {
                    Btn b = btns.get(i);
                    if (b.down) {
                        b.down = false;
                        if (b.hit(e.getX(), e.getY())) {
                            Sound.click();
                            b.press();
                        }
                    }
                }
                onRelease(e.getX(), e.getY());
            }

            public void mouseExited(MouseEvent e) {
                for (int i = 0; i < btns.size(); i++) {
                    btns.get(i).hover = false;
                    btns.get(i).down = false;
                }
                setCursor(Cursor.getDefaultCursor());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) { moved(e); }
            public void mouseDragged(MouseEvent e) { moved(e); }
        });

        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (isShowing()) onWheel(e.getWheelRotation());
            }
        });

        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!isShowing()) return;
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { onEscape(); return; }
                if (e.getKeyCode() == KeyEvent.VK_F9) {
                    app.bank.questionScale = Math.max(0.7, app.bank.questionScale - 0.1);
                    app.bank.markDirty(); app.bank.save(); repaint(); return;
                }
                if (e.getKeyCode() == KeyEvent.VK_F10) {
                    app.bank.questionScale = Math.min(1.6, app.bank.questionScale + 0.1);
                    app.bank.markDirty(); app.bank.save(); repaint(); return;
                }
                for (int i = 0; i < btns.size(); i++) {
                    Btn b = btns.get(i);
                    if (b.key != -1 && b.key == e.getKeyCode() && b.visible && b.enabled) {
                        b.vis = 1f;
                        b.flash = 0.7f;
                        b.flashColor = b.color;
                        Sound.click();
                        b.press();
                        return;
                    }
                }
                onKey(e);
            }
        });
    }

    private void moved(MouseEvent e) {
        if (!isShowing()) return;
        mx = e.getX();
        my = e.getY();
        boolean any = false;
        for (int i = 0; i < btns.size(); i++) {
            Btn b = btns.get(i);
            boolean h = b.hit(mx, my);
            if (h && !b.hover && b.visible) Sound.hover();
            b.hover = h;
            if (h) any = true;
        }
        setCursor(Cursor.getPredefinedCursor(any ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
    }

    /* ------------------------------------------------------------------ */
    /* overridable hooks                                                    */
    /* ------------------------------------------------------------------ */

    /** Called just before the page is shown. */
    public void enter() {}
    /** Called just after the page is hidden. */
    public void leave() {}

    protected boolean onPress(int x, int y) { return false; }
    protected void onRelease(int x, int y) {}
    protected void onWheel(int notches) {}
    protected void onKey(KeyEvent e) {}
    protected void onEscape() { app.goMenu(); }

    protected void tick(double dt) {}
    protected abstract void render(Graphics2D g, int w, int h);

    /* ------------------------------------------------------------------ */
    /* the clock                                                            */
    /* ------------------------------------------------------------------ */

    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        double dt = prevNs == 0 ? 0.016 : (now - prevNs) / 1.0e9;
        prevNs = now;
        if (!isShowing()) return;                 // parked while off-screen
        if (dt > 0.08) dt = 0.08;                 // never explode after a stall
        time += dt;

        for (int i = 0; i < btns.size(); i++) btns.get(i).update(dt);
        fx.update(dt);
        if (shake > 0) shake = Math.max(0, shake - dt * 2.4);
        tick(dt);

        frameCount++;
        int div = repaintDivisor < 1 ? 1 : repaintDivisor;
        if (frameCount % div == 0) repaint();
    }

    /* ------------------------------------------------------------------ */
    /* painting                                                             */
    /* ------------------------------------------------------------------ */

    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            Theme.backdrop(g, w, h);

            if (shake > 0.001) {
                double s = shake * 18;
                g.translate((Math.random() - 0.5) * s, (Math.random() - 0.5) * s);
            }

            render(g, w, h);
            for (int i = 0; i < btns.size(); i++) UI.drawBtn(g, btns.get(i));
            fx.draw(g, w, h);
        } finally {
            g.dispose();
        }
    }

    /** Convenience: kick the camera. */
    protected void kick(double amount) {
        shake = Math.max(shake, amount);
    }
}
