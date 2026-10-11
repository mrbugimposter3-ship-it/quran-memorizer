package com.memoriblitz.quran;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Reader for a supplementary text (Qaseeda Burdah, Dalail al-Khayrat, ...). */
public class SupplementaryActivity extends AppCompatActivity {

    private static class Item {
        String kind, arabic, translit, english;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String file = getIntent().getStringExtra("file");
        if (file == null) file = "burdah.data";

        List<Item> items = load(file);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(18);
        root.setPadding(pad, dp(18), pad, dp(18));

        for (Item it : items) {
            if (it.kind.equals("T")) {
                root.addView(Ui.title(this, it.arabic, 24, R.color.emerald));
                if (it.translit != null && !it.translit.isEmpty()) {
                    root.addView(Ui.title(this, it.translit, 14, R.color.text_secondary));
                }
                root.addView(Ui.vspace(this, 16));
            } else if (it.kind.equals("C")) {
                root.addView(Ui.title(this, it.arabic, 16, R.color.cream));
                root.addView(Ui.vspace(this, 10));
            } else if (it.kind.equals("V")) {
                LinearLayout box = new LinearLayout(this);
                box.setOrientation(LinearLayout.VERTICAL);
                box.setPadding(dp(14), dp(12), dp(14), dp(12));
                box.setBackground(Ui.pill(this, R.color.card, 14));
                TextView ar = Ui.label(this, it.arabic, 22, R.color.cream);
                ar.setGravity(Gravity.CENTER);
                ar.setTextDirection(View.TEXT_DIRECTION_RTL);
                box.addView(ar);
                box.addView(Ui.vspace(this, 6));
                box.addView(Ui.label(this, it.translit, 15, R.color.text_primary));
                box.addView(Ui.vspace(this, 4));
                box.addView(Ui.label(this, it.english, 14, R.color.text_secondary));
                root.addView(box);
                root.addView(Ui.vspace(this, 10));
            }
        }

        scroll.addView(root);
        setContentView(scroll);
    }

    private List<Item> load(String file) {
        List<Item> out = new ArrayList<>();
        try {
            InputStream in = getAssets().open(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 2) continue;
                Item it = new Item();
                it.kind = p[0];
                if (p[0].equals("T")) { it.arabic = p[1]; it.translit = p.length > 2 ? p[2] : ""; }
                else if (p[0].equals("C")) { it.arabic = p[1]; }
                else if (p[0].equals("V")) { it.arabic = p[1]; it.translit = p.length > 2 ? p[2] : ""; it.english = p.length > 3 ? p[3] : ""; }
                out.add(it);
            }
            br.close();
        } catch (Throwable t) {}
        return out;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
