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

        db.execSQL("CREATE TABLE products (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "category TEXT," +
                "unit TEXT DEFAULT 'عدد'," +
                "per_carton INTEGER DEFAULT 1," +
                "buy_price INTEGER DEFAULT 0," +
                "piece_price INTEGER DEFAULT 0," +
                "carton_price INTEGER DEFAULT 0," +
                "stock_pieces INTEGER DEFAULT 0," +
                "photo TEXT)");

        db.execSQL("CREATE TABLE customers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT," +
                "address TEXT)");

        db.execSQL("CREATE TABLE orders (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer_id INTEGER," +
                "customer_name TEXT," +
                "created_at TEXT," +
                "total INTEGER DEFAULT 0," +
                "paid INTEGER DEFAULT 0," +
                "profit INTEGER DEFAULT 0," +
                "note TEXT)");

        db.execSQL("CREATE TABLE order_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "product_id INTEGER," +
                "product_name TEXT," +
                "quantity_pieces INTEGER," +
                "unit_price INTEGER," +
                "buy_price INTEGER," +
                "line_total INTEGER)");

        db.execSQL("CREATE TABLE payments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "order_id INTEGER," +
                "amount INTEGER," +
                "created_at TEXT)");

        db.execSQL("CREATE TABLE settings (" +
                "key TEXT PRIMARY KEY," +
                "value TEXT)");

        db.execSQL("INSERT INTO settings(key,value) VALUES " +
                "('store_name','فروشگاه من')," +
                "('store_phone','')");
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
                "products", null, values);
    }

    // دریافت کالاها

    public Cursor products() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM products ORDER BY name",
                null
        );
    }

    // افزودن مشتری

    public long addCustomer(String name, String phone) {

        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("phone", phone);
        values.put("address", "");

        return getWritableDatabase().insert(
                "customers", null, values);
    }

    // دریافت مشتریان

    public Cursor customers() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM customers ORDER BY name",
                null
        );
    }

    // دریافت سفارش‌ها

    public Cursor orders() {
        return getReadableDatabase().rawQuery(
                "SELECT * FROM orders ORDER BY id DESC",
                null
        );
    }

    // ثبت سفارش

    public long createOrder(
            long customerId,
            ArrayList<CartLine> items,
            long paid) {

        if (items == null || items.isEmpty() || paid < 0) {
            return -1;
        }

        long total = 0;
        long profit = 0;

        for (CartLine item : items) {

            if (item.qty <= 0 || item.price < 0 || item.buy < 0) {
                return -1;
            }

            total += (long) item.qty * item.price;
            profit += (long) item.qty * (item.price - item.buy);
        }

        if (paid > total) {
            return -1;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {

            String customerName;

            Cursor customer = db.query(
                    "customers",
                    new String[]{"name"},
                    "id=?",
                    new String[]{String.valueOf(customerId)},
                    null, null, null
            );

            try {
                if (!customer.moveToFirst()) {
                    return -1;
                }

                customerName = customer.getString(0);

            } finally {
                customer.close();
            }

            ContentValues order = new ContentValues();

            order.put("customer_id", customerId);
            order.put("customer_name", customerName);
            order.put("created_at", currentDate());
            order.put("total", total);
            order.put("paid", paid);
            order.put("profit", profit);
            order.put("note", "");

            long orderId = db.insertOrThrow(
                    "orders", null, order);

            for (CartLine item : items) {

                ContentValues row = new ContentValues();

                row.put("order_id", orderId);
                row.put("product_id", item.productId);
                row.put("product_name", item.name);
                row.put("quantity_pieces", item.qty);
                row.put("unit_price", item.price);
                row.put("buy_price", item.buy);
                row.put("line_total",
                        (long) item.qty * item.price);

                db.insertOrThrow(
                        "order_items", null, row);

                android.database.sqlite.SQLiteStatement statement =
                        db.compileStatement(
                                "UPDATE products " +
                                "SET stock_pieces = stock_pieces - ? " +
                                "WHERE id = ? AND stock_pieces >= ?"
                        );

                statement.bindLong(1, item.qty);
                statement.bindLong(2, item.productId);
                statement.bindLong(3, item.qty);

                int updated = statement.executeUpdateDelete();
                statement.close();

                if (updated != 1) {
                    return -1;
                }
            }

            // ثبت پرداخت اولیه

            if (paid > 0) {

                ContentValues payment = new ContentValues();

                payment.put("order_id", orderId);
                payment.put("amount", paid);
                payment.put("created_at", currentDate());

                db.insertOrThrow(
                        "payments", null, payment);
            }

            db.setTransactionSuccessful();

            return orderId;

        } catch (Exception e) {

            return -1;

        } finally {

            db.endTransaction();
        }
    }

    // محاسبه آمار

    public long scalar(String sql) {

        Cursor cursor = getReadableDatabase().rawQuery(
                sql, null);

        try {
            return cursor.moveToFirst()
                    ? cursor.getLong(0)
                    : 0;

        } finally {
            cursor.close();
        }
    }

    // دریافت تنظیمات

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

    // ذخیره تنظیمات

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

        if (amount <= 0) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {

            Cursor cursor = db.query(
                    "orders",
                    new String[]{"total", "paid"},
                    "id=?",
                    new String[]{String.valueOf(orderId)},
                    null, null, null
            );

            long remaining;

            try {

                if (!cursor.moveToFirst()) {
                    return;
                }

                remaining = cursor.getLong(0) - cursor.getLong(1);

            } finally {
                cursor.close();
            }

            if (amount > remaining) {
                return;
            }

            ContentValues payment = new ContentValues();

            payment.put("order_id", orderId);
            payment.put("amount", amount);
            payment.put("created_at", currentDate());

            db.insertOrThrow(
                    "payments", null, payment);

            ContentValues update = new ContentValues();

            update.put("paid", amount + getPaid(db, orderId));

            db.update(
                    "orders",
                    update,
                    "id=?",
                    new String[]{String.valueOf(orderId)}
            );

            db.setTransactionSuccessful();

        } finally {

            db.endTransaction();
        }
    }

    private long getPaid(SQLiteDatabase db, long orderId) {

        Cursor cursor = db.query(
                "orders",
                new String[]{"paid"},
                "id=?",
                new String[]{String.valueOf(orderId)},
                null, null, null
        );

        try {

            return cursor.moveToFirst()
                    ? cursor.getLong(0)
                    : 0;

        } finally {
            cursor.close();
        }
    }
                    }
