import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main menu: drifting transliterations, a colour-cycling title and one tile
 * per game mode with its personal best. Keys 1-4 launch a game directly.
 */
public class MenuScreen extends Screen {

    private static class Ghost {
        String text;
        double x, y, vy, scale;
        int alpha;
        Color c;
    }

    private static final String[] KEYS  = {
            "test", "match", "flash", "fill",
            "recite", "order", "next", "arabic", "reverse", "pair",
            "initials", "speed", "grid", "fillar", "jumble",
            "vsai", "versebuild", "urdu", "reveal", "word",
            "mix", "oddone", "wordcount", "edgeword", "nextword",
            "partial", "en2ur", "ur2en", "ur2ar", "ar2tr",
            "surahinfo", "wheresurah", "wherejuz", "wherepage", "whereayah", "story"
    };
    private static final String[] NAMES = {
            "TEST", "MATCH", "FLASH", "FILL",
            "RECITE", "ORDER", "NEXT", "ARABIC", "REVERSE", "PAIR",
            "INITIALS", "SPEED", "GRID", "FILL-AR", "JUMBLE",
            "VS AI", "VERSE BUILD", "URDU", "REVEAL", "WORD WALK",
            "MIX", "ODD ONE OUT", "WORD COUNT", "FIRST/LAST", "NEXT WORD",
            "PARTIAL", "EN\u2192URDU", "URDU\u2192EN", "URDU\u2192AR", "AR\u2192TRANSLIT",
            "SURAH TRIVIA", "WHICH SURAH", "WHICH JUZ", "WHICH PAGE", "AYAH NO.", "NUMBER STORY"
    };
    private static final String[] BLURB = {
            "Match the ayah to its meaning before the bar drains",
            "Pair each transliteration with its meaning",
            "Self-graded cards for the ayahs you keep missing",
            "Supply the missing word of the ayah",
            "Type the full transliteration from memory",
            "Tap the words in the correct order",
            "Pick the ayah that follows",
            "See the Arabic, choose its meaning",
            "See the meaning, choose the Arabic",
            "Match Arabic phrases to their transliterations",
            "Recall the ayah from first letters",
            "Rapid true/false against the clock",
            "Memory game: match Arabic with its meaning",
            "Supply the missing Arabic word",
            "Unscramble the scrambled letters",
            "Race the clock on surah / page / juz",
            "Build the ayah word by word",
            "See the Arabic, choose its Urdu meaning",
            "An ayah is revealed word by word - recall it early",
            "Step a surah word by word with meaning and context",
            "Cycles through every mode at random",
            "Three ayahs share a surah - pick the odd one out",
            "Count the Arabic words of the ayah",
            "Recall the first or last word",
            "Supply the final word of the ayah",
            "Identify the full ayah from a fragment",
            "See the English, choose its Urdu",
            "See the Urdu, choose its English",
            "See the Urdu, choose the Arabic",
            "See the Arabic, choose its transliteration",
            "Ayah counts and revelation place",
            "Name the surah of the ayah",
            "Name the juz of the ayah",
            "Name the page of the ayah",
            "Name the ayah number",
            "Each ayah as a number-story of famous books"
    };
    private static final Color[] HUES = {
            Theme.CYAN, Theme.GOLD, Theme.VIOLET, Theme.ORANGE,
            Theme.CYAN, Theme.GOLD, Theme.VIOLET, Theme.ORANGE, Theme.PINK, Theme.PINK,
            Theme.LIME, Theme.RED, Theme.VIOLET, Theme.ORANGE, Theme.GOLD,
            Theme.RED, Theme.LIME, Theme.ORANGE, Theme.VIOLET, Theme.GOLD,
            Theme.CYAN, Theme.VIOLET, Theme.CYAN, Theme.GOLD, Theme.ORANGE,
            Theme.CYAN, Theme.ORANGE, Theme.PINK, Theme.LIME, Theme.VIOLET,
            Theme.GOLD, Theme.CYAN, Theme.GOLD, Theme.VIOLET, Theme.PINK, Theme.GOLD
    };

    private final Random rnd = new Random();
    private final List<Ghost> ghosts = new ArrayList<Ghost>();
    private final Btn[] tiles = new Btn[KEYS.length];
    private final Btn btnSelect;
    private final Btn btnRead;
    private final Btn btnSound;
    private final Btn btnReset;
    private final Btn btnReminder;
    private final Btn btnFont;
    private final Btn btnBurdah;
    private final Btn btnDalail;

    private double pulse;
    private int lastGhostW;
    private double menuScroll = 0;
    private double gridTotalH = 0;
    private double gridViewH = 0;

    public MenuScreen(MemoriBlitz app) {
        super(app);

        for (int i = 0; i < KEYS.length; i++) {
            final String k = KEYS[i];
            tiles[i] = new Btn(0, 0, 10, 10, NAMES[i], HUES[i], 2, new Runnable() {
                public void run() { MenuScreen.this.app.startGame(k); }
            });
            tiles[i].sub = BLURB[i];
            if (i < 9) {
                tiles[i].badge = String.valueOf(i + 1);
                tiles[i].key = KeyEvent.VK_1 + i;
            } else if (i == 9) {
                tiles[i].badge = "0";
                tiles[i].key = KeyEvent.VK_0;
            }
            btns.add(tiles[i]);
        }

        btnSelect = new Btn(0, 0, 230, 46, "SELECT SURAH / JUZ / PAGE", Theme.LIME, 0, new Runnable() {
            public void run() { MenuScreen.this.app.goSelect(); }
        });
        btnSelect.key = KeyEvent.VK_S;
        btns.add(btnSelect);

        btnRead = new Btn(0, 0, 150, 46, "READ", Theme.CYAN, 1, new Runnable() {
            public void run() { MenuScreen.this.app.goReader(); }
        });
        btnRead.key = KeyEvent.VK_R;
        btns.add(btnRead);

        btnSound = new Btn(0, 0, 150, 46, "SOUND ON", Theme.VIOLET, 1, new Runnable() {
            public void run() {
                Sound.toggle();
                MenuScreen.this.app.bank.soundOn = Sound.isOn();
                MenuScreen.this.app.bank.markDirty();
            }
        });
        btnSound.key = KeyEvent.VK_M;
        btns.add(btnSound);

        btnReset = new Btn(0, 0, 160, 46, "RESET STATS", Theme.DIM, 1, new Runnable() {
            public void run() {
                MenuScreen.this.app.bank.resetStats();
                fx.burst(getWidth() / 2.0, getHeight() * 0.5, Theme.DIM, 20);
            }
        });
        btns.add(btnReset);

        btnReminder = new Btn(0, 0, 130, 46, "REMINDER", Theme.GOLD, 1, new Runnable() {
            public void run() { MenuScreen.this.app.adjustReminder(); }
        });
        btns.add(btnReminder);

        btnFont = new Btn(0, 0, 110, 46, "FONT 100%", Theme.CYAN, 1, new Runnable() {
            public void run() {
                double s = MenuScreen.this.app.bank.questionScale + 0.2;
                if (s > 1.6) s = 0.8;
                MenuScreen.this.app.bank.questionScale = s;
                MenuScreen.this.app.bank.markDirty();
                MenuScreen.this.app.bank.save();
                refresh();
            }
        });
        btns.add(btnFont);

        btnBurdah = new Btn(0, 0, 120, 46, "BURDAH", Theme.ORANGE, 1, new Runnable() {
            public void run() { MenuScreen.this.app.goBurdah(); }
        });
        btns.add(btnBurdah);

        btnDalail = new Btn(0, 0, 120, 46, "DALAIL", Theme.PINK, 1, new Runnable() {
            public void run() { MenuScreen.this.app.goDalail(); }
        });
        btns.add(btnDalail);
    }

    @Override
    public void enter() {
        refresh();
        if (ghosts.isEmpty() || Math.abs(lastGhostW - getWidth()) > 120) initGhosts();
    }

    @Override
    public void leave() {
        app.bank.save();
    }

    @Override
    protected void onWheel(int notches) {
        menuScroll += notches * 48;
    }

    private void refresh() {
        for (int i = 0; i < KEYS.length; i++) {
            int b = app.bank.bestOf(KEYS[i]);
            tiles[i].corner = b > 0 ? "BEST " + b : null;
        }
        btnSound.label = Sound.isOn() ? "SOUND ON" : "SOUND OFF";
        btnSound.color = Sound.isOn() ? Theme.VIOLET : Theme.DIM;
        btnFont.label = "FONT " + (int) Math.round(app.bank.questionScale * 100) + "%";
    }

    private void initGhosts() {
        lastGhostW = getWidth();
        ghosts.clear();
        if (app.bank.study.isEmpty()) return;
        for (int i = 0; i < 26; i++) {
            Ghost gh = new Ghost();
            gh.text = app.bank.study.get(rnd.nextInt(app.bank.study.size())).translit;
            gh.x = rnd.nextDouble() * 0.84;
            gh.y = rnd.nextDouble();
            gh.vy = 0.010 + rnd.nextDouble() * 0.026;
            gh.scale = 0.75 + rnd.nextDouble() * 1.0;
            gh.c = Theme.RAINBOW[rnd.nextInt(Theme.RAINBOW.length)];
            gh.alpha = 12 + rnd.nextInt(28);
            ghosts.add(gh);
        }
    }

    @Override
    protected void tick(double dt) {
        pulse += dt;
        for (int i = 0; i < ghosts.size(); i++) {
            Ghost gh = ghosts.get(i);
            gh.y -= gh.vy * dt;
            if (gh.y < -0.09) {
                gh.y = 1.09;
                gh.x = rnd.nextDouble() * 0.84;
                gh.text = app.bank.study.isEmpty()
                        ? gh.text
                        : app.bank.study.get(rnd.nextInt(app.bank.study.size())).translit;
            }
        }
    }

    private Color cycle() {
        double t = pulse * 0.65;
        int i0 = ((int) t) % Theme.RAINBOW.length;
        int i1 = (i0 + 1) % Theme.RAINBOW.length;
        return Theme.mix(Theme.RAINBOW[i0], Theme.RAINBOW[i1], t - Math.floor(t));
    }

    @Override
    protected void render(Graphics2D g, int w, int h) {
        for (int i = 0; i < ghosts.size(); i++) {
            Ghost gh = ghosts.get(i);
            int fs = (int) (17 * gh.scale);
            g.setFont(Theme.bold(fs));
            g.setColor(Theme.a(gh.c, gh.alpha));
            g.drawString(gh.text, (float) (gh.x * w), (float) (gh.y * h));
        }

        int ts = Theme.fitDisplay(g, "MEMORIBLITZ", (int) (w * 0.80), (int) (h * 0.115), 26);
        Theme.glowCenter(g, "MEMORIBLITZ", Theme.display(ts), cycle(), w / 2.0, h * 0.115);

        Theme.center(g, "Full Qur'an  ·  6236 ayahs  ·  memorise it at speed",
                Theme.bold(14), Theme.a(Theme.DIM, 235), w / 2.0, h * 0.115 + ts * 0.62);

        /* ---------- game tiles (3 cols, scrollable) ---------- */
        double gridW = Math.min(1120, w - 110);
        double gridX = (w - gridW) / 2.0;
        double gap = 12;
        int cols = 3;
        double tw = (gridW - gap * (cols - 1)) / cols;
        double th = Math.min(56, h * 0.08);
        double ty0 = h * 0.205;
        int rows = (tiles.length + cols - 1) / cols;
        gridTotalH = rows * th + (rows - 1) * gap;
        gridViewH = h - ty0 - 150;
        double maxScroll = Math.max(0, gridTotalH - gridViewH);
        if (menuScroll < 0) menuScroll = 0;
        if (menuScroll > maxScroll) menuScroll = maxScroll;

        for (int i = 0; i < tiles.length; i++) {
            int col = i % cols;
            int row = i / cols;
            tiles[i].x = gridX + col * (tw + gap);
            tiles[i].y = ty0 + row * (th + gap) - menuScroll;
            tiles[i].w = tw;
            tiles[i].h = th;
            tiles[i].visible = tiles[i].y + th >= ty0 - 6 && tiles[i].y <= ty0 + gridViewH + 6;
        }

        if (maxScroll > 0) {
            Theme.center(g, "scroll for more  ·  " + (menuScroll > 0 ? "▲" : "▼"),
                    Theme.bold(10), Theme.a(Theme.DIM, 140), w / 2.0, ty0 + gridViewH + 16);
        }

        /* ---------- stats strip ---------- */
        double stripY = h - 112;
        String stats = app.bank.studyLabel + "  -  " + app.bank.size() + " ayahs  -  "
                + app.bank.totalCorrect() + " correct recalls";
        Theme.center(g, stats, Theme.bold(13), Theme.a(Theme.DIM, 230), w / 2.0, stripY);

        Theme.center(g, "keys 1-9, 0 start a game   .   1-4 answer   .   SPACE keeps a card   .   ESC returns here",
                Theme.bold(11), Theme.a(Theme.DIM, 165), w / 2.0, stripY + 20);

        /* ---------- bottom buttons ---------- */
        double by = h - 62;
        double[] bw = { 160, 105, 105, 120, 115, 105, 105, 85 };
        double total = 0;
        for (double b : bw) total += b;
        total += (bw.length - 1) * 10;
        double bx = (w - total) / 2.0;
        Btn[] row = { btnSelect, btnRead, btnSound, btnReset, btnReminder, btnFont, btnBurdah, btnDalail };
        double cx = bx;
        for (int i = 0; i < row.length; i++) {
            row[i].x = cx;
            row[i].y = by;
            row[i].w = bw[i];
            cx += bw[i] + 10;
        }

        if (!app.bank.quran.isLoaded()) {
            Theme.center(g, "Could not load quran.data - check the res/ folder.",
                    Theme.bold(13), Theme.RED, w / 2.0, stripY + 48);
        }
    }
}
