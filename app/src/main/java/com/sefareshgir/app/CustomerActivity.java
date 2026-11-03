package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

import android.database.Cursor;

public class CustomerActivity extends Activity {

    private Database db;
    private LinearLayout listLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        showScreen();
        loadCustomers();
    }

    private void showScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        root.setBackgroundColor(Color.rgb(246, 248, 247));

        TextView title = new TextView(this);
        title.setText("مدیریت مشتریان");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(7, 91, 67));
        title.setGravity(Gravity.CENTER);
        title.setPadding(5, 15, 5, 20);

        root.addView(title);

        Button addButton = new Button(this);
        addButton.setText("＋ افزودن مشتری جدید");
        addButton.setAllCaps(false);
        addButton.setOnClickListener(v -> showAddCustomerDialog());

        root.addView(addButton);

        TextView listTitle = new TextView(this);
        listTitle.setText("فهرست مشتریان");
        listTitle.setTextSize(19);
        listTitle.setTextColor(Color.DKGRAY);
        listTitle.setPadding(5, 20, 5, 10);

        root.addView(listTitle);

        ScrollView scroll = new ScrollView(this);

        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(listLayout);
        root.addView(scroll);

        setContentView(root);
    }

    private void loadCustomers() {

        listLayout.removeAllViews();

        Cursor cursor = db.customers();

        if (cursor == null || cursor.getCount() == 0) {

            TextView empty = new TextView(this);
            empty.setText("هنوز مشتری ثبت نشده است.");
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10, 30, 10, 30);

            listLayout.addView(empty);

            if (cursor != null) cursor.close();
            return;
        }

        while (cursor.moveToNext()) {

            String name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
            );

            String phone = cursor.getString(
                cursor.getColumnIndexOrThrow("phone")
            );

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(15, 15, 15, 15);
            card.setBackgroundColor(Color.WHITE);

            TextView customerName = new TextView(this);
            customerName.setText(name);
            customerName.setTextSize(18);
            customerName.setTextColor(Color.rgb(7, 91, 67));

            card.addView(customerName);

            TextView customerPhone = new TextView(this);
            customerPhone.setText(
                phone == null || phone.isEmpty()
                    ? "شماره تماس ثبت نشده"
                    : "شماره تماس: " + phone
            );
            customerPhone.setTextSize(14);
            customerPhone.setTextColor(Color.DKGRAY);
            customerPhone.setPadding(0, 8, 0, 0);

            card.addView(customerPhone);

            LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                );

            params.setMargins(0, 5, 0, 12);

            listLayout.addView(card, params);
        }

        cursor.close();
    }

    private void showAddCustomerDialog() {

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(20, 10, 20, 10);

        EditText name = new EditText(this);
        name.setHint("نام مشتری");
        name.setSingleLine(true);

        form.addView(name);

        EditText phone = new EditText(this);
        phone.setHint("شماره تماس");
        phone.setInputType(
            android.text.InputType.TYPE_CLASS_PHONE
        );
        phone.setSingleLine(true);

        form.addView(phone);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("ثبت مشتری جدید")
            .setView(form)
            .setPositiveButton("ثبت", null)
            .setNegativeButton("انصراف", null)
            .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    String customerName =
                        name.getText().toString().trim();

                    String customerPhone =
                        phone.getText().toString().trim();

                    if (customerName.isEmpty()) {
                        name.setError("نام مشتری را وارد کن");
                        return;
                    }

                    db.addCustomer(
                        customerName,
                        customerPhone
                    );

                    Toast.makeText(
                        this,
                        "مشتری با موفقیت ثبت شد.",
                        Toast.LENGTH_SHORT
                    ).show();

                    dialog.dismiss();
                    loadCustomers();
                });
        });

        dialog.show();
    }
}
