package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Locale;

public class OrderActivity extends Activity {

    private Database db;

    private LinearLayout cartLayout;
    private Spinner customerSpinner;
    private Spinner productSpinner;

    private EditText quantityField;
    private EditText paidField;

    private TextView priceView;
    private TextView stockView;
    private TextView totalView;
    private TextView cartCountView;

    private ArrayList<Long> customerIds = new ArrayList<>();
    private ArrayList<Long> productIds = new ArrayList<>();
    private ArrayList<String> productNames = new ArrayList<>();
    private ArrayList<Long> productPrices = new ArrayList<>();
    private ArrayList<Long> productBuys = new ArrayList<>();
    private ArrayList<Integer> productStocks = new ArrayList<>();

    private ArrayList<CartLine> cart = new ArrayList<>();

    private long total = 0;

    private final int GREEN = Color.rgb(7, 91, 67);
    private final int DARK = Color.rgb(35, 48, 43);
    private final int BACKGROUND = Color.rgb(245, 247, 246);
    private final int GRAY = Color.rgb(110, 120, 115);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        showScreen();
        loadCustomers();
        loadProducts();
        updateCart();
    }

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable shape(int color, int radius) {

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));

        return drawable;
    }

    private TextView text(
            String value,
            int size,
            int color,
            boolean bold) {

        TextView view = new TextView(this);

        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);

        if (bold) {
            view.setTypeface(null, Typeface.BOLD);
        }

        return view;
    }

    private void styleButton(Button button, int color) {

        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setTextSize(14);
        button.setTypeface(null, Typeface.BOLD);
        button.setBackground(shape(color, 12));
        button.setPadding(dp(12), dp(10), dp(12), dp(10));
    }

    private LinearLayout card() {

        LinearLayout layout = new LinearLayout(this);

        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(15), dp(15), dp(15), dp(15));
        layout.setBackground(shape(Color.WHITE, 16));
        layout.setElevation(dp(2));
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        -1,
                        -2
                );

        params.setMargins(0, 0, 0, dp(14));

        layout.setLayoutParams(params);

        return layout;
    }

    private void addLabel(LinearLayout parent, String value) {

        TextView label = text(value, 14, DARK, true);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.setMargins(0, dp(8), 0, dp(6));

        parent.addView(label, params);
    }

    private void showScreen() {

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);

        LinearLayout root = new LinearLayout(this);

        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(16), dp(16), dp(24));
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        scroll.addView(root);

        // سربرگ

        LinearLayout header = card();
        header.setBackground(shape(GREEN, 18));

        TextView title = text(
                "ثبت سفارش جدید",
                23,
                Color.WHITE,
                true
        );

        title.setGravity(Gravity.CENTER);
        header.addView(title);

        TextView subtitle = text(
                "مدیریت سفارش و فروشگاه",
                13,
                Color.rgb(220, 240, 230),
                false
        );

        subtitle.setGravity(Gravity.CENTER);

        header.addView(subtitle);

        root.addView(header);

        // انتخاب مشتری

        LinearLayout customerCard = card();

        customerCard.addView(
                text("اطلاعات مشتری", 18, GREEN, true)
        );

        addLabel(customerCard, "انتخاب مشتری");

        customerSpinner = new Spinner(this);
        customerSpinner.setMinimumHeight(dp(48));

        customerCard.addView(customerSpinner);

        root.addView(customerCard);

        // انتخاب کالا

        LinearLayout productCard = card();

        productCard.addView(
                text("انتخاب محصول", 18, GREEN, true)
        );

        addLabel(productCard, "نام کالا");

        productSpinner = new Spinner(this);
        productSpinner.setMinimumHeight(dp(48));

        productCard.addView(productSpinner);

        priceView = text(
                "قیمت فروش: ۰ ریال",
                15,
                GREEN,
                true
        );

        priceView.setPadding(0, dp(12), 0, dp(4));

        productCard.addView(priceView);

        stockView = text(
                "موجودی: ۰ عدد",
                14,
                GRAY,
                false
        );

        productCard.addView(stockView);

        addLabel(productCard, "تعداد موردنظر");

        quantityField = new EditText(this);
        quantityField.setSingleLine(true);
        quantityField.setTextSize(15);
        quantityField.setHint("تعداد به عدد");
        quantityField.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        quantityField.setPadding(
                dp(12), dp(10), dp(12), dp(10)
        );

        quantityField.setBackground(shape(BACKGROUND, 12));

        productCard.addView(quantityField);

        Button addButton = new Button(this);
        addButton.setText("افزودن به سبد خرید");
        styleButton(addButton, GREEN);

        LinearLayout.LayoutParams addParams =
                new LinearLayout.LayoutParams(-1, -2);

        addParams.setMargins(0, dp(12), 0, 0);

        productCard.addView(addButton, addParams);

        root.addView(productCard);

        addButton.setOnClickListener(v -> addToCart());

        productSpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(
                    android.widget.AdapterView<?> parent,
                    View view,
                    int position,
                    long id) {

                if (position >= 0 &&
                        position < productPrices.size()) {

                    priceView.setText(
                            "قیمت فروش: " +
                            money(productPrices.get(position)) +
                            " ریال"
                    );

                    stockView.setText(
                            "موجودی: " +
                            productStocks.get(position) +
                            " عدد"
                    );

                } else {

                    priceView.setText("قیمت فروش: ۰ ریال");
                    stockView.setText("موجودی: ۰ عدد");
                }
            }

            @Override
            public void onNothingSelected(
                    android.widget.AdapterView<?> parent) {
            }
        });

        // سبد خرید

        LinearLayout cartCard = card();

        TextView cartTitle = text(
                "سبد خرید",
                19,
                GREEN,
                true
        );

        cartCard.addView(cartTitle);

        cartCountView = text(
                "تعداد اقلام: ۰",
                13,
                GRAY,
                false
        );

        cartCard.addView(cartCountView);

        cartLayout = new LinearLayout(this);
        cartLayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams cartParams =
                new LinearLayout.LayoutParams(-1, -2);

        cartParams.setMargins(0, dp(10), 0, 0);

        cartCard.addView(cartLayout, cartParams);

        root.addView(cartCard);

        // محاسبات مالی

        LinearLayout paymentCard = card();

        paymentCard.addView(
                text("خلاصه مالی سفارش", 19, GREEN, true)
        );

        totalView = text(
                "مبلغ کل: ۰ ریال",
                20,
                GREEN,
                true
        );

        totalView.setPadding(0, dp(15), 0, dp(15));

        paymentCard.addView(totalView);

        addLabel(paymentCard, "مبلغ دریافتی از مشتری (ریال)");

        paidField = new EditText(this);

        paidField.setSingleLine(true);
        paidField.setHint("مبلغ پرداختی");
        paidField.setTextSize(15);

        paidField.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER
        );

        paidField.setPadding(
                dp(12), dp(10), dp(12), dp(10)
        );

        paidField.setBackground(shape(BACKGROUND, 12));

        paymentCard.addView(paidField);

        Button saveButton = new Button(this);

        saveButton.setText("ثبت نهایی سفارش");
        styleButton(saveButton, GREEN);

        LinearLayout.LayoutParams saveParams =
                new LinearLayout.LayoutParams(-1, -2);

        saveParams.setMargins(0, dp(18), 0, 0);

        paymentCard.addView(saveButton, saveParams);

        root.addView(paymentCard);

        saveButton.setOnClickListener(v -> saveOrder());

        setContentView(scroll);
    }

    private String money(long value) {
        return String.format(Locale.US, "%,d", value);
    }

    private void loadCustomers() {

        customerIds.clear();

        ArrayList<String> names = new ArrayList<>();

        names.add("مشتری متفرقه");
        customerIds.add(-1L);

        Cursor cursor = db.customers();

        try {

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

        } finally {
            cursor.close();
        }

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
        productStocks.clear();

        ArrayList<String> names = new ArrayList<>();

        Cursor cursor = db.products();

        try {

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
                productStocks.add(stock);

                names.add(name);
            }

        } finally {
            cursor.close();
        }

        if (names.isEmpty()) {
            names.add("کالای موجودی‌دار ثبت نشده است");
        }

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
    }

    private int getCurrentStock(long productId) {

        Cursor cursor = db.products();

        try {

            while (cursor.moveToNext()) {

                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow("id")
                );

                if (id == productId) {

                    return cursor.getInt(
                            cursor.getColumnIndexOrThrow("stock_pieces")
                    );
                }
            }

        } finally {
            cursor.close();
        }

        return 0;
    }

    private void addToCart() {

        int position = productSpinner.getSelectedItemPosition();

        if (position < 0 || position >= productIds.size()) {

            Toast.makeText(
                    this,
                    "ابتدا کالای معتبر انتخاب کن.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String value = quantityField.getText().toString().trim();

        if (value.isEmpty()) {

            quantityField.setError("تعداد را وارد کن");
            return;
        }

        try {

            int quantity = Integer.parseInt(value);

            if (quantity <= 0) {
                quantityField.setError("تعداد باید بیشتر از صفر باشد");
                return;
            }

            long productId = productIds.get(position);

            int stock = getCurrentStock(productId);

            int alreadyAdded = 0;

            CartLine existing = null;

            for (CartLine line : cart) {

                if (line.productId == productId) {

                    alreadyAdded += line.qty;
                    existing = line;
                }
            }

            if ((long) alreadyAdded + quantity > stock) {

                quantityField.setError("موجودی کالا کافی نیست");

                Toast.makeText(
                        this,
                        "موجودی قابل سفارش: " +
                        Math.max(0, stock - alreadyAdded),
                        Toast.LENGTH_LONG
                ).show();

                return;
            }

            if (existing != null) {

                existing.qty += quantity;

            } else {

                cart.add(
                        new CartLine(
                                productId,
                                productNames.get(position),
                                quantity,
                                productPrices.get(position),
                                productBuys.get(position)
                        )
                );
            }

            quantityField.setText("");

            updateCart();

            Toast.makeText(
                    this,
                    "کالا به سبد خرید اضافه شد.",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (NumberFormatException e) {

            quantityField.setError("تعداد معتبر وارد کن");
        }
    }

    private void updateCart() {

        cartLayout.removeAllViews();

        total = 0;

        int count = 0;

        for (int i = 0; i < cart.size(); i++) {

            CartLine line = cart.get(i);

            long lineTotal = (long) line.qty * line.price;

            total += lineTotal;
            count += line.qty;

            addCartItem(line);
        }

        cartCountView.setText(
                "تعداد اقلام: " + cart.size() +
                " | مجموع تعداد: " + count
        );

        totalView.setText(
                "مبلغ کل سفارش: " + money(total) + " ریال"
        );

        if (cart.isEmpty()) {

            TextView empty = text(
                    "سبد خرید خالی است.",
                    14,
                    GRAY,
                    false
            );

            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, dp(20), 0, dp(20));

            cartLayout.addView(empty);
        }
    }

    private void addCartItem(CartLine line) {

        LinearLayout item = new LinearLayout(this);

        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(12), dp(12), dp(12));
        item.setBackground(shape(BACKGROUND, 12));
        item.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout.LayoutParams itemParams =
                new LinearLayout.LayoutParams(-1, -2);

        itemParams.setMargins(0, 0, 0, dp(9));

        item.setLayoutParams(itemParams);

        TextView name = text(
                line.name,
                16,
                DARK,
                true
        );

        item.addView(name);

        TextView details = text(
                "قیمت واحد: " + money(line.price) +
                " ریال\nمبلغ: " +
                money((long) line.qty * line.price) +
                " ریال",
                13,
                GRAY,
                false
        );

        details.setPadding(0, dp(8), 0, dp(8));

        item.addView(details);

        LinearLayout controls = new LinearLayout(this);

        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER_VERTICAL);

        Button minus = new Button(this);
        minus.setText("−");
        styleButton(minus, GRAY);

        Button plus = new Button(this);
        plus.setText("+");
        styleButton(plus, GREEN);

        TextView quantity = text(
                "تعداد: " + line.qty,
                15,
                DARK,
                true
        );

        quantity.setGravity(Gravity.CENTER);

        LinearLayout.LayoutParams quantityParams =
                new LinearLayout.LayoutParams(0, -2, 1);

        controls.addView(minus);
        controls.addView(quantity, quantityParams);
        controls.addView(plus);

        item.addView(controls);

        Button delete = new Button(this);

        delete.setText("حذف از سبد خرید");
        styleButton(delete, Color.rgb(190, 55, 55));

        LinearLayout.LayoutParams deleteParams =
                new LinearLayout.LayoutParams(-1, -2);

        deleteParams.setMargins(0, dp(8), 0, 0);

        item.addView(delete, deleteParams);

        plus.setOnClickListener(v -> {

            int stock = getCurrentStock(line.productId);

            int added = 0;

            for (CartLine other : cart) {

                if (other.productId == line.productId) {
                    added += other.qty;
                }
            }

            if (added >= stock) {

                Toast.makeText(
                        this,
                        "موجودی کافی نیست.",
              
