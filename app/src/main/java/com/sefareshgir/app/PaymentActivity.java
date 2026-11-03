package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

import android.database.Cursor;

public class PaymentActivity extends Activity {

    private Database db;
    private LinearLayout listLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        showScreen();
        loadOrders();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (listLayout != null) {
            loadOrders();
        }
    }

    private void showScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        root.setBackgroundColor(Color.rgb(246, 248, 247));

        TextView title = new TextView(this);
        title.setText("دریافت پرداخت‌ها");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(7, 91, 67));
        title.setGravity(Gravity.CENTER);
        title.setPadding(5, 15, 5, 20);

        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("سفارش‌های دارای مانده بدهی");
        subtitle.setTextSize(16);
        subtitle.setPadding(5, 5, 5, 15);

        root.addView(subtitle);

        ScrollView scroll = new ScrollView(this);

        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(listLayout);
        root.addView(scroll);

        setContentView(root);
    }

    private void loadOrders() {

        listLayout.removeAllViews();

        Cursor cursor = db.orders();

        long totalDebt = 0;
        int debtCount = 0;

        while (cursor.moveToNext()) {

            long orderId = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
            );

            long total = cursor.getLong(
                cursor.getColumnIndexOrThrow("total")
            );

            long paid = cursor.getLong(
                cursor.getColumnIndexOrThrow("paid")
            );

            String customer = cursor.getString(
                cursor.getColumnIndexOrThrow("customer_name")
            );

            if (customer == null || customer.isEmpty()) {
                customer = "مشتری متفرقه";
            }

            long remaining = total - paid;

            if (remaining <= 0) {
                continue;
            }

            totalDebt += remaining;
            debtCount++;

            addOrderCard(
                orderId,
                customer,
                total,
                paid,
                remaining
            );
        }

        cursor.close();

        TextView summary = new TextView(this);

        summary.setText(
            "تعداد سفارش‌های بدهکار: " + debtCount +
            "\nمجموع بدهی: " + totalDebt + " ریال"
        );

        summary.setTextSize(17);
        summary.setTextColor(Color.rgb(7, 91, 67));
        summary.setPadding(10, 15, 10, 20);

        listLayout.addView(summary);

        if (debtCount == 0) {

            TextView empty = new TextView(this);
            empty.setText("هیچ سفارش بدهکاری وجود ندارد.");
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10, 30, 10, 30);

            listLayout.addView(empty);
        }
    }

    private void addOrderCard(
            long orderId,
            String customer,
            long total,
            long paid,
            long remaining) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(15, 15, 15, 15);
        card.setBackgroundColor(Color.WHITE);

        LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );

        params.setMargins(0, 5, 0, 12);

        TextView details = new TextView(this);

        details.setText(
            "شماره سفارش: " + orderId +
            "\nمشتری: " + customer +
            "\nمبلغ کل: " + total + " ریال" +
            "\nپرداخت‌شده: " + paid + " ریال" +
            "\nمانده بدهی: " + remaining + " ریال"
        );

        details.setTextSize(15);
        details.setTextColor(Color.DKGRAY);

        card.addView(details);

        Button paymentButton = new Button(this);
        paymentButton.setText("ثبت پرداخت جدید");
        paymentButton.setAllCaps(false);

        paymentButton.setOnClickListener(v ->
            showPaymentDialog(orderId, remaining)
        );

        card.addView(paymentButton);

        listLayout.addView(card, params);
    }

    private void showPaymentDialog(
            long orderId,
            long remaining) {

        EditText amountField = new EditText(this);

        amountField.setHint("مبلغ دریافتی به ریال");
        amountField.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
        );

        new AlertDialog.Builder(this)
            .setTitle("دریافت وجه سفارش " + orderId)
            .setMessage(
                "مانده بدهی: " + remaining + " ریال"
            )
            .setView(amountField)
            .setPositiveButton("ثبت پرداخت", null)
            .setNegativeButton("انصراف", null)
            .create()
            .show();

        // دکمه ثبت با اعتبارسنجی در ادامه متد مدیریت می‌شود.
        showValidatedPaymentDialog(orderId, remaining);
    }

    private void showValidatedPaymentDialog(
            long orderId,
            long remaining) {

        EditText amountField = new EditText(this);

        amountField.setHint("مبلغ دریافتی به ریال");
        amountField.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
        );

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("ثبت پرداخت سفارش " + orderId)
            .setMessage(
                "مانده بدهی: " + remaining + " ریال"
            )
            .setView(amountField)
            .setPositiveButton("ثبت", null)
            .setNegativeButton("انصراف", null)
            .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    String value = amountField
                        .getText()
                        .toString()
                        .trim();

                    if (value.isEmpty()) {
                        amountField.setError("مبلغ را وارد کن");
                        return;
                    }

                    try {

                        long amount = Long.parseLong(value);

                        if (amount <= 0 || amount > remaining) {

                            amountField.setError(
                                "مبلغ باید بیشتر از صفر و حداکثر برابر بدهی باشد."
                            );

                            return;
                        }

                        db.addPayment(orderId, amount);

                        Toast.makeText(
                            this,
                            "پرداخت با موفقیت ثبت شد.",
                            Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();
                        loadOrders();

                    } catch (NumberFormatException e) {

                        amountField.setError("مبلغ معتبر وارد کن");
                    }
                });
        });

        dialog.show();
    }
          }
