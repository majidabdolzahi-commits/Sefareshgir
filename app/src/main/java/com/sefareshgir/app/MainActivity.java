package com.sefareshgir.app;
import android.content.Intent;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

public class MainActivity extends Activity {

    LinearLayout layout;

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

        addButton("📦 مدیریت کالاها");
        addButton("👥 مدیریت مشتریان");
        addButton("🧾 ثبت سفارش");
        addButton("💰 دریافت پرداخت");
        addButton("📊 گزارش فروش");
        addButton("⚙️ تنظیمات");

        setContentView(layout);
    }
    button.setOnClickListener(v -> {

    if (title.contains("کالاها")) {

        startActivity(
            new Intent(this, ProductActivity.class)
        );

    } else if (title.contains("مشتریان")) {

        startActivity(
            new Intent(this, CustomerActivity.class)
        );

    } else if (title.contains("ثبت سفارش")) {

        startActivity(
            new Intent(this, OrderActivity.class)
        );

    } else if (title.contains("دریافت پرداخت")) {

        startActivity(
            new Intent(this, PaymentActivity.class)
        );

    }

}
        startActivity(
            new Intent(this, ProductActivity.class)
        );

    } else if (title.contains("مشتریان")) {

        startActivity(
            new Intent(this, CustomerActivity.class)
        );

    } else if (title.contains("ثبت سفارش")) {

        startActivity(
            new Intent(this, OrderActivity.class)
        );

    }

});

    if (title.contains("کالاها")) {

        startActivity(
            new Intent(this, ProductActivity.class)
        );

    } else if (title.contains("مشتریان")) {

        startActivity(
            new Intent(this, CustomerActivity.class)
        );

    }

});
        startActivity(new Intent(this, ProductActivity.class));
    }
});
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(17);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 65);

        params.setMargins(0, 12, 0, 5);

        layout.addView(button, params);
    }
}
