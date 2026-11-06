package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.text.InputType;

import android.database.Cursor;

import java.util.ArrayList;

public class OrderActivity extends Activity {

    private Database db;

    private Spinner customerSpinner;
    private AutoCompleteTextView productSearch;

    private EditText cartonField;
    private EditText pieceField;
    private EditText paidField;

    private TextView priceView;
    private TextView stockView;
    private TextView cartView;
    private TextView totalView;

    private LinearLayout cartContainer;

    private ArrayList<Long> customerIds = new ArrayList<>();
    private ArrayList<Long> productIds = new ArrayList<>();
    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayList<Long> productPrices = new ArrayList<>();
    private ArrayList<Long> productBuys = new ArrayList<>();
    private ArrayList<Integer> productCartons = new ArrayList<>();
    private ArrayList<Integer> productStocks = new ArrayList<>();

    private ArrayList<CartLine> cart = new ArrayList<>();

    private int selectedProduct = -1;

    private long total = 0;

    private int green = Color.rgb(12, 105, 76);
    private int background = Color.rgb(245, 248, 247);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        buildScreen();
        loadCustomers();
        loadProducts();
        updateCart();
    }

    private TextView text(String value, int size) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(40, 50, 45));
        t.setPadding(4, 10, 4, 10);

        return t;
    }

    private EditText field(String hint) {

        EditText e = new EditText(this);
        e.setHint(hint);
        e.setSingleLine(true);
        e.setTextSize(15);
        e.setPadding(14, 10, 14, 10);
        e.setBackgroundColor(Color.WHITE);
        e.setInputType(InputType.TYPE_CLASS_NUMBER);

        return e;
    }

    private Button button(String title) {

        Button b = new Button(this);
        b.setText(title);
        b.setAllCaps(false);
        b.setTextSize(15);

        return b;
    }

    private LinearLayout section(String title) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(14, 12, 14, 12);
        box.setBackgroundColor(Color.WHITE);

        TextView heading = text(title, 17);
        heading.setTextColor(green);

        box.addView(heading);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1, -2
                );

        params.setMargins(0, 8, 0, 8);

        return box;
    }

    private void buildScreen() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(15, 12, 15, 20);
        root.setBackgroundColor(background);

        scroll.addView(root);

        TextView title = text("ثبت سفارش جدید", 25);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(green);

        root.addView(title);

        root.addView(text(
                "اطلاعات مشتری",
                18
        ));

        customerSpinner = new Spinner(this);

        root.addView(customerSpinner);

        LinearLayout productBox = section("انتخاب کالا");

        productSearch = new AutoCompleteTextView(this);
        productSearch.setHint("نام کالا را جستجو کنید...");
        productSearch.setTextSize(16);
        productSearch.setPadding(15, 12, 15, 12);
        productSearch.setThreshold(1);

        productBox.addView(productSearch);

        priceView = text("قیمت فروش: -", 15);
        productBox.addView(priceView);

        stockView = text("موجودی: -", 15);
        productBox.addView(stockView);

        productSearch.setOnItemClickListener(
                (parent, view, position, id) -> {

                    String selected =
                            parent.getItemAtPosition(position).toString();

                    selectProduct(selected);
                }
        );

        root.addView(productBox);

        LinearLayout quantityBox =
                section("تعداد سفارش");

        cartonField = field("تعداد کارتن");
        pieceField = field("تعداد عدد");

        quantityBox.addView(cartonField);
        quantityBox.addView(pieceField);

        TextView guide = text(
                "مثال: ۲ کارتن و ۵ عدد",
                13
        );

        guide.setTextColor(Color.GRAY);

        quantityBox.addView(guide);

        Button addButton = button("＋ افزودن کالا به سبد");
        addButton.setBackgroundColor(green);
        addButton.setTextColor(Color.WHITE);

        quantityBox.addView(addButton);

        addButton.setOnClickListener(v -> addToCart());

        root.addView(quantityBox);

        LinearLayout basket = section("سبد خرید");

        cartContainer = new LinearLayout(this);
        cartContainer.setOrientation(LinearLayout.VERTICAL);

        basket.addView(cartContainer);

        totalView = text("مبلغ کل: ۰ ریال", 20);
        totalView.setTextColor(green);

        basket.addView(totalView);

        root.addView(basket);

        LinearLayout payment = section("اطلاعات پرداخت");

        paidField = field("مبلغ دریافتی به ریال");
        payment.addView(paidField);

        TextView paymentGuide = text(
                "در صورت پرداخت‌نکردن، مبلغ را خالی بگذارید.",
                13
        );

        payment.addView(paymentGuide);

        root.addView(payment);

        Button saveButton = button("ثبت نهایی سفارش");

        saveButton.setTextSize(18);
        saveButton.setBackgroundColor(green);
        saveButton.setTextColor(Color.WHITE);

        root.addView(saveButton);

        saveButton.setOnClickListener(v -> saveOrder());

        setContentView(scroll);
    }

    private void loadCustomers() {

        customerIds.clear();

        ArrayList<String> names = new ArrayList<>();

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
        productCartons.clear();
        productStocks.clear();

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

            int carton = cursor.getInt(
                    cursor.getColumnIndexOrThrow("per_carton")
            );

            int stock = cursor.getInt(
                    cursor.getColumnIndexOrThrow("stock_pieces")
            );

            productIds.add(id);
            productNames.add(name);
            productPrices.add(price);
            productBuys.add(buy);
            productCartons.add(Math.max(1, carton));
            productStocks.add(stock);

            names.add(name);
        }

        cursor.close();

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        names
                );

        productSearch.setAdapter(adapter);
    }

    private void selectProduct(String name) {

        selectedProduct = productNames.indexOf(name);

        if (selectedProduct < 0) {
            return;
        }

        priceView.setText(
                "قیمت هر عدد: " +
                productPrices.get(selectedProduct) +
                " ریال"
        );

        stockView.setText(
                "موجودی: " +
                productStocks.get(selectedProduct) +
                " عدد | هر کارتن: " +
                productCartons.get(selectedProduct) +
                " عدد"
        );

        cartonField.setText("");
        pieceField.setText("");
    }

    private void addToCart() {

        if (selectedProduct < 0) {

            Toast.makeText(
                    this,
                    "ابتدا کالا را انتخاب کنید.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        try {

            int cartons = cartonField.getText().toString().trim().isEmpty()
                    ? 0
                    : Integer.parseInt(
                            cartonField.getText().toString().trim()
                    );

            int pieces = pieceField.getText().toString().trim().isEmpty()
                    ? 0
                    : Integer.parseInt(
                            pieceField.getText().toString().trim()
                    );

            if (cartons < 0 || pieces < 0 ||
                    (cartons == 0 && pieces == 0)) {

                throw new NumberFormatException();
            }

            long quantity =
                    (long) cartons * productCartons.get(selectedProduct)
                    + pieces;

            if (quantity > Integer.MAX_VALUE) {
                throw new NumberFormatException();
            }

            int already = 0;

            long id = productIds.get(selectedProduct);

            for (CartLine item : cart) {

                if (item.productId == id) {
                    already += item.qty;
                }
            }

            if ((long) already + quantity >
                    productStocks.get(selectedProduct)) {

                Toast.makeText(
                        this,
                        "موجودی کافی نیست.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            cart.add(
                    new CartLine(
                            id,
                            productNames.get(selectedProduct),
                            (int) quantity,
                            productPrices.get(selectedProduct),
                            productBuys.get(selectedProduct)
                    )
            );

            productSearch.setText("");
            selectedProduct = -1;

            priceView.setText("قیمت فروش: -");
            stockView.setText("موجودی: -");

            cartonField.setText("");
            pieceField.setText("");

            updateCart();

            Toast.makeText(
                    this,
                    "کالا اضافه شد.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "تعداد واردشده معتبر نیست.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void updateCart() {

        cartContainer.removeAllViews();

        total = 0;

        if (cart.isEmpty()) {

            cartContainer.addView(
                    text("سبد خرید خالی است.", 15)
            );
        }

        for (int i = 0; i < cart.size(); i++) {

            final int index = i;

            CartLine item = cart.get(i);

            long lineTotal =
                    (long) item.qty * item.price;

            total += lineTotal;

            LinearLayout row = new LinearLayout(this);

            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(8, 10, 8, 10);

            TextView details = text(
                    item.name +
                    "\nتعداد: " + item.qty +
                    " عدد\nمبلغ: " + lineTotal +
                    " ریال",
                    15
            );

            row.addView(details);

            Button remove = button("حذف از سفارش");

            remove.setTextColor(Color.RED);

            remove.setOnClickListener(v -> {

                cart.remove(index);
                updateCart();
            });

            row.addView(remove);

            cartContainer.addView(row);

            View divider = new View(this);
            divider.setBackgroundColor(Color.LTGRAY);

            cartContainer.addView(
                    divider,
                    new LinearLayout.LayoutParams(-1, 1)
            );
        }

        totalView.setText(
                "مبلغ کل سفارش: " + total + " ریال"
        );
    }

    private void saveOrder() {

        if (customerIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "ابتدا یک مشتری ثبت کنید.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        if (cart.isEmpty()) {

            Toast.makeText(
                    this,
                    "سبد خرید خالی است.",
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
                        "ثبت سفارش انجام نشد. موجودی را بررسی کنید.",
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            new AlertDialog.Builder(this)
                    .setTitle("سفارش با موفقیت ثبت شد")
                    .setMessage(
                            "شماره سفارش: " + orderId +
                            "\nمبلغ کل: " + total + " ریال" +
                            "\nپرداختی: " + paid + " ریال" +
                            "\nباقی‌مانده: " +
                            (total - paid) + " ریال"
                    )
                    .setPositiveButton("تأیید", (d, w) -> {

                        cart.clear();

                        paidField.setText("");

                        updateCart();
                        loadProducts();
                    })
                    .show();

        } catch (NumberFormatException e) {

            paidField.setError(
                    "مبلغ معتبر وارد کنید."
            );
        }
    }
    }
