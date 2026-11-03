package com.sefareshgir.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {

    private static final String DB_NAME = "sefareshgir.db";
    private static final int DB_VERSION = 1;

    public Database(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(
            "CREATE TABLE products (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "category TEXT," +
            "unit TEXT DEFAULT 'عدد'," +
            "per_carton INTEGER DEFAULT 1," +
            "buy_price INTEGER DEFAULT 0," +
            "piece_price INTEGER DEFAULT 0," +
            "carton_price INTEGER DEFAULT 0," +
            "stock_pieces INTEGER DEFAULT 0," +
            "photo TEXT)"
        );

        db.execSQL(
            "CREATE TABLE customers (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "phone TEXT," +
            "address TEXT)"
        );

        db.execSQL(
            "CREATE TABLE orders (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "customer_id INTEGER," +
            "customer_name TEXT," +
            "created_at TEXT," +
            "total INTEGER DEFAULT 0," +
            "paid INTEGER DEFAULT 0," +
            "profit INTEGER DEFAULT 0," +
            "note TEXT)"
        );

        db.execSQL(
            "CREATE TABLE order_items (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER," +
            "product_id INTEGER," +
            "product_name TEXT," +
            "quantity_pieces INTEGER," +
            "unit_price INTEGER," +
            "buy_price INTEGER," +
            "line_total INTEGER)"
        );

        db.execSQL(
            "CREATE TABLE payments (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "order_id INTEGER," +
            "amount INTEGER," +
            "created_at TEXT)"
        );

        db.execSQL(
            "CREATE TABLE settings (" +
            "key TEXT PRIMARY KEY," +
            "value TEXT)"
        );

        db.execSQL(
            "INSERT INTO settings(key,value) VALUES " +
            "('store_name','فروشگاه من')," +
            "('store_phone','')"
        );
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {
    }

    private String currentDate() {
        return new SimpleDateFormat(
            "yyyy/MM/dd HH:mm",
            Locale.getDefault()
        ).format(new Date());
    }

    // افزودن کالا

    public long addProduct(
            String name,
            String category,
            int perCarton,
            long buyPrice,
            long piecePrice,
            long cartonPrice,
            int stock) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("category", category);
        values.put("per_carton", Math.max(1, perCarton));
        values.put("buy_price", buyPrice);
        values.put("piece_price", piecePrice);
        values.put("carton_price", cartonPrice);
        values.put("stock_pieces", stock);

        return getWritableDatabase().insert(
            "products", null, values
        );
    }

    // دریافت کالاها

    public Cursor products() {

        return getReadableDatabase().rawQuery(
            "SELECT * FROM products ORDER BY name",
            null
        );
    }

    // افزودن مشتری

    public long addCustomer(
            String name,
            String phone,
            String address) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("address", address);

        return getWritableDatabase().insert(
            "customers", null, values
        );
    }

    // دریافت مشتریان

    public Cursor customers() {

        return getReadableDatabase().rawQuery(
            "SELECT * FROM customers ORDER BY name",
            null
        );
    }

    // ثبت سفارش

    public long createOrder(
            String customer,
            long total,
            long paid,
            long profit,
            String note,
            ArrayList<CartLine> items) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues order = new ContentValues();

            order.put("customer_name", customer);
            order.put("created_at", currentDate());
            order.put("total", total);
            order.put("paid", paid);
            order.put("profit", profit);
            order.put("note", note);

            long orderId = db.insertOrThrow(
                "orders", null, order
            );

            for (CartLine item : items) {

                ContentValues row = new ContentValues();

                row.put("order_id", orderId);
                row.put("product_id", item.productId);
                row.put("product_name", item.name);
                row.put("quantity_pieces", item.qty);
                row.put("unit_price", item.price);
                row.put("buy_price", item.buy);
                row.put("line_total", item.qty * item.price);

                db.insertOrThrow(
                    "order_items", null, row
                );

                db.execSQL(
                    "UPDATE products " +
                    "SET stock_pieces = stock_pieces - ? " +
                    "WHERE id = ? AND stock_pieces >= ?",
                    new Object[]{
                        item.qty,
                        item.productId,
                        item.qty
                    }
                );
            }

            if (paid > 0) {

                ContentValues payment = new ContentValues();

                payment.put("order_id", orderId);
                payment.put("amount", paid);
                payment.put("created_at", currentDate());

                db.insert("payments", null, payment);
            }

            db.setTransactionSuccessful();

            return orderId;

        } finally {
            db.endTransaction();
        }
    }

    // دریافت سفارش‌ها

    public Cursor orders() {

        return getReadableDatabase().rawQuery(
            "SELECT * FROM orders ORDER BY id DESC",
            null
        );
    }

    // محاسبه آمار

    public long scalar(String sql) {

        Cursor cursor =
            getReadableDatabase().rawQuery(sql, null);

        try {
            return cursor.moveToFirst()
                ? cursor.getLong(0)
                : 0;
        } finally {
            cursor.close();
        }
    }

    // تنظیمات فروشگاه

    public String setting(String key) {

        Cursor cursor = getReadableDatabase().rawQuery(
            "SELECT value FROM settings WHERE key=?",
            new String[]{key}
        );

        try {
            return cursor.moveToFirst()
                ? cursor.getString(0)
                : "";
        } finally {
            cursor.close();
        }
    }

    public void setSetting(String key, String value) {

        ContentValues values = new ContentValues();

        values.put("key", key);
        values.put("value", value);

        getWritableDatabase().insertWithOnConflict(
            "settings",
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    // ثبت پرداخت جدید

    public void addPayment(long orderId, long amount) {

        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();

        try {

            ContentValues payment = new ContentValues();

            payment.put("order_id", orderId);
            payment.put("amount", amount);
            payment.put("created_at", currentDate());

            db.insert("payments", null, payment);

            db.execSQL(
                "UPDATE orders SET paid = paid + ? WHERE id = ?",
                new Object[]{amount, orderId}
            );

            db.setTransactionSuccessful();

        } finally {
            db.endTransaction();
        }
    }
}
