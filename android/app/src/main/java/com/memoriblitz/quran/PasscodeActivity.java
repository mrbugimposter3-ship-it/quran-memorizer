package com.memoriblitz.quran;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Lock screen: passcode is 6463 or 25524, re-chosen every 60 minutes. */
public class PasscodeActivity extends AppCompatActivity {

    private EditText input;
    private TextView msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        root.setPadding(pad, dp(60), pad, dp(24));

        TextView title = Ui.title(this, "QURAN MEMORIZER", 28, R.color.emerald);
        root.addView(title);
        root.addView(Ui.vspace(this, 8));
        TextView sub = Ui.title(this, "Enter passcode to continue", 14, R.color.text_secondary);
        root.addView(sub);
        root.addView(Ui.vspace(this, 28));

        input = new EditText(this);
        input.setHint("passcode");
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setTextSize(26);
        input.setTextColor(Ui.color(this, R.color.text_primary));
        input.setHintTextColor(Ui.color(this, R.color.text_secondary));
        input.setPadding(dp(14), dp(12), dp(14), dp(12));
        input.setBackground(Ui.outline(this, R.color.cream, 14));
        root.addView(input);
        root.addView(Ui.vspace(this, 16));

        Button enter = Ui.button(this, "ENTER", R.color.leaf, new View.OnClickListener() {
            @Override public void onClick(View v) { check(); }
        });
        root.addView(enter);
        root.addView(Ui.vspace(this, 14));

        msg = Ui.label(this, "", 14, R.color.warm_red);
        root.addView(msg);

        setContentView(root);
    }

    private void check() {
        String code = ProgressStore.get(this).passcode();
        String got = input.getText().toString().trim();
        if (got.equals(code)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            msg.setText("Incorrect passcode");
            input.setText("");
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
