import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.Font;
import java.awt.BorderLayout;

/**
 * QuranMemorizer (MemoriBlitz) - a fast, loud Qur'an memorisation trainer.
 *
 * The full Qur'an (6236 ayahs, 114 surahs, 30 juz, 604 pages) is bundled as
 * quran.data. Pick what to memorise by surah, juz or page, then drill it with
 * four game modes:
 *   TEST    ayah <-> meaning, against the clock
 *   MATCH   pair transliterations with their meanings
 *   FLASH   adaptive self-graded cards
 *   FILL    supply the missing word of the ayah
 *
 * Everything is drawn by hand on Graphics2D with a green + coffee theme.
 */
public class MemoriBlitz extends JFrame {

    public final WordBank bank = new WordBank();

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private final MenuScreen menu;
    private final QuranSelect select;
    private final Reader reader;
    private final SupplementaryReader burdah;
    private final SupplementaryReader dalail;
    private final GameScreen[] games;

    /** When non-null, the Reader was opened from this game and BACK returns here. */
    String readerReturnKey = null;

    /** Non-modal, self-dismissing reminder popup (never stacks). */
    private JDialog reminderDialog;
    private JTextArea reminderText;
    private Timer reminderCloseTimer;

    public MemoriBlitz() {
        super("MemoriBlitz - Qur'an memorisation trainer");

        Sound.setOn(bank.soundOn);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { shutdown(); }
        });

        root.setOpaque(true);
        root.setBackground(Theme.BG_A);

        menu = new MenuScreen(this);
        select = new QuranSelect(this);
        reader = new Reader(this);
        burdah = new SupplementaryReader(this, "burdah.data");
        dalail = new SupplementaryReader(this, "dalail.data");
        games = new GameScreen[] {
                new TestGame(this),
                new MatchGame(this),
                new FlashGame(this),
                new FillGame(this),
                new ReciteGame(this),
                new OrderGame(this),
                new NextGame(this),
                new ArabicGame(this),
                new ReverseGame(this),
                new PairGame(this),
                new InitialsGame(this),
                new SpeedGame(this),
                new GridGame(this),
                new FillArGame(this),
                new JumbleGame(this),
                new VsAiGame(this),
                new VerseBuildGame(this),
                new UrduGame(this),
                new RevealGame(this),
                new WordGame(this),
                new MixGame(this),
                new OddOneGame(this),
                new WordCountGame(this),
                new EdgeWordGame(this),
                new NextWordGame(this),
                new PartialGame(this),
                new En2UrGame(this),
                new Ur2EnGame(this),
                new Ur2ArGame(this),
                new Ar2TrGame(this),
                new SurahInfoGame(this),
                new WhereSurahGame(this),
                new WhereJuzGame(this),
                new WherePageGame(this),
                new WhereAyahGame(this),
                new StoryGame(this)
        };

        root.add(menu, "menu");
        root.add(select, "select");
        root.add(reader, "reader");
        root.add(burdah, "burdah");
        root.add(dalail, "dalail");
        for (int i = 0; i < games.length; i++) root.add(games[i], games[i].key);

        setContentPane(root);
        setSize(1200, 790);
        setMinimumSize(new Dimension(1020, 700));
        setLocationRelativeTo(null);

        cards.show(root, "menu");
        menu.enter();

        startReminder();
    }

    private void startReminder() {
        if (bank.popupInterval <= 0) return;
        Timer t = new Timer(bank.popupInterval * 1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) { showReminder(); }
        });
        t.setRepeats(true);
        t.start();
    }

    /** Periodic reminder: Surah Al-Fatiha + the ayah of the current prayer period. */
    public void showReminder() {
        if (bank.popupInterval <= 0) return;
        QuranData.Ayah day = bank.quran.ayahForPrayer();
        StringBuilder sb = new StringBuilder();
        sb.append("Surah Al-Fatiha:\n");
        for (QuranData.Ayah a : bank.quran.fatiha()) {
            sb.append(a.arabic).append("\n");
        }
        sb.append("\nAyah of this prayer ");
        if (day != null) {
            sb.append(day.fullRef()).append(":\n").append(day.arabic)
              .append("\n").append(day.translit).append("\n").append(day.english);
            if (day.urdu != null) sb.append("\n").append(day.urdu);
        }

        // Reuse a single non-modal dialog so reminders never stack or block a game.
        if (reminderDialog != null && reminderDialog.isVisible()) {
            reminderText.setText(sb.toString());
            restartReminderClose();
            return;
        }
        if (reminderDialog == null) {
            reminderDialog = new JDialog(this, "Reminder", false);
            reminderDialog.setAlwaysOnTop(true);
            reminderDialog.setLayout(new BorderLayout());
            reminderText = new JTextArea();
            reminderText.setEditable(false);
            reminderText.setLineWrap(true);
            reminderText.setWrapStyleWord(true);
            reminderText.setBackground(new Color(32, 24, 16));
            reminderText.setForeground(new Color(242, 232, 213));
            reminderText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
            reminderText.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
            JScrollPane sp = new JScrollPane(reminderText);
            sp.setBorder(null);
            reminderDialog.add(sp, BorderLayout.CENTER);
        }
        reminderText.setText(sb.toString());
        reminderDialog.pack();
        reminderDialog.setSize(420, Math.min(520, reminderDialog.getHeight()));
        reminderDialog.setLocation(
                getX() + getWidth() - 440,
                getY() + 70);
        reminderDialog.setVisible(true);
        restartReminderClose();
    }

    private void restartReminderClose() {
        if (reminderCloseTimer != null) reminderCloseTimer.stop();
        reminderCloseTimer = new Timer(9000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (reminderDialog != null) reminderDialog.setVisible(false);
            }
        });
        reminderCloseTimer.setRepeats(false);
        reminderCloseTimer.start();
    }

    /** Adjust the reminder popup interval (seconds; 0 disables). */
    public void adjustReminder() {
        String got = JOptionPane.showInputDialog(this,
                "Reminder popup interval in seconds (0 = off):", bank.popupInterval);
        if (got == null) return;
        try {
            int v = Integer.parseInt(got.trim());
            bank.popupInterval = Math.max(0, Math.min(3600, v));
            bank.markDirty();
            bank.save();
        } catch (Throwable ig) {}
    }

    public GameScreen gameByKey(String key) {
        for (int i = 0; i < games.length; i++) {
            if (games[i].key.equals(key)) return games[i];
        }
        return null;
    }

    public void goMenu() {
        bank.save();
        readerReturnKey = null;
        cards.show(root, "menu");
        menu.enter();
        focusLater(menu);
    }

    public void goSelect() {
        bank.save();
        readerReturnKey = null;
        cards.show(root, "select");
        select.enter();
        focusLater(select);
    }

    public void goReader() {
        bank.save();
        readerReturnKey = null;
        cards.show(root, "reader");
        reader.enter();
        focusLater(reader);
    }

    /** Open the full-Quran reader as a peek from within a game; BACK returns to the game. */
    public void openReaderFrom(String gameKey) {
        readerReturnKey = gameKey;
        cards.show(root, "reader");
        reader.enter();
        focusLater(reader);
    }

    public void goBurdah() {
        bank.save();
        readerReturnKey = null;
        cards.show(root, "burdah");
        burdah.enter();
        focusLater(burdah);
    }

    public void goDalail() {
        bank.save();
        readerReturnKey = null;
        cards.show(root, "dalail");
        dalail.enter();
        focusLater(dalail);
    }

    /** Return to a game without resetting it (used after a Quran peek). */
    public void resumeGame(String key) {
        readerReturnKey = null;
        GameScreen game = gameByKey(key);
        if (game == null) { goMenu(); return; }
        cards.show(root, key);
        focusLater(game);
    }

    public void startGame(String key) {
        GameScreen game = gameByKey(key);
        if (game == null) return;
        if (bank.isEmpty()) {
            goSelect();
            return;
        }
        bank.save();
        readerReturnKey = null;
        game.begin();
        cards.show(root, key);
        focusLater(game);
    }

    private void focusLater(final Screen s) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                s.requestFocusInWindow();
            }
        });
    }

    public void shutdown() {
        bank.save();
        dispose();
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable ignored) {
        }

        // passcode gate
        final String code = Passcode.current();
        while (true) {
            String got = JOptionPane.showInputDialog(null, "Enter passcode:",
                    "QuranMemorizer", JOptionPane.QUESTION_MESSAGE);
            if (got == null) { System.exit(0); return; }
            if (got.trim().equals(code)) break;
            JOptionPane.showMessageDialog(null, "Incorrect passcode",
                    "QuranMemorizer", JOptionPane.ERROR_MESSAGE);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MemoriBlitz app = new MemoriBlitz();
                app.setVisible(true);
                app.menu.requestFocusInWindow();
            }
        });
    }
}
