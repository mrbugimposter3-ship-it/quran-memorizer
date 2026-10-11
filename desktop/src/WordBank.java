import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * Holds the current study selection (a range of ayahs), per-ayah hit/miss
 * progress and best scores. Persists to one small text file.
 */
public class WordBank {

    public final QuranData quran = new QuranData();

    /** The currently selected ayahs to practise. */
    public final List<QuranData.Ayah> study = new ArrayList<QuranData.Ayah>();
    public String studyLabel = "No selection";

    public final Map<String, Integer> best = new HashMap<String, Integer>();
    /** global ayah index -> { hits, misses } */
    private final Map<Integer, int[]> progress = new HashMap<Integer, int[]>();

    public boolean soundOn = true;
    /** Seconds between reminder popups (Fatiha + ayah-of-the-day); 0 = off. */
    public int popupInterval = 30;
    /** Global multiplier for question text size (0.7 .. 1.6). */
    public double questionScale = 1.0;

    private final File file;
    private boolean dirty;

    public WordBank() {
        File dir = new File(System.getProperty("user.home"), ".quranmemorizer");
        file = new File(dir, "progress.txt");
        load();
    }

    /* ------------------------------------------------------------------ */
    /* selection                                                            */
    /* ------------------------------------------------------------------ */

    public void setStudy(List<QuranData.Ayah> list, String label) {
        study.clear();
        study.addAll(list);
        studyLabel = label;
        dirty = true;
    }

    public boolean isEmpty() { return study.isEmpty(); }
    public int size() { return study.size(); }

    /* ------------------------------------------------------------------ */
    /* picking / distractors                                                */
    /* ------------------------------------------------------------------ */

    public QuranData.Ayah randomAyah(Random r) {
        if (study.isEmpty()) return null;
        return study.get(r.nextInt(study.size()));
    }

    /** Weighted toward ayahs with more misses (adaptive). */
    public QuranData.Ayah adaptiveAyah(Random r) {
        if (study.isEmpty()) return null;
        double total = 0;
        for (QuranData.Ayah a : study) total += weight(a);
        double t = r.nextDouble() * total;
        for (QuranData.Ayah a : study) {
            t -= weight(a);
            if (t <= 0) return a;
        }
        return study.get(study.size() - 1);
    }

    private double weight(QuranData.Ayah a) {
        int[] p = progress.get(Integer.valueOf(a.global));
        if (p == null) return 1.0;
        double w = 1.0 + p[1] * 2.2 - Math.min(p[0], 6) * 0.28;
        return w < 0.35 ? 0.35 : w;
    }

    public List<String> distractorMeanings(String answer, int n, Random r) {
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : study) {
            String m = a.english;
            if (m == null || m.trim().length() == 0) continue;
            if (!pool.contains(m) && !m.equalsIgnoreCase(answer)) pool.add(m);
        }
        Collections.shuffle(pool, r);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    public List<String> distractorTranslits(String answer, int n, Random r) {
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : study) {
            String m = a.translit;
            if (m == null || m.trim().length() == 0) continue;
            if (!pool.contains(m) && !m.equalsIgnoreCase(answer)) pool.add(m);
        }
        Collections.shuffle(pool, r);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    public List<String> distractorUrdu(String answer, int n, Random r) {
        List<String> pool = new ArrayList<String>();
        for (QuranData.Ayah a : study) {
            String m = a.urdu;
            if (m == null || m.trim().length() == 0) continue;
            if (!pool.contains(m) && !m.equalsIgnoreCase(answer)) pool.add(m);
        }
        Collections.shuffle(pool, r);
        List<String> out = new ArrayList<String>();
        for (int i = 0; i < pool.size() && out.size() < n; i++) out.add(pool.get(i));
        return out;
    }

    /** A contiguous fragment of a text (about half its words), for partial-ayah questions. */
    public String partial(String text, Random r) {
        if (text == null) return "";
        String[] w = text.split("\\s+");
        if (w.length <= 3) return text;
        int len = Math.max(2, w.length / 2);
        int start = r.nextInt(w.length - len + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = start; i < start + len; i++) {
            if (i > start) sb.append(' ');
            sb.append(w[i]);
        }
        return sb.toString();
    }

    /* ------------------------------------------------------------------ */
    /* progress                                                             */
    /* ------------------------------------------------------------------ */

    public int[] progressOf(QuranData.Ayah a) {
        int[] p = progress.get(Integer.valueOf(a.global));
        return p == null ? new int[] { 0, 0 } : p;
    }

    public void record(QuranData.Ayah a, boolean hit) {
        if (a == null) return;
        int[] p = progress.get(Integer.valueOf(a.global));
        if (p == null) {
            p = new int[] { 0, 0 };
            progress.put(Integer.valueOf(a.global), p);
        }
        if (hit) p[0]++; else p[1]++;
        dirty = true;
    }

    public int totalCorrect() {
        int t = 0;
        for (int[] p : progress.values()) t += p[0];
        return t;
    }

    public int shakyCount() {
        int t = 0;
        for (QuranData.Ayah a : study) {
            int[] p = progressOf(a);
            if (p[1] > p[0]) t++;
        }
        return t;
    }

    public void resetStats() {
        progress.clear();
        dirty = true;
        save();
    }

    /* ------------------------------------------------------------------ */
    /* scores                                                               */
    /* ------------------------------------------------------------------ */

    public int bestOf(String game) {
        Integer v = best.get(game);
        return v == null ? 0 : v.intValue();
    }

    public boolean submitScore(String game, int score) {
        if (score > bestOf(game)) {
            best.put(game, Integer.valueOf(score));
            dirty = true;
            return true;
        }
        return false;
    }

    public void markDirty() { dirty = true; }

    /* ------------------------------------------------------------------ */
    /* persistence                                                          */
    /* ------------------------------------------------------------------ */

    public void save() {
        if (!dirty) return;
        dirty = false;
        PrintWriter pw = null;
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            pw.println("# QuranMemorizer progress");
            pw.println("S|sound|" + (soundOn ? "1" : "0"));
            pw.println("R|interval|" + popupInterval);
            pw.println("Q|qscale|" + questionScale);
            pw.println("M|study|" + studyLabel.replace('|', ' '));
            for (Map.Entry<String, Integer> e : best.entrySet()) {
                pw.println("B|" + e.getKey() + "|" + e.getValue().intValue());
            }
            for (Map.Entry<Integer, int[]> e : progress.entrySet()) {
                int[] p = e.getValue();
                if (p[0] == 0 && p[1] == 0) continue;
                pw.println("P|" + e.getKey().intValue() + "|" + p[0] + "|" + p[1]);
            }
        } catch (Throwable t) {
            /* read-only home directory - run without persistence */
        } finally {
            if (pw != null) pw.close();
        }
    }

    private void load() {
        if (file.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0 || line.charAt(0) == '#') continue;
                    String[] p = line.split("\\|", -1);
                    if (p.length >= 3 && p[0].equals("S")) {
                        soundOn = "1".equals(p[2]);
                    } else if (p.length >= 3 && p[0].equals("R")) {
                        try { popupInterval = Integer.parseInt(p[2].trim()); } catch (Throwable ig) {}
                    } else if (p.length >= 3 && p[0].equals("Q")) {
                        try { questionScale = Double.parseDouble(p[2].trim()); } catch (Throwable ig) {}
                    } else if (p.length >= 3 && p[0].equals("B")) {
                        best.put(p[1], Integer.valueOf(Integer.parseInt(p[2].trim())));
                    } else if (p.length >= 4 && p[0].equals("P")) {
                        int g = Integer.parseInt(p[1].trim());
                        int h = Integer.parseInt(p[2].trim());
                        int m = Integer.parseInt(p[3].trim());
                        progress.put(Integer.valueOf(g), new int[] { h, m });
                    }
                }
            } catch (Throwable t) {
                best.clear();
                progress.clear();
            } finally {
                if (br != null) try { br.close(); } catch (Throwable ig) {}
            }
        }
        // Default selection: Juz 30 (Amma) - short surahs, the classic starting point.
        if (study.isEmpty() && quran.isLoaded()) {
            setStudy(quran.ayahsForJuz(30), "Juz 30 (Amma)");
            dirty = false;
        }
    }
}
