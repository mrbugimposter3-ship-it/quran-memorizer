import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

/**
 * Tiny software synthesiser - no audio assets required.
 * Every effect is generated as raw PCM on the fly and played on a single
 * daemon thread so rapid-fire clicks can never stall the UI.
 */
public final class Sound {
    private Sound() {}

    private static volatile boolean on = true;
    private static final float RATE = 44100f;
    private static final Random RND = new Random();
    private static final AtomicInteger PENDING = new AtomicInteger();

    private static final ExecutorService POOL = Executors.newSingleThreadExecutor(new ThreadFactory() {
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "memoriblitz-sfx");
            t.setDaemon(true);
            return t;
        }
    });

    public static void setOn(boolean b) { on = b; }
    public static boolean isOn() { return on; }
    public static void toggle() { on = !on; if (on) click(); }

    /* ------------------------------------------------------------------ */
    /* synthesis                                                            */
    /* ------------------------------------------------------------------ */

    /** wave: 0 sine, 1 square, 2 saw, 3 noise */
    private static double[] osc(double f0, double f1, int ms, double vol, int wave, double decay) {
        int n = Math.max(1, (int) (RATE * ms / 1000.0));
        double[] out = new double[n];
        double phase = 0;
        for (int i = 0; i < n; i++) {
            double p = (double) i / (double) n;
            double f = f0 + (f1 - f0) * p;
            phase += 2 * Math.PI * f / RATE;
            double s;
            if (wave == 0) s = Math.sin(phase);
            else if (wave == 1) s = Math.sin(phase) >= 0 ? 1 : -1;
            else if (wave == 2) { double ph = phase / (2 * Math.PI); s = 2 * (ph - Math.floor(ph)) - 1; }
            else s = RND.nextDouble() * 2 - 1;

            double env = Math.exp(-decay * p) * (1 - p * 0.12);
            if (i < 220) env *= i / 220.0;                 // de-click
            int tail = n - i;
            if (tail < 400) env *= Math.min(1.0, tail / 400.0);
            out[i] = s * env * vol;
        }
        return out;
    }

    private static double[] cat(double[]... parts) {
        int n = 0;
        for (int i = 0; i < parts.length; i++) n += parts[i].length;
        double[] out = new double[n];
        int k = 0;
        for (int i = 0; i < parts.length; i++) {
            System.arraycopy(parts[i], 0, out, k, parts[i].length);
            k += parts[i].length;
        }
        return out;
    }

    private static byte[] toBytes(double[] s) {
        byte[] out = new byte[s.length * 2];
        for (int i = 0; i < s.length; i++) {
            double v = s[i];
            if (v > 1) v = 1;
            if (v < -1) v = -1;
            int q = (int) (v * 31000);
            out[i * 2] = (byte) (q & 0xFF);
            out[i * 2 + 1] = (byte) ((q >> 8) & 0xFF);
        }
        return out;
    }

    private static void play(final byte[] pcm) {
        if (!on) return;
        if (PENDING.get() > 6) return;              // drop rather than lag behind
        PENDING.incrementAndGet();
        POOL.submit(new Runnable() {
            public void run() {
                try {
                    AudioFormat fmt = new AudioFormat(RATE, 16, 1, true, false);
                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
                    SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(fmt);
                    line.start();
                    line.write(pcm, 0, pcm.length);
                    line.drain();
                    line.close();
                } catch (Throwable t) {
                    on = false;                      // no mixer available; stay silent
                } finally {
                    PENDING.decrementAndGet();
                }
            }
        });
    }

    /* ------------------------------------------------------------------ */
    /* the actual sound effects                                             */
    /* ------------------------------------------------------------------ */

    public static void click() {
        play(toBytes(osc(1050, 1500, 50, 0.16, 0, 7)));
    }

    public static void tick() {
        play(toBytes(osc(1750, 1750, 26, 0.09, 0, 11)));
    }

    public static void hover() {
        play(toBytes(osc(2200, 2600, 18, 0.05, 0, 14)));
    }

    public static void good() {
        play(toBytes(cat(osc(660, 660, 65, 0.20, 0, 5), osc(990, 990, 95, 0.20, 0, 4))));
    }

    public static void great() {
        play(toBytes(cat(
                osc(660, 660, 55, 0.19, 0, 5),
                osc(880, 880, 55, 0.19, 0, 5),
                osc(1320, 1320, 120, 0.21, 0, 3))));
    }

    public static void combo(int n) {
        int step = Math.min(n, 12);
        double f = 520 + step * 78;
        play(toBytes(cat(
                osc(f, f, 45, 0.16, 0, 6),
                osc(f * 1.5, f * 1.5, 70, 0.18, 0, 4))));
    }

    public static void wrong() {
        play(toBytes(cat(osc(240, 150, 110, 0.20, 1, 3), osc(150, 92, 150, 0.18, 3, 3))));
    }

    public static void whoosh() {
        play(toBytes(osc(1000, 190, 170, 0.11, 3, 2.2)));
    }

    public static void pop() {
        play(toBytes(osc(320, 900, 70, 0.16, 0, 8)));
    }

    public static void levelUp() {
        play(toBytes(cat(
                osc(523, 523, 70, 0.18, 0, 4),
                osc(659, 659, 70, 0.18, 0, 4),
                osc(784, 784, 70, 0.18, 0, 4),
                osc(1046, 1046, 190, 0.20, 0, 2.4))));
    }

    public static void fanfare() {
        play(toBytes(cat(
                osc(523, 523, 110, 0.18, 0, 3),
                osc(659, 659, 110, 0.18, 0, 3),
                osc(784, 784, 110, 0.18, 0, 3),
                osc(1046, 1046, 260, 0.21, 0, 2),
                osc(1318, 1318, 320, 0.19, 0, 1.8))));
    }

    public static void gameOver() {
        play(toBytes(cat(
                osc(440, 440, 150, 0.18, 2, 2.4),
                osc(330, 330, 160, 0.18, 2, 2.4),
                osc(220, 165, 420, 0.20, 2, 2.0))));
    }
}
