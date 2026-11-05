package com.sefareshgir.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

public class ProductActivity extends Activity {

    private Database db;
    private LinearLayout listLayout;
    private EditText searchBox;

    private Uri selectedPhoto;
    private ImageView photoPreview;

    private static final int PICK_IMAGE = 1001;

    private int green = Color.rgb(13, 105, 75);
    private int background = Color.rgb(245, 248, 246);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(this);

        buildScreen();
        loadProducts("");
    }

    private void buildScreen() {

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 18, 16, 12);
        root.setBackgroundColor(background);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView title = new TextView(this);
        title.setText("مدیریت کالاها");
        title.setTextSize(24);
        title.setTypeface(null, Typeface.BOLD);
        title.setTextColor(green);
        title.setGravity(Gravity.RIGHT);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("ثبت، جست‌وجو و مدیریت محصولات فروشگاه");
        subtitle.setTextSize(13);
        subtitle.setTextColor(Color.GRAY);
        subtitle.setPadding(0, 5, 0, 16);
        root.addView(subtitle);

        Button addButton = new Button(this);
        addButton.setText("＋ افزودن کالای جدید");
        addButton.setTextSize(16);
        addButton.setAllCaps(false);
        addButton.setTextColor(Color.WHITE);
        addButton.setBackground(round(green, 14));
        addButton.setOnClickListener(v -> showAddDialog());

        root.addView(addButton);

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(15);
        searchBox.setHint("جست‌وجوی نام کالا یا دسته‌بندی");
        searchBox.setPadding(14, 8, 14, 8);
        searchBox.setBackground(round(Color.WHITE, 12));

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(
                        -1, ViewGroup.LayoutParams.WRAP_CONTENT);

        searchParams.setMargins(0, 15, 0, 12);
        root.addView(searchBox, searchParams);

        TextView listTitle = new TextView(this);
        listTitle.setText("فهرست محصولات");
        listTitle.setTextSize(18);
        listTitle.setTypeface(null, Typeface.BOLD);
        listTitle.setTextColor(Color.DKGRAY);
        listTitle.setPadding(0, 5, 0, 10);
        root.addView(listTitle);

        ScrollView scroll = new ScrollView(this);

        listLayout = new LinearLayout(this);
        listLayout.setOrientation(LinearLayout.VERTICAL);

        scroll.addView(listLayout);

        root.addView(scroll,
                new LinearLayout.LayoutParams(-1, 0, 1));

        setContentView(root);

        searchBox.addTextChangedListener(
                new android.text.TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                loadProducts(s.toString());
            }

            @Override
            public void afterTextChanged(
                    android.text.Editable s) {
            }
        });
    }

    private GradientDrawable round(int color, int radius) {

        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(radius);
        return shape;
    }

    private void loadProducts(String query) {

        listLayout.removeAllViews();

        Cursor cursor = db.products();

        int count = 0;

        if (cursor != null) {

            while (cursor.moveToNext()) {

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name"));

                String category = cursor.getString(
                        cursor.getColumnIndexOrThrow("category"));

                if (!name.contains(query)
                        && !category.contains(query)) {
                    continue;
                }

                count++;

                int perCarton = cursor.getInt(
                        cursor.getColumnIndexOrThrow("per_carton"));

                long buy = cursor.getLong(
                        cursor.getColumnIndexOrThrow("buy_price"));

                long piece = cursor.getLong(
                        cursor.getColumnIndexOrThrow("piece_price"));

                long carton = cursor.getLong(
                        cursor.getColumnIndexOrThrow("carton_price"));

                int stock = cursor.getInt(
                        cursor.getColumnIndexOrThrow("stock_pieces"));

                String photo = cursor.getString(
                        cursor.getColumnIndexOrThrow("photo"));

                addProductCard(
                        name, category, perCarton,
                        buy, piece, carton, stock, photo);
            }

            cursor.close();
        }

        if (count == 0) {

            TextView empty = new TextView(this);

            empty.setText(query.isEmpty()
                    ? "هنوز کالایی ثبت نشده است."
                    : "کالایی با این مشخصات پیدا نشد.");

            empty.setTextSize(15);
            empty.setTextColor(Color.GRAY);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10, 35, 10, 35);

            listLayout.addView(empty);
        }
    }

    private void addProductCard(
            String name,
            String category,
            int perCarton,
            long buy,
            long piece,
            long carton,
            int stock,
            String photo) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setPadding(12, 12, 12, 12);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(round(Color.WHITE, 16));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(-1, -2);

        cardParams.setMargins(0, 4, 0, 12);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        image.setBackground(round(
                Color.rgb(235, 241, 237), 12));

        LinearLayout.LayoutParams imageParams =
                new LinearLayout.LayoutParams(85, 85);

        if (photo != null && !photo.isEmpty()) {
            try {
                image.setImageURI(Uri.parse(photo));
            } catch (Exception ignored) {
                image.setImageResource(
                        android.R.drawable.ic_menu_gallery);
            }
        } else {
            image.setImageResource(
                    android.R.drawable.ic_menu_gallery);
        }

        card.addView(image, imageParams);

        LinearLayout details = new LinearLayout(this);
        details.setOrientation(LinearLayout.VERTICAL);
        details.setPadding(12, 0, 0, 0);

        TextView productName = new TextView(this);
        productName.setText(name);
        productName.setTextSize(17);
        productName.setTypeface(null, Typeface.BOLD);
        productName.setTextColor(green);

        details.addView(productName);

        TextView info = new TextView(this);

        info.setText(
                "دسته‌بندی: " + category +
                "\nهر کارتن: " + perCarton + " عدد" +
                "\nقیمت خرید: " + buy + " ریال" +
                "\nفروش عددی: " + piece + " ریال" +
                "\nفروش کارتنی: " + carton + " ریال" +
                "\nموجودی: " + stock + " عدد"
        );

        info.setTextSize(12);
        info.setTextColor(Color.DKGRAY);
        info.setPadding(0, 5, 0, 0);

        details.addView(info);

        card.addView(details,
                new LinearLayout.LayoutParams(0, -2, 1));

        listLayout.addView(card, cardParams);
    }

    private EditText addField(
            LinearLayout layout,
            String hint,
            boolean number) {

        EditText field = new EditText(this);
        field.setHint(hint);
        field.setTextSize(14);
        field.setSingleLine(true);
        field.setPadding(12, 5, 12, 5);
        field.setBackground(round(
                Color.rgb(245, 247, 246), 10));

        if (number) {
            field.setInputType(
                    android.text.InputType.TYPE_CLASS_NUMBER);
        }

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, -2);

        params.setMargins(0, 5, 0, 8);

        layout.addView(field, params);

        return field;
    }

    private void showAddDialog() {

        selectedPhoto = null;

        ScrollView scroll = new ScrollView(this);

        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(18, 10, 18, 10);
        form.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        photoPreview = new ImageView(this);
        photoPreview.setImageResource(
                android.R.drawable.ic_menu_gallery);
        photoPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photoPreview.setBackground(round(
                Color.rgb(235, 241, 237), 14));

        LinearLayout.LayoutParams photoParams =
                new LinearLayout.LayoutParams(120, 120);

        photoParams.gravity = Gravity.CENTER;
        photoParams.setMargins(0, 5, 0, 8);

        form.addView(photoPreview, photoParams);

        Button photoButton = new Button(this);
        photoButton.setText("انتخاب تصویر کالا");
        photoButton.setAllCaps(false);

        photoButton.setOnClickListener(v -> choosePhoto());

        form.addView(photoButton);

        EditText name = addField(form, "نام کالا", false);
        EditText category = addField(form, "دسته‌بندی کالا", false);

        EditText perCarton = addField(
                form, "تعداد عدد در هر کارتن", true);

        EditText buyPrice = addField(
                form, "قیمت خرید هر عدد (ریال)", true);

        EditText piecePrice = addField(
                form, "قیمت فروش هر عدد (ریال)", true);

        EditText cartonPrice = addField(
                form, "قیمت فروش هر کارتن (ریال)", true);

        EditText stockCartons = addField(
                form, "موجودی کارتن", true);

        EditText stockPieces = addField(
                form, "موجودی عدد اضافه", true);

        scroll.addView(form);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("ثبت کالای جدید")
                .setView(scroll)
                .setPositiveButton("ثبت کالا", null)
                .setNegativeButton("انصراف", null)
                .create();

        dialog.setOnShowListener(v -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(button -> {

                try {

                    String productName =
                            name.getText().toString().trim();

                    String productCategory =
                            category.getText().toString().trim();

                    if (productName.isEmpty()) {
                        name.setError("نام کالا را وارد کن");
                        return;
                    }

                    int cartonCount = Integer.parseInt(
                            value(perCarton));

                    long buy = Long.parseLong(value(buyPrice));
                    long piece = Long.parseLong(value(piecePrice));
                    long carton = Long.parseLong(value(cartonPrice));

                    int cartons = Integer.parseInt(
                            value(stockCartons));

                    int pieces = Integer.parseInt(
                            value(stockPieces));

                    if (cartonCount <= 0 ||
                            cartons < 0 || pieces < 0 ||
                            buy < 0 || piece < 0 || carton < 0) {

                        Toast.makeText(this,
                                "اطلاعات واردشده معتبر نیست.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    long totalStock =
                            (long) cartons * cartonCount + pieces;

                    if (totalStock > Integer.MAX_VALUE) {
                        Toast.makeText(this,
                                "موجودی بیش از حد مجاز است.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (productCategory.isEmpty()) {
                        productCategory = "عمومی";
                    }

                    long id = db.addProduct(
                            productName,
                            productCategory,
                            cartonCount,
                            buy,
                            piece,
                            carton,
                            (int) totalStock
                    );

                    if (id <= 0) {
                        Toast.makeText(this,
                                "ثبت کالا انجام نشد.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (selectedPhoto != null) {

                        ContentValues photoValues =
                                new ContentValues();

                        photoValues.put(
                                "photo", selectedPhoto.toString());

                        db.getWritableDatabase().update(
                                "products",
                                photoValues,
                                "id=?",
                                new String[]{String.valueOf(id)}
                        );
                    }

                    Toast.makeText(this,
                            "کالا با موفقیت ثبت شد.",
                            Toast.LENGTH_SHORT).show();

                    dialog.dismiss();
                    loadProducts(searchBox.getText().toString());

                } catch (NumberFormatException e) {

                    Toast.makeText(this,
                            "لطفاً فیلدهای عددی را درست وارد کن.",
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        dialog.show();
    }

    private String value(EditText field) {

        String text = field.getText().toString().trim();

        return text.isEmpty() ? "0" : text;
    }

    private void choosePhoto() {

        Intent intent = new Intent(
                Intent.ACTION_OPEN_DOCUMENT);

        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE &&
                resultCode == RESULT_OK &&
                data != null &&
                data.getData() != null) {

            selectedPhoto = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                        selectedPhoto,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (photoPreview != null) {
                    photoPreview.setImageURI(selectedPhoto);
                }

            } catch (Exception e) {

                Toast.makeText(this,
                        "خواندن تصویر ممکن نشد.",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
            }
