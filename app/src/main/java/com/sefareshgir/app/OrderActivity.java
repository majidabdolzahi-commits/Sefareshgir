package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class OrderActivity extends Activity {

    private Database db;

    private Spinner customerSpinner;
    private Spinner productSpinner;

    private EditText quantityField;
    private EditText paidField;

    private TextView priceView;
    private TextView totalView;

    private ArrayList<Long> customerIds = new ArrayList<>();
    private ArrayList<Long> productIds = new ArrayList<>();
    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayList<Long> productPrices = new ArrayList<>();
    private ArrayList<Long> productBuys = new ArrayList<>();

    private ArrayList<CartLine> cart = new ArrayList<>();

    private long total = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        showScreen();
        loadCustomers();
        loadProducts();
    }

    private void showScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 18);
        root.setBackgroundColor(Color.rgb(246, 248, 247));

        TextView title = new TextView(this);
        title.setText("ثبت سفارش جدید");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(7, 91, 67));
        title.setGravity(Gravity.CENTER);
        title.setPadding(5, 15, 5, 20);

        root.addView(title);

        TextView customerLabel = new TextView(this);
        customerLabel.setText("انتخاب مشتری");
        customerLabel.setTextSize(16);
        root.addView(customerLabel);

        customerSpinner = new Spinner(this);
        root.addView(customerSpinner);

        TextView productLabel = new TextView(this);
        productLabel.setText("انتخاب کالا");
        productLabel.setTextSize(16);
        productLabel.setPadding(0, 15, 0, 0);
        root.addView(productLabel);

        productSpinner = new Spinner(this);
        root.addView(productSpinner);

        priceView = new TextView(this);
        priceView.setTextSize(15);
        priceView.setPadding(0, 10, 0, 10);
        root.addView(priceView);

        quantityField = new EditText(this);
        quantityField.setHint("تعداد به عدد");
        quantityField.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
        );
        root.addView(quantityField);

        Button addButton = new Button(this);
        addButton.setText("افزودن کالا به سفارش");
        addButton.setAllCaps(false);
        root.addView(addButton);

        TextView cartTitle = new TextView(this);
        cartTitle.setText("اقلام سفارش");
        cartTitle.setTextSize(19);
        cartTitle.setPadding(0, 20, 0, 10);
        root.addView(cartTitle);

        totalView = new TextView(this);
        totalView.setTextSize(18);
        totalView.setTextColor(Color.rgb(7, 91, 67));
        totalView.setPadding(0, 15, 0, 10);
        root.addView(totalView);

        paidField = new EditText(this);
        paidField.setHint("مبلغ پرداختی (ریال)");
        paidField.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
        );
        root.addView(paidField);

        Button saveButton = new Button(this);
        saveButton.setText("ثبت نهایی سفارش");
        saveButton.setAllCaps(false);
        root.addView(saveButton);

        addButton.setOnClickListener(v -> addToCart());

        saveButton.setOnClickListener(v -> saveOrder());

        setContentView(root);
    }

    private void loadCustomers() {

        customerIds.clear();

        ArrayList<String> names = new ArrayList<>();

        names.add("مشتری متفرقه");
        customerIds.add(-1L);

        Cursor cursor = db.customers();

        while (cursor.moveToNext()) {

            customerIds.add(
                cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
                )
            );

            names.add(
                cursor.getString(
                    cursor.getColumnIndexOrThrow("name")
                )
            );
        }

        cursor.close();

        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
            );

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        customerSpinner.setAdapter(adapter);
    }

    private void loadProducts() {

        productIds.clear();
        productNames.clear();
        productPrices.clear();
        productBuys.clear();

        ArrayList<String> names = new ArrayList<>();

        Cursor cursor = db.products();

        while (cursor.moveToNext()) {

            long id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
            );

            long price = cursor.getLong(
                cursor.getColumnIndexOrThrow("piece_price")
            );

            long buy = cursor.getLong(
                cursor.getColumnIndexOrThrow("buy_price")
            );

            int stock = cursor.getInt(
                cursor.getColumnIndexOrThrow("stock_pieces")
            );

            if (stock <= 0) {
                continue;
            }

            productIds.add(id);
            productNames.add(name);
            productPrices.add(price);
            productBuys.add(buy);

            names.add(name + " | موجودی: " + stock);
        }

        cursor.close();

        ArrayAdapter<String> adapter =
            new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
            );

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        );

        productSpinner.setAdapter(adapter);

        productSpinner.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(
                        android.widget.AdapterView<?> parent,
                        android.view.View view,
                        int position,
                        long id) {

                    if (position < productPrices.size()) {
                        priceView.setText(
                            "قیمت فروش هر عدد: " +
                            productPrices.get(position) +
                            " ریال"
                        );
                    }
                }

                @Override
                public void onNothingSelected(
                        android.widget.AdapterView<?> parent) {
                }
            }
        );
    }

    private void addToCart() {

        int position = productSpinner.getSelectedItemPosition();

        if (position < 0 || position >= productIds.size()) {
            Toast.makeText(
                this,
                "ابتدا کالا ثبت کن.",
                Toast.LENGTH_SHORT
            ).show();
            return;
        }

        try {

            int quantity = Integer.parseInt(
                quantityField.getText().toString().trim()
            );

            if (quantity <= 0) {
                throw new NumberFormatException();
            }

            long productId = productIds.get(position);

            Cursor cursor = db.products();

            int stock = 0;

            while (cursor.moveToNext()) {

                if (cursor.getLong(
                    cursor.getColumnIndexOrThrow("id")
                ) == productId) {

                    stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow("stock_pieces")
                    );

                    break;
                }
            }

            cursor.close();

            int alreadyAdded = 0;

            for (CartLine line : cart) {
                if (line.productId == productId) {
                    alreadyAdded += line.qty;
                }
            }

            if ((long) alreadyAdded + quantity > stock) {

                Toast.makeText(
                    this,
                    "موجودی کالا کافی نیست.",
                    Toast.LENGTH_LONG
                ).show();

                return;
            }

            cart.add(
                new CartLine(
                    productId,
                    productNames.get(position),
                    quantity,
                    productPrices.get(position),
                    productBuys.get(position)
                )
            );

            quantityField.setText("");

            updateTotal();

            Toast.makeText(
                this,
                "کالا به سفارش اضافه شد.",
                Toast.LENGTH_SHORT
            ).show();

        } catch (NumberFormatException e) {

            quantityField.setError("تعداد معتبر وارد کن");
        }
    }

    private void updateTotal() {

        total = 0;

        StringBuilder details = new StringBuilder();

        for (CartLine line : cart) {

            long lineTotal = (long) line.qty * line.price;

            total += lineTotal;

            details.append(line.name)
                .append(" | ")
                .append(line.qty)
                .append(" عدد | ")
                .append(lineTotal)
                .append(" ریال\n");
        }

        totalView.setText(
            "اقلام سفارش:\n" + details +
            "\nمبلغ کل: " + total + " ریال"
        );
    }

    private void saveOrder() {

        if (cart.isEmpty()) {

            Toast.makeText(
                this,
                "هیچ کالایی به سفارش اضافه نشده است.",
                Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            long paid = paidField.getText().toString().trim().isEmpty()
                ? 0
                : Long.parseLong(
                    paidField.getText().toString().trim()
                );

            if (paid < 0 || paid > total) {

                paidField.setError(
                    "مبلغ پرداختی نامعتبر است."
                );

                return;
            }

            long customerId = customerIds.get(
                customerSpinner.getSelectedItemPosition()
            );

            long orderId = db.createOrder(
                customerId,
                cart,
                paid
            );

            if (orderId == -1) {

                Toast.makeText(
                    this,
                    "ثبت سفارش انجام نشد. موجودی کالا را بررسی کن.",
                    Toast.LENGTH_LONG
                ).show();

                return;
            }

            new AlertDialog.Builder(this)
                .setTitle("سفارش ثبت شد")
                .setMessage(
                    "شماره سفارش: " + orderId +
                    "\nمبلغ کل: " + total + " ریال" +
                    "\nپرداخت‌شده: " + paid + " ریال" +
                    "\nباقی‌مانده: " + (total - paid) + " ریال"
                )
                .setPositiveButton("باشه", (dialog, which) -> {
                    cart.clear();
                    total = 0;
                    paidField.setText("");
                    quantityField.setText("");
                    updateTotal();
                    loadProducts();
                })
                .show();

        } catch (NumberFormatException e) {

            paidField.setError("مبلغ معتبر وارد کن");
        }
    }
          }
