/** Maps an ayah's English text to a thematic emoji for memorability. */
public final class Emoji {
    private Emoji() {}

    // emoji first, then keywords that trigger it
    private static final String[][] PAIRS = {
        {"🔥", "fire", "hell", "burn", "blaze", "flame", "torment", "scorch"},
        {"⛰️", "mountain", "mountains", "mount"},
        {"⚖️", "judgement", "judgment", "resurrection", "reckoning", "account", "scale", "balance"},
        {"🌴", "paradise", "garden", "gardens", "heaven", "jannah"},
        {"🌊", "sea", "ocean", "river", "rivers", "water", "flood", "wave"},
        {"☀️", "sun", "daylight", "day"},
        {"🌙", "moon", "night"},
        {"⭐", "star", "stars", "constellation"},
        {"🌍", "earth", "land", "world", "ground"},
        {"🌧️", "rain", "rainfall", "shower"},
        {"🌳", "tree", "trees", "olive", "fig", "palm"},
        {"🐦", "bird", "birds", "fowl"},
        {"🐪", "camel", "camels"},
        {"👥", "tribe", "tribes", "people", "nations", "mankind", "community"},
        {"📜", "prophet", "messenger", "messengers", "apostle", "apostles"},
        {"📖", "book", "quran", "scripture", "revelation", "scroll"},
        {"🕌", "prayer", "worship", "prostrate", "mosque", "fast", "fasting"},
        {"💚", "mercy", "merciful", "forgiv", "compassion"},
        {"💡", "light", "lamp", "illum"},
        {"🌑", "darkness", "dark", "shadow"},
        {"❤️", "heart", "hearts", "love"},
        {"✨", "gold", "silver", "wealth", "treasure", "riches"},
        {"💨", "wind", "winds", "storm", "gale"},
        {"☁️", "cloud", "clouds", "sky"},
        {"🐝", "bee", "bees", "honey"},
        {"🐜", "ant", "ants"},
        {"🐎", "horse", "horses", "steed"},
        {"🐄", "cattle", "cow", "cows", "sheep", "goat", "livestock"},
        {"🐟", "fish", "whale", "sea creature"},
        {"🕷️", "spider"},
        {"🐘", "elephant"},
        {"🐍", "snake", "serpent", "viper"},
        {"🦁", "lion"},
        {"🐺", "wolf"},
        {"👑", "king", "kingdom", "throne", "sovereign", "dominion"},
        {"⚔️", "war", "battle", "fight", "enemy", "strive", "struggle"},
        {"🕊️", "peace", "dove"},
        {"👼", "angel", "angels"},
        {"😈", "devil", "satan", "shaytan", "iblis", "evil"},
        {"⚰️", "death", "grave", "graves", "die", "perish"},
        {"🌱", "life", "grow", "seed", "sprout", "vegetation"},
        {"🍇", "food", "fruit", "fruits", "grain", "eat", "provision"},
        {"🍯", "honey"},
        {"🥛", "milk"},
        {"🍷", "wine", "drink", "cup"},
        {"🏠", "house", "home", "dwelling", "abode"},
        {"🏙️", "city", "town", "village"},
        {"⛵", "ship", "boat", "ark", "vessel"},
        {"🪨", "stone", "rock", "rocks", "boulder"},
        {"🏜️", "desert", "sand", "dust"},
        {"🌌", "heavens", "firmament", "universe"},
        {"⚡", "thunder", "lightning", "thunderbolt"},
        {"🌅", "dawn", "morning", "daybreak"},
        {"🌇", "evening", "sunset", "dusk"},
        {"🕋", "pilgrimage", "hajj", "kaaba", "sacred house"},
        {"🤲", "charity", "give", "poor", "orphan", "needy", "alms"},
    };

    /** Return the first matching emoji for the ayah's English text. */
    public static String forAyah(String english) {
        if (english == null) return "📖";
        String lower = english.toLowerCase();
        for (String[] p : PAIRS) {
            for (int i = 1; i < p.length; i++) {
                if (lower.contains(p[i])) return p[0];
            }
        }
        return "📖";
    }
}
