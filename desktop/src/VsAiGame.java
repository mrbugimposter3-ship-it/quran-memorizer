import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/** VS AI: race the clock on ayah metadata (surah / page / juz / ayah number). */
public class VsAiGame extends QuizGame {

    private double aiScore;

    public VsAiGame(MemoriBlitz app) {
        super(app);
        key = "vsai";
        title = "VS AI";
        blurb = "Race the clock on surah / page / juz / ayah number.";
        accent = Theme.RED;
    }

    @Override protected double startTime() { return 60; }
    @Override protected Font promptFont() { return Theme.arabic(26); }

    @Override protected void resetGame() {
        aiScore = 0;
        super.resetGame();
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        target = app.bank.randomAyah(rnd);
        if (target == null) { finish(); return; }
        int type = rnd.nextInt(4);
        List<String> wrong = new ArrayList<String>();
        String right;
        switch (type) {
            case 0: // surah
                right = app.bank.quran.surah(target.surah).english;
                while (wrong.size() < 3) {
                    int s = 1 + rnd.nextInt(app.bank.quran.surahs.size());
                    String n = app.bank.quran.surah(s).english;
                    if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
                }
                break;
            case 1: // page
                right = "Page " + target.page;
                while (wrong.size() < 3) {
                    int p = 1 + rnd.nextInt(604);
                    String n = "Page " + p;
                    if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
                }
                break;
            case 2: // juz
                right = "Juz " + target.juz;
                while (wrong.size() < 3) {
                    int j = 1 + rnd.nextInt(30);
                    String n = "Juz " + j;
                    if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
                }
                break;
            default: // ayah number
                right = "Ayah " + target.ayah;
                while (wrong.size() < 3) {
                    int a = 1 + rnd.nextInt(286);
                    String n = "Ayah " + a;
                    if (!n.equals(right) && !wrong.contains(n)) wrong.add(n);
                }
                break;
        }
        prompt = target.arabic;
        setOptions(right, wrong);
    }

    @Override protected void round(double dt) {
        aiScore += 15 * dt;
        super.round(dt);
    }

    @Override protected void paintGame(Graphics2D g, int w, int h) {
        super.paintGame(g, w, h);
        if (over || target == null) return;
        Theme.center(g, "AI  " + (int) aiScore, Theme.mono(20), Theme.RED, w / 2.0, h * 0.10);
    }

    @Override protected String adviceLine() {
        return score > (int) aiScore ? "You beat the AI!" : "The AI won this round.";
    }
}
