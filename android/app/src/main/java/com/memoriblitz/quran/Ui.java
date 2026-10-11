package com.memoriblitz.quran;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/** Small helpers for the green + coffee look. */
public final class Ui {
    private Ui() {}

    public static int color(Context c, int id) {
        return ContextCompat.getColor(c, id);
    }

    /** A rounded, tinted background drawable. */
    public static GradientDrawable pill(Context c, int colorId, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color(c, colorId));
        float r = radiusDp * c.getResources().getDisplayMetrics().density;
        d.setCornerRadius(r);
        return d;
    }

    public static GradientDrawable outline(Context c, int colorId, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(Color.TRANSPARENT);
        d.setStroke((int) (2 * c.getResources().getDisplayMetrics().density), color(c, colorId));
        float r = radiusDp * c.getResources().getDisplayMetrics().density;
        d.setCornerRadius(r);
        return d;
    }

    public static Button button(Context c, String text, int colorId, View.OnClickListener l) {
        Button b = new Button(c);
        b.setText(text);
        b.setTextColor(color(c, R.color.espresso));
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setBackground(pill(c, colorId, 24));
        b.setOnClickListener(l);
        return b;
    }

    public static Button ghost(Context c, String text, int colorId, View.OnClickListener l) {
        Button b = new Button(c);
        b.setText(text);
        b.setTextColor(color(c, colorId));
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setBackground(outline(c, colorId, 24));
        b.setOnClickListener(l);
        return b;
    }

    public static TextView title(Context c, String text, int sizeSp, int colorId) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(sizeSp);
        t.setTextColor(color(c, colorId));
        t.setGravity(android.view.Gravity.CENTER);
        return t;
    }

    public static TextView label(Context c, String text, int sizeSp, int colorId) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(sizeSp);
        t.setTextColor(color(c, colorId));
        return t;
    }

    public static int dp(Context c, int v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    /** Vertical spacer (height in dp). */
    public static View vspace(Context c, int dp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(c, dp)));
        return v;
    }

    /** Horizontal spacer (width in dp). */
    public static View hspace(Context c, int dp) {
        View v = new View(c);
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(c, dp), 1));
        return v;
    }

    /** Small "QURAN" peek button that opens the full-Quran reader from a game. */
    public static Button quranPeek(final AppCompatActivity act) {
        Button b = ghost(act, "QURAN", R.color.emerald, new View.OnClickListener() {
            @Override public void onClick(View v) {
                act.startActivity(new Intent(act, ReaderActivity.class));
            }
        });
        b.setTextSize(13);
        return b;
    }
}
