package com.memoriblitz.quran;

import android.content.Context;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The full Qur'an, loaded once from the bundled quran.data asset.
 * Mirrors the desktop QuranData: 6236 ayahs, 114 surahs, 30 juz, 604 pages.
 */
public class QuranData {

    public static class Surah {
        public int number;
        public String arabic;
        public String english;
        public String translit;
        public int ayahCount;
        public String type;
    }

    public static class Ayah {
        public int global;
        public int surah;
        public int ayah;
        public int juz;
        public int page;
        public String arabic;
        public String translit;
        public String english;
        public String urdu;

        public String ref() { return surah + ":" + ayah; }

        public String fullRef() {
            return "Surah " + surah + " : " + ayah + "  ·  Juz " + juz + "  ·  Page " + page;
        }
    }

    public final List<Surah> surahs = new ArrayList<>();
    public final List<Ayah> ayahs = new ArrayList<>();
    public final int[] juzStart = new int[31];
    public final int[] pageStart = new int[605];

    private boolean loaded;

    private static QuranData instance;

    public static synchronized QuranData get(Context ctx) {
        if (instance == null) instance = new QuranData(ctx);
        return instance;
    }

    private QuranData(Context ctx) {
        load(ctx);
    }

    private void load(Context ctx) {
        try {
            InputStream in = ctx.getAssets().open("quran.data");
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            List<String[]> sRecs = new ArrayList<>();
            List<String[]> aRecs = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.length() == 0 || line.charAt(0) == '#') continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                if (p[0].equals("S")) sRecs.add(p);
                else if (p[0].equals("J")) {
                    juzStart[Integer.parseInt(p[1])] = globalOf(Integer.parseInt(p[2]), Integer.parseInt(p[3]), sRecs);
                } else if (p[0].equals("P")) {
                    pageStart[Integer.parseInt(p[1])] = globalOf(Integer.parseInt(p[2]), Integer.parseInt(p[3]), sRecs);
                } else if (p[0].equals("A")) aRecs.add(p);
            }
            br.close();

            for (String[] p : sRecs) {
                Surah s = new Surah();
                s.number = Integer.parseInt(p[1]);
                s.arabic = p[2];
                s.english = p[3];
                s.translit = p[4];
                s.ayahCount = Integer.parseInt(p[5]);
                s.type = p[6];
                surahs.add(s);
            }
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

    public boolean isLoaded() { return loaded; }
    public int size() { return ayahs.size(); }

    public Surah surah(int n) { return (n < 1 || n > surahs.size()) ? null : surahs.get(n - 1); }

    public List<Ayah> ayahsForSurah(int n) {
        List<Ayah> out = new ArrayList<>();
        for (Ayah a : ayahs) if (a.surah == n) out.add(a);
        return out;
    }

    public List<Ayah> ayahsForJuz(int n) {
        List<Ayah> out = new ArrayList<>();
        for (Ayah a : ayahs) if (a.juz == n) out.add(a);
        return out;
    }

    public List<Ayah> ayahsForPage(int n) {
        List<Ayah> out = new ArrayList<>();
        for (Ayah a : ayahs) if (a.page == n) out.add(a);
        return out;
    }

    /** Resolve a selection spec ("surah:N" / "juz:N" / "page:N" / "all") to ayahs. */
    public List<Ayah> ayahsFor(String spec) {
        if (spec == null) return ayahsForJuz(30);
        if (spec.equals("all")) return new ArrayList<>(ayahs);
        try {
            int i = spec.indexOf(':');
            String kind = spec.substring(0, i);
            int n = Integer.parseInt(spec.substring(i + 1));
            if (kind.equals("surah")) return ayahsForSurah(n);
            if (kind.equals("juz")) return ayahsForJuz(n);
            if (kind.equals("page")) return ayahsForPage(n);
        } catch (Throwable ig) {}
        return ayahsForJuz(30);
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
