package com.memoriblitz.quran;

/**
 * Mnemonic peg system: maps digits 0-9 to famous books / chapters so that a
 * number can be recalled as a short "story". Mirrors the desktop Mnemonic.
 */
public final class Mnemonic {

    private Mnemonic() {}

    /** Digit -> famous book short name (the "peg"). */
    public static final String[] PEG = {
        "Hobbit",        // 0
        "Fellowship",    // 1
        "Two Towers",    // 2
        "Return King",   // 3
        "Sign of Four",  // 4
        "Mockingbird",   // 5
        "Gatsby",        // 6
        "Lord of Flies", // 7
        "Geronimo",      // 8
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
