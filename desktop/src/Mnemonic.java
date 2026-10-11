/**
 * Mnemonic peg system: maps digits 0-9 to famous books / chapters so that a
 * number can be recalled as a short "story". Number story game encodes each
 * ayah's metadata (surah / ayah / juz / hizb / page / position-on-page) with
 * these book names for effortless, photographic recall.
 */
public final class Mnemonic {

    private Mnemonic() {}

    /** Digit -> famous book short name (the "peg"). */
    public static final String[] PEG = {
        "Hobbit",        // 0
        "Fellowship",    // 1  (LotR book one)
        "Two Towers",    // 2  (LotR book two)
        "Return King",   // 3  (LotR book three)
        "Sign of Four",  // 4  (Sherlock Holmes)
        "Mockingbird",   // 5  (To Kill a Mockingbird)
        "Gatsby",        // 6  (The Great Gatsby)
        "Lord of Flies", // 7  (Lord of the Flies)
        "Geronimo",      // 8  (Geronimo Stilton)
        "Dracula"        // 9
    };

    /** A number as a sequence of books, e.g. 42 -> "Sign of Four+Two Towers". */
    public static String story(int n) {
        String s = String.valueOf(n);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (i > 0) sb.append('+');
            sb.append(PEG[s.charAt(i) - '0']);
        }
        return sb.toString();
    }
}
