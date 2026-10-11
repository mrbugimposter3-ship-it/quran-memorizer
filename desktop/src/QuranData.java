import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The full Qur'an, loaded once from the bundled quran.data resource.
 *
 * quran.data is a plain pipe-delimited text file:
 *   S|number|arabic|english|transliteration|ayah_count|type
 *   J|juz|sura|ayah
 *   P|page|sura|ayah
 *   A|sura|ayah_in_surah|juz|page|transliteration|english
 *
 * 6236 ayahs across 114 surahs, 30 juz and 604 pages (Madani mushaf).
 */
public class QuranData {

    public static class Surah {
        public int number;
        public String arabic;
        public String english;
        public String translit;
        public int ayahCount;
        public String type;
        public int globalStart;   // 0-based index of its first ayah

        public String label() {
            return number + ". " + english + " (" + translit + ")";
        }
    }

    public static class Ayah {
        public int global;      // 0-based
        public int surah;       // 1-based
        public int ayah;        // 1-based within surah
        public int juz;         // 1-based
        public int page;        // 1-based
        public String arabic;
        public String translit;
        public String english;
        public String urdu;

        public String ref() {
            return surah + ":" + ayah;
        }

        public String fullRef() {
            return "Surah " + surah + " : " + ayah + "  ·  Juz " + juz + "  ·  Page " + page;
        }
    }

    public final List<Surah> surahs = new ArrayList<Surah>();
    public final List<Ayah> ayahs = new ArrayList<Ayah>();

    /** global (0-based) index where each juz starts */
    public final int[] juzStart = new int[31];
    /** global (0-based) index where each page starts */
    public final int[] pageStart = new int[605];

    private boolean loaded;

    public QuranData() {
        load();
    }

    /* ------------------------------------------------------------------ */
    /* loading                                                              */
    /* ------------------------------------------------------------------ */

    private void load() {
        BufferedReader br = open();
        if (br == null) { loaded = false; return; }

        try {
            // First pass: surah metadata (needed to compute global offsets)
            List<String[]> sRecs = new ArrayList<String[]>();
            List<String[]> aRecs = new ArrayList<String[]>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                if (p[0].equals("S")) sRecs.add(p);
                else if (p[0].equals("J")) {
                    int j = Integer.parseInt(p[1]);
                    int sura = Integer.parseInt(p[2]);
                    int aya = Integer.parseInt(p[3]);
                    juzStart[j] = globalOf(sura, aya, sRecs);
                } else if (p[0].equals("P")) {
                    int pg = Integer.parseInt(p[1]);
                    int sura = Integer.parseInt(p[2]);
                    int aya = Integer.parseInt(p[3]);
                    pageStart[pg] = globalOf(sura, aya, sRecs);
                } else if (p[0].equals("A")) aRecs.add(p);
            }

            // Build surahs
            int cum = 0;
            for (String[] p : sRecs) {
                Surah s = new Surah();
                s.number = Integer.parseInt(p[1]);
                s.arabic = p[2];
                s.english = p[3];
                s.translit = p[4];
                s.ayahCount = Integer.parseInt(p[5]);
                s.type = p[6];
                s.globalStart = cum;
                surahs.add(s);
                cum += s.ayahCount;
            }

            // Build ayahs
            for (String[] p : aRecs) {
                Ayah a = new Ayah();
                a.surah = Integer.parseInt(p[1]);
                a.ayah = Integer.parseInt(p[2]);
                a.juz = Integer.parseInt(p[3]);
                a.page = Integer.parseInt(p[4]);
                a.arabic = p[5];
                a.translit = p[6];
                a.english = p[7];
                a.urdu = p.length > 8 ? p[8] : null;
                a.global = globalOf(a.surah, a.ayah, sRecs);
                ayahs.add(a);
            }

            loaded = !ayahs.isEmpty();
        } catch (Throwable t) {
            loaded = false;
        } finally {
            try { br.close(); } catch (Throwable ig) {}
        }
    }

    private int globalOf(int surah, int aya, List<String[]> sRecs) {
        int cum = 0;
        for (String[] p : sRecs) {
            int num = Integer.parseInt(p[1]);
            int count = Integer.parseInt(p[5]);
            if (num == surah) return cum + (aya - 1);
            cum += count;
        }
        return cum + (aya - 1);
    }

    private BufferedReader open() {
        try {
            InputStream in = QuranData.class.getResourceAsStream("/quran.data");
            if (in != null) return new BufferedReader(new InputStreamReader(in, "UTF-8"));
        } catch (Throwable ig) {}
        String[] candidates = { "res/quran.data", "quran.data", "res\\quran.data" };
        for (String c : candidates) {
            File f = new File(c);
            if (f.exists()) {
                try {
                    return new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
                } catch (Throwable ig) {}
            }
        }
        return null;
    }

    /* ------------------------------------------------------------------ */
    /* queries                                                              */
    /* ------------------------------------------------------------------ */

    public boolean isLoaded() { return loaded; }

    public int size() { return ayahs.size(); }

    public Surah surah(int n) {
        if (n < 1 || n > surahs.size()) return null;
        return surahs.get(n - 1);
    }

    /** Ayahs of a surah, in order. */
    public List<Ayah> ayahsForSurah(int n) {
        List<Ayah> out = new ArrayList<Ayah>();
        for (Ayah a : ayahs) if (a.surah == n) out.add(a);
        return out;
    }

    /** Ayahs of a juz, in order. */
    public List<Ayah> ayahsForJuz(int n) {
        List<Ayah> out = new ArrayList<Ayah>();
        for (Ayah a : ayahs) if (a.juz == n) out.add(a);
        return out;
    }

    /** Ayahs on a page, in order. */
    public List<Ayah> ayahsForPage(int n) {
        List<Ayah> out = new ArrayList<Ayah>();
        for (Ayah a : ayahs) if (a.page == n) out.add(a);
        return out;
    }

    /** A human label for a juz, e.g. "Juz 1". */
    public String juzLabel(int n) {
        if (n < 1 || n > 30) return "Juz " + n;
        Ayah first = ayahs.get(juzStart[n]);
        return "Juz " + n + "  ·  starts " + first.surah + ":" + first.ayah;
    }

    public String pageLabel(int n) {
        if (n < 1 || n > 604) return "Page " + n;
        Ayah first = ayahs.get(pageStart[n]);
        return "Page " + n + "  ·  starts " + first.surah + ":" + first.ayah;
    }

    /** The seven ayahs of Surah Al-Fatiha (for the periodic reminder popup). */
    public List<Ayah> fatiha() { return ayahsForSurah(1); }

    /** A deterministic ayah that changes at each prayer time (fajr..isha). */
    public Ayah ayahForPrayer() {
        if (ayahs.isEmpty()) return null;
        java.util.Calendar c = java.util.Calendar.getInstance();
        int period = PrayerTimes.currentPeriod();
        int seed = c.get(java.util.Calendar.YEAR) * 10000
                 + c.get(java.util.Calendar.DAY_OF_YEAR) * 10
                 + period;
        java.util.Random r = new java.util.Random(seed);
        return ayahs.get(r.nextInt(ayahs.size()));
    }
}
