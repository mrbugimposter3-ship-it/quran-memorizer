import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;

/** App lock: passcode is 6463 or 25524, re-chosen every 60 minutes. */
public final class Passcode {
    private Passcode() {}

    private static final File FILE = new File(
            System.getProperty("user.home"), ".quranmemorizer/passcode.txt");

    private static String code;
    private static long time;

    public static synchronized String current() {
        load();
        long now = System.currentTimeMillis();
        if (code == null || now - time > 60 * 60 * 1000L) {
            code = new Random().nextBoolean() ? "6463" : "25524";
            time = now;
            save();
        }
        return code;
    }

    private static void load() {
        if (!FILE.exists()) return;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(FILE), "UTF-8"));
            String line = br.readLine();
            br.close();
            if (line != null) {
                String[] p = line.split("\\|");
                if (p.length == 2) {
                    code = p[0];
                    time = Long.parseLong(p[1].trim());
                }
            }
        } catch (Throwable ignored) {}
    }

    private static void save() {
        try {
            FILE.getParentFile().mkdirs();
            PrintWriter pw = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(FILE), "UTF-8"));
            pw.println(code + "|" + time);
            pw.close();
        } catch (Throwable ignored) {}
    }
}
