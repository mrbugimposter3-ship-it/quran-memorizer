package com.memoriblitz.quran;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;

import com.memoriblitz.quran.QuranData.Ayah;

/**
 * A single global reminder scheduler shared by every screen. The popup shows
 * Surah Al-Fatiha plus the ayah of the current prayer period, and never stacks
 * (if one is already open, a new one is skipped until it is dismissed).
 */
public final class Reminder {

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static boolean showing = false;
    private static boolean scheduled = false;

    private Reminder() {}

    /** (Re)start the reminder loop from an Activity (menu, reader, or a game). */
    public static void schedule(final Activity act) {
        handler.removeCallbacksAndMessages(null);
        scheduled = false;
        ProgressStore store = ProgressStore.get(act);
        int sec = store.popupInterval();
        if (sec <= 0) return;
        scheduled = true;
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                scheduled = false;
                show(act);
                schedule(act);
            }
        }, sec * 1000L);
    }

    public static void show(Activity act) {
        if (act == null || act.isFinishing()) return;
        ProgressStore store = ProgressStore.get(act);
        if (store.popupInterval() <= 0) return;
        if (showing) return;   // never stack

        QuranData quran = QuranData.get(act);
        StringBuilder sb = new StringBuilder();
        sb.append("Surah Al-Fatiha:\n");
        for (Ayah a : quran.fatiha()) sb.append(a.arabic).append("\n");
        Ayah day = quran.ayahForPrayer();
        if (day != null) {
            sb.append("\nAyah of this prayer ").append(day.fullRef()).append(":\n")
              .append(day.arabic).append("\n").append(day.translit).append("\n").append(day.english);
            if (day.urdu != null) sb.append("\n").append(day.urdu);
        }

        showing = true;
        new AlertDialog.Builder(act)
                .setTitle("Reminder")
                .setMessage(sb.toString())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface d, int w) { showing = false; }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override public void onCancel(DialogInterface d) { showing = false; }
                })
                .show();
    }
}
