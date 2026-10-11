import java.util.ArrayList;
import java.util.List;

/** ODD ONE OUT: three ayahs share a surah, one is from elsewhere - pick it. */
public class OddOneGame extends QuizGame {

    public OddOneGame(MemoriBlitz app) {
        super(app);
        key = "oddone";
        title = "ODD ONE OUT";
        blurb = "Three ayahs share a surah - pick the odd one out.";
        accent = Theme.VIOLET;
    }

    @Override protected void nextQuestion() {
        if (app.bank.isEmpty()) { finish(); return; }
        List<Integer> surahs = new ArrayList<Integer>();
        for (QuranData.Ayah a : app.bank.study)
            if (!surahs.contains(Integer.valueOf(a.surah))) surahs.add(Integer.valueOf(a.surah));
        int fam = -1;
        for (int i = 0; i < surahs.size(); i++) {
            if (countIn(surahs.get(i).intValue()) >= 3) { fam = surahs.get(i).intValue(); break; }
        }
        if (fam < 0) { finish(); return; }
        List<QuranData.Ayah> family = new ArrayList<QuranData.Ayah>();
        for (QuranData.Ayah a : app.bank.study) if (a.surah == fam && family.size() < 3) family.add(a);
        List<QuranData.Ayah> others = new ArrayList<QuranData.Ayah>();
        for (QuranData.Ayah a : app.bank.study) if (a.surah != fam && others.size() < 6) others.add(a);
        if (family.size() < 3 || others.isEmpty()) { finish(); return; }
        QuranData.Ayah odd = others.get(rnd.nextInt(others.size()));
        target = odd;
        prompt = "Pick the ayah NOT from " + app.bank.quran.surah(fam).english + ".";
        List<String> wrong = new ArrayList<String>();
        for (QuranData.Ayah a : family) wrong.add(a.english);
        setOptions(odd.english, wrong);
    }

    private int countIn(int surah) {
        int c = 0;
        for (QuranData.Ayah a : app.bank.study) if (a.surah == surah) c++;
        return c;
    }
}
