package com.sefareshgir.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(25, 35, 25, 25);
        layout.setBackgroundColor(Color.rgb(245,247,250));

        TextView title = new TextView(this);
        title.setText("سفارش‌یار");
        title.setTextSize(30);
        title.setTextColor(Color.rgb(16,91,73));
        title.setGravity(Gravity.CENTER);

        layout.addView(title);

        addButton("📦 مدیریت کالاها", ProductActivity.class);
        addButton("👥 مدیریت مشتریان", CustomerActivity.class);
        addButton("🧾 ثبت سفارش", OrderActivity.class);
        addButton("💰 دریافت پرداخت", PaymentActivity.class);

        setContentView(layout);
    }

    private void addButton(String text, Class<?> target) {

        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(17);

        button.setOnClickListener(v -> {
            startActivity(new Intent(this, target));
        });

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                65
            );

        params.setMargins(0, 12, 0, 5);

        layout.addView(button, params);
    }
}
