package com.sefareshgir.app;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {

    private final int GREEN = Color.rgb(24,132,96);
    private final int DARK = Color.rgb(30,45,39);
    private final int MUTED = Color.rgb(120,132,126);
    private final int PAGE = Color.rgb(248,250,248);

    private Database db;
    private LinearLayout productList;
    private LinearLayout categoryList;
    private EditText search;

    private String selectedCategory = "همه";

    private ArrayList<Product> products = new ArrayList<>();
    private ArrayList<String> categories = new ArrayList<>();

    private static class Product {
        long id;
        long price;
        String name;
        String category;

        Product(long id, String name, String category, long price) {
            this.id = id;
            this.name = name;
            this.category = category == null ? "" : category;
            this.price = price;
        }
    }

    private int dp(float n) {
        return (int)(n * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable bg(int color, int radius, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));

        if (stroke != 0)
            d.setStroke(dp(1), stroke);

        return d;
    }

    private TextView text(String value, int size,
                          int color, boolean bold) {

        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold)
            t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

        t.setGravity(Gravity.CENTER_VERTICAL);
        t.setTextDirection(View.TEXT_DIRECTION_RTL);

        return t;
    }

    private LinearLayout.LayoutParams lp(int w, int h) {
        return new LinearLayout.LayoutParams(
                w < 0 ? w : dp(w),
                h < 0 ? h : dp(h)
        );
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);

        db = new Database(this);

        getWindow().setStatusBarColor(PAGE);
        getWindow().setNavigationBarColor(Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        );

        buildScreen();
        loadProducts();
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(PAGE);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20),dp(12),dp(20),dp(8));

        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);

        titleBox.addView(text("سفارش‌یار",27,DARK,true));
        titleBox.addView(text(
                "مدیریت سریع سفارش‌های فروشگاه",
                12,MUTED,false
        ));

        header.addView(titleBox,
                new LinearLayout.LayoutParams(0,-2,1));

        TextView offline = text(
                "● آفلاین · ریال",12,GREEN,true
        );

        offline.setGravity(Gravity.CENTER);
        offline.setPadding(dp(13),dp(9),dp(13),dp(9));
        offline.setBackground(bg(
                Color.rgb(231,245,238),25,0
        ));

        header.addView(offline);
        root.addView(header);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setHint("جست‌وجوی نام کالا...");
        search.setPadding(dp(16),0,dp(16),0);
        search.setBackground(bg(
                Color.WHITE,18,Color.rgb(230,235,231)
        ));

        LinearLayout.LayoutParams slp = lp(-1,55);
        slp.setMargins(dp(18),dp(13),dp(18),dp(15));
        root.addView(search,slp);

        TextView catTitle = text(
                "دسته‌بندی کالاها",18,DARK,true
        );

        catTitle.setPadding(dp(20),0,dp(20),dp(7));
        root.addView(catTitle);

        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);

        categoryList = new LinearLayout(this);
        categoryList.setPadding(dp(10),dp(5),dp(10),dp(10));

        hsv.addView(categoryList);
        root.addView(hsv);

        LinearLayout section = new LinearLayout(this);
        section.setGravity(Gravity.CENTER_VERTICAL);
        section.setPadding(dp(20),dp(6),dp(20),dp(9));

        section.addView(text(
                "همه کالاها",19,DARK,true
        ),new LinearLayout.LayoutParams(0,-2,1));

        section.addView(text(
                "برای افزودن لمس کنید",11,MUTED,false
        ));

        root.addView(section);

        ScrollView scroll = new ScrollView(this);

        productList = new LinearLayout(this);
        productList.setOrientation(LinearLayout.VERTICAL);
        productList.setPadding(dp(12),dp(2),dp(12),dp(14));

        scroll.addView(productList);

        root.addView(scroll,
                new LinearLayout.LayoutParams(-1,0,1));

        search.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(
                    CharSequence s,int st,int c,int a) {}

            public void onTextChanged(
                    CharSequence s,int st,int before,int count) {
                render();
            }

            public void afterTextChanged(Editable e) {}
        });

        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setBackgroundColor(Color.WHITE);
        nav.setElevation(dp(8));
        nav.setPadding(dp(5),dp(6),dp(5),dp(4));

        navItem(nav,"کالاها","⌂",true,()->{});

        navItem(nav,"سبد سفارش","🛒",false,()->{
            startActivity(new Intent(this,OrderActivity.class));
        });

        navItem(nav,"مدیریت","＋",false,()->{
            startActivity(new Intent(this,ProductActivity.class));
        });

        navItem(nav,"سفارش‌ها","▤",false,()->{
            startActivity(new Intent(this,PaymentActivity.class));
        });

        root.addView(nav,lp(-1,62));

        setContentView(root);
    }

    private void navItem(LinearLayout bar,
                         String caption,
                         String icon,
                         boolean active,
                         Runnable click) {

        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);

        TextView a = text(
                icon,22,active ? GREEN : MUTED,true
        );

        a.setGravity(Gravity.CENTER);

        TextView b = text(
                caption,10,active ? GREEN : MUTED,active
        );

        b.setGravity(Gravity.CENTER);

        item.addView(a);
        item.addView(b);

        bar.addView(item,
                new LinearLayout.LayoutParams(0,-1,1));

        item.setOnClickListener(v -> click.run());
    }

    private void loadProducts() {

        products.clear();
        categories.clear();

        Cursor c = db.products();

        try {

            while(c.moveToNext()) {

                String cat = c.getString(
                        c.getColumnIndexOrThrow("category")
                );

                Product p = new Product(
                        c.getLong(c.getColumnIndexOrThrow("id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        cat,
                        c.getLong(c.getColumnIndexOrThrow("piece_price"))
                );

                products.add(p);

                if(cat != null && !cat.trim().isEmpty()
                        && !categories.contains(cat)) {
                    categories.add(cat);
                }
            }

        } finally {
            c.close();
        }

        buildCategories();
        render();
    }

    private void buildCategories() {

        categoryList.removeAllViews();

        addCategory("همه");

        for(String c : categories) {
            addCategory(c);
        }
    }

    private void addCategory(String name) {

        TextView chip = text(
                name,14,
                name.equals(selectedCategory) ? Color.WHITE : MUTED,
                true
        );

        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(17),dp(9),dp(17),dp(9));

        chip.setBackground(bg(
                name.equals(selectedCategory) ? GREEN : Color.WHITE,
                25,0
        ));

        LinearLayout.LayoutParams p = lp(-2,42);
        p.setMargins(dp(5),0,dp(5),0);

        categoryList.addView(chip,p);

        chip.setOnClickListener(v -> {
            selectedCategory = name;
            buildCategories();
            render();
        });
    }

    private void render() {

        productList.removeAllViews();

        ArrayList<Product> visible = new ArrayList<>();

        String q = search.getText().toString()
                .trim().toLowerCase(Locale.ROOT);

        for(Product p : products) {

            boolean categoryOk =
                    selectedCategory.equals("همه")
                    || p.category.equals(selectedCategory);

            boolean queryOk =
                    p.name.toLowerCase(Locale.ROOT).contains(q);

            if(categoryOk && queryOk)
                visible.add(p);
        }

        if(visible.isEmpty()) {

            TextView empty = text(
                    products.isEmpty()
                    ? "هنوز کالایی ثبت نشده است."
                    : "کالایی پیدا نشد.",
                    14,MUTED,false
            );

            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(20),dp(35),dp(20),dp(35));

            productList.addView(empty);
            return;
        }

        for(int i=0;i<visible.size();i+=2) {

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            row.addView(card(visible.get(i)),
                    new LinearLayout.LayoutParams(0,dp(244),1));

            if(i+1<visible.size()) {

                row.addView(new View(this),lp(10,1));

                row.addView(card(visible.get(i+1)),
                        new LinearLayout.LayoutParams(0,dp(244),1));
            }

            LinearLayout.LayoutParams rp = lp(-1,-2);
            rp.setMargins(0,0,0,dp(12));

            productList.addView(row,rp);
        }
    }

    private View card(Product p) {

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setBackground(bg(
                Color.WHITE,20,Color.rgb(232,237,233)
        ));

        box.setClipToOutline(true);

        TextView picture = text("🛍️",43,GREEN,false);
        picture.setGravity(Gravity.CENTER);
        picture.setBackground(bg(
                Color.rgb(237,244,239),0,0
        ));

        box.addView(picture,lp(-1,122));

        TextView name = text(p.name,15,DARK,true);
        name.setMaxLines(2);
        name.setPadding(dp(10),dp(9),dp(10),0);

        box.addView(name);

        TextView price = text(
                NumberFormat.getInstance(Locale.US)
                .format(p.price) + " ریال / عدد",
                12,GREEN,true
        );

        price.setPadding(dp(10),dp(5),dp(10),dp(7));

        box.addView(price);

        TextView add = text(
                "＋ افزودن به سفارش",13,GREEN,true
        );

        add.setGravity(Gravity.CENTER);
        add.setBackground(bg(
                Color.rgb(232,246,239),13,0
        ));

        LinearLayout.LayoutParams ap = lp(-1,39);
        ap.setMargins(dp(8),0,dp(8),dp(8));

        box.addView(add,ap);

        View.OnClickListener openOrder = v ->
                startActivity(new Intent(
                        MainActivity.this,OrderActivity.class
                ));

        add.setOnClickListener(openOrder);
        box.setOnClickListener(openOrder);

        return box;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(db != null && productList != null)
            loadProducts();
    }
                }
