package com.sefareshgir.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME = "sefareshgir.db";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "buy_price INTEGER DEFAULT 0," +
                "piece_price INTEGER DEFAULT 0," +
                "carton_price INTEGER DEFAULT 0," +
                "stock INTEGER DEFAULT 0," +
                "category TEXT DEFAULT ''," +
                "pieces_per_carton INTEGER DEFAULT 1," +
                "image TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT DEFAULT ''," +
                "address TEXT DEFAULT '')");

        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id INTEGER," +
                "customer_name TEXT," +
                "total INTEGER DEFAULT 0," +
                "paid INTEGER DEFAULT 0," +
                "date TEXT)");

        db.execSQL("CREATE TABLE order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "product_id INTEGER," +
                "product_name TEXT," +
                "qty INTEGER," +
                "price INTEGER," +
                "buy_price INTEGER," +
                "subtotal INTEGER)");

        db.execSQL("CREATE TABLE payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "amount INTEGER," +
                "date TEXT)");

        db.execSQL("CREATE TABLE settings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "value TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Database upgrades will be handled here.
    }

    private String currentDate() {
        return new SimpleDateFormat(
                "yyyy-MM-dd HH:mm",
                Locale.getDefault()
        ).format(new Date());
    }

    public Cursor products() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM products ORDER BY name",
                null
        );
    }

    public Cursor customers() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM customers ORDER BY name",
                null
        );
    }

    public Cursor orders() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM orders ORDER BY id DESC",
                null
        );
    }

    public long addCustomer(String name, String phone) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);

        return getWritableDatabase().insert(
                "customers",
                null,
                values
        );
    }

    public long addProduct(
            String name,
            long buyPrice,
            long piecePrice,
            long cartonPrice,
            int stock
    ) {
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("buy_price", buyPrice);
        values.put("piece_price", piecePrice);
        values.put("carton_price", cartonPrice);
        values.put("stock", stock);

        return getWritableDatabase().insert(
                "products",
                null,
                values
        );
    }

    public long createOrder(
            long customerId,
            List<CartLine> cart,
            long paid
    ) {

        SQLiteDatabase db = getWritableDatabase();
        long orderId = -1;

        db.beginTransaction();

        try {

            String customerName = "مشتری متفرقه";

            if (customerId > 0) {
                Cursor customer = db.rawQuery(
                        "SELECT name FROM customers WHERE id=?",
                        new String[]{String.valueOf(customerId)}
                );

                if (customer.moveToFirst()) {
                    customerName = customer.getString(0);
                } else {
                    customer.close();
                    throw new IllegalStateException("مشتری پیدا نشد");
                }

                customer.close();
            }

            long total = 0;

            for (Cart 
