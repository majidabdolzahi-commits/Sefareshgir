package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import android.database.Cursor;

public class ProductActivity extends Activity {

    private Database db;
    private LinearLayout listLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        showScreen();
        loadProducts();
    }

    private void showScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);
        root.setBackgroundColor(Color.rgb(246, 248, 247));

        TextView title = new TextView(this);
        title.setText("مدیریت کالاها");
        title.setTextSize(24);
        title.setTextColor(Color.rgb(7, 91, 67));
        title.setGravity(Gravity.CENTER);
        title.setPadding(5, 15, 5, 20);

        root.addView(title);

        Button addButton = new Button(this);
        addButton.setText("＋ افزودن کالای جدید");
        addButton.setAllCaps(false);
        addButton.setTextSize(16);
        addButton.setOnClickListener(v -> showAddProductDialog());

        root.addView(addButton);

        TextView listTitle = new TextView(this);
        listTitle.setText("فهرست کالاها");
        listTitle.setTextSize(19);
        listTitle.setTextColor(Color.DKGRAY);
        listTitle.setPadding(5, 20, 5, 10);

        root.addView(listTitle);

        ScrollView scrollView = new ScrollView(this);

        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        scrollView.addView(listLayout);
        root.addView(scrollView);

        setContentView(root);
    }

    private void loadProducts() {

        listLayout.removeAllViews();

        Cursor cursor = db.products();

        if (cursor == null || cursor.getCount() == 0) {

            TextView empty = new TextView(this);
            empty.setText("هنوز کالایی ثبت نشده است.");
            empty.setTextSize(16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10, 30, 10, 30);

            listLayout.addView(empty);

            if (cursor != null) cursor.close();
            return;
        }

        while (cursor.moveToNext()) {

            long id = cursor.getLong(
                cursor.getColumnIndexOrThrow("id")
            );

            String name = cursor.getString(
                cursor.getColumnIndexOrThrow("name")
            );

            String category = cursor.getString(
                cursor.getColumnIndexOrThrow("category")
            );

            int perCarton = cursor.getInt(
                cursor.getColumnIndexOrThrow("per_carton")
            );

            long buyPrice = cursor.getLong(
                cursor.getColumnIndexOrThrow("buy_price")
            );

            long piecePrice = cursor.getLong(
                cursor.getColumnIndexOrThrow("piece_price")
            );

            long cartonPrice = cursor.getLong(
                cursor.getColumnIndexOrThrow("carton_price")
            );

            int stock = cursor.getInt(
                cursor.getColumnIndexOrThrow("stock_pieces")
            );

            addProductCard(
                name,
                category,
                perCarton,
                buyPrice,
                piecePrice,
                cartonPrice,
                stock
            );
        }

        cursor.close();
    }

    private void addProductCard(
            String name,
            String category,
            int perCarton,
            long buyPrice,
            long piecePrice,
            long cartonPrice,
            int stock) {

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

        TextView nameView = new TextView(this);
        nameView.setText(name);
        nameView.setTextSize(19);
        nameView.setTextColor(Color.rgb(7, 91, 67));

        card.addView(nameView);

        TextView details = new TextView(this);

        String text =
            "دسته‌بندی: " + category +
            "\nهر کارتن: " + perCarton + " عدد" +
            "\nقیمت خرید: " + buyPrice + " ریال" +
            "\nفروش عددی: " + piecePrice + " ریال" +
            "\nفروش کارتنی: " + cartonPrice + " ریال" +
            "\nموجودی: " + stock + " عدد";

        details.setText(text);
        details.setTextSize(14);
        details.setTextColor(Color.DKGRAY);
        details.setPadding(0, 8, 0, 0);

        card.addView(details);

        listLayout.addView(card, params);
    }

    private void showAddProductDialog() {

        ScrollView scroll = new ScrollView(this);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(20, 10, 20, 10);

        EditText name = addField(form, "نام کالا");
        EditText category = addField(form, "دسته‌بندی");
        EditText perCarton = addNumberField(form, "تعداد عدد در هر کارتن");
        EditText buyPrice = addNumberField(form, "قیمت خرید هر عدد (ریال)");
        EditText piecePrice = addNumberField(form, "قیمت فروش هر عدد (ریال)");
        EditText cartonPrice = addNumberField(form, "قیمت فروش هر کارتن (ریال)");
        EditText stockCartons = addNumberField(form, "موجودی کارتن");
        EditText stockPieces = addNumberField(form, "موجودی عدد");

        scroll.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
            .setTitle("ثبت کالای جدید")
            .setView(scroll)
            .setPositiveButton("ثبت", null)
            .setNegativeButton("انصراف", null)
            .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    try {

                        String productName = name.getText().toString().trim();
                        String productCategory = category.getText().toString().trim();

                        int cartonCount = Integer.parseInt(
                            perCarton.getText().toString().trim()
                        );

                        long buy = Long.parseLong(
                            buyPrice.getText().toString().trim()
                        );

                        long piece = Long.parseLong(
                            piecePrice.getText().toString().trim()
                        );

                        long carton = Long.parseLong(
                            cartonPrice.getText().toString().trim()
                        );

                        int cartons = Integer.parseInt(
                            stockCartons.getText().toString().trim()
                        );

                        int pieces = Integer.parseInt(
                            stockPieces.getText().toString().trim()
                        );

                        if (productName.isEmpty()) {
                            name.setError("نام کالا را وارد کن");
                            return;
                        }

                        if (productCategory.isEmpty()) {
                            productCategory = "عمومی";
                        }

                        if (cartonCount <= 0 || cartons < 0 || pieces < 0 ||
                            pieces >= cartonCount || buy < 0 ||
                            piece < 0 || carton < 0) {

                            Toast.makeText(
                                this,
                                "اطلاعات واردشده معتبر نیست.",
                                Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        long totalStock =
                            (long) cartons * cartonCount + pieces;

                        if (totalStock > Integer.MAX_VALUE) {
                            Toast.makeText(
                                this,
                                "موجودی بیش از حد مجاز است.",
                                Toast.LENGTH_LONG
                            ).show();
                            return;
                        }

                        db.addProduct(
                            productName,
                            productCategory,
                            cartonCount,
                            buy,
                            piece,
                            carton,
                            (int) totalStock
                        );

                        Toast.makeText(
                            this,
                            "کالا با موفقیت ثبت شد.",
                            Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();
                        loadProducts();

                    } catch (NumberFormatException e) {

                        Toast.makeText(
                            this,
                            "لطفاً تمام فیلدهای عددی را تکمیل کن.",
                            Toast.LENGTH_LONG
                        ).show();
                    }
                });
        });

        dialog.show();
    }

    private EditText addField(LinearLayout layout, String hint) {

        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(15);
        field.setSingleLine(true);

        layout.addView(field);

        return field;
    }

    private EditText addNumberField(LinearLayout layout, String hint) {

        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(15);
        field.setInputType(
            android.text.InputType.TYPE_CLASS_NUMBER
        );
        field.setSingleLine(true);

        layout.addView(field);

        return field;
    }
                }
