package com.techfix.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "techfix.db";
    public static final int DB_VERSION = 1;

    public static final String T_USERS = "users";
    public static final String T_BRANCHES = "branches";
    public static final String T_TECHNICIANS = "technicians";
    public static final String T_CATEGORIES = "categories";
    public static final String T_SERVICES = "services";
    public static final String T_PARTS = "spare_parts";
    public static final String T_APPOINTMENTS = "appointments";
    public static final String T_IMAGES = "repair_images";
    public static final String T_PAYMENTS = "payments";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "email TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "phone TEXT,"
                + "role TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_BRANCHES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "address TEXT,"
                + "city TEXT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "phone TEXT)");

        db.execSQL("CREATE TABLE " + T_TECHNICIANS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "branch_id INTEGER NOT NULL,"
                + "specialty TEXT,"
                + "available INTEGER DEFAULT 1,"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id))");

        db.execSQL("CREATE TABLE " + T_CATEGORIES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "description TEXT)");

        db.execSQL("CREATE TABLE " + T_SERVICES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "category_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "price REAL NOT NULL,"
                + "description TEXT,"
                + "sample_image_hint TEXT,"
                + "FOREIGN KEY(category_id) REFERENCES " + T_CATEGORIES + "(id))");

        db.execSQL("CREATE TABLE " + T_PARTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "quantity INTEGER NOT NULL,"
                + "branch_id INTEGER NOT NULL,"
                + "category_id INTEGER NOT NULL,"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id),"
                + "FOREIGN KEY(category_id) REFERENCES " + T_CATEGORIES + "(id))");

        db.execSQL("CREATE TABLE " + T_APPOINTMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "customer_id INTEGER NOT NULL,"
                + "branch_id INTEGER,"
                + "technician_id INTEGER,"
                + "service_id INTEGER NOT NULL,"
                + "device_note TEXT,"
                + "status TEXT NOT NULL,"
                + "created_at TEXT NOT NULL,"
                + "FOREIGN KEY(customer_id) REFERENCES " + T_USERS + "(id),"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id),"
                + "FOREIGN KEY(technician_id) REFERENCES " + T_TECHNICIANS + "(id),"
                + "FOREIGN KEY(service_id) REFERENCES " + T_SERVICES + "(id))");

        db.execSQL("CREATE TABLE " + T_IMAGES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "appointment_id INTEGER NOT NULL,"
                + "file_path TEXT NOT NULL,"
                + "caption TEXT,"
                + "FOREIGN KEY(appointment_id) REFERENCES " + T_APPOINTMENTS + "(id))");

        db.execSQL("CREATE TABLE " + T_PAYMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "appointment_id INTEGER NOT NULL,"
                + "amount REAL NOT NULL,"
                + "method TEXT,"
                + "paid INTEGER DEFAULT 0,"
                + "paid_at TEXT,"
                + "FOREIGN KEY(appointment_id) REFERENCES " + T_APPOINTMENTS + "(id))");

        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + T_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PARTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + T_TECHNICIANS);
        db.execSQL("DROP TABLE IF EXISTS " + T_BRANCHES);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        insertUser(db, "Demo Customer", "customer@techfix.lk", "customer123", "0771234567", "CUSTOMER");
        insertUser(db, "TechFix Staff", "staff@techfix.lk", "staff123", "0112345678", "STAFF");

        long colombo = insertBranch(db, "TechFix Colombo", "42 Galle Road, Colombo 03", "Colombo",
                6.9271, 79.8612, "0112555000");
        long galle = insertBranch(db, "TechFix Galle", "12 Church Street, Galle Fort", "Galle",
                6.0535, 80.2210, "0912223344");

        insertTech(db, "Nimal Perera", colombo, "Mobile screens & batteries", 1);
        insertTech(db, "Ishara Fernando", colombo, "Laptops & motherboards", 1);
        insertTech(db, "Kasun Silva", galle, "Mobile water damage", 1);
        insertTech(db, "Tharushi Jayasuriya", galle, "Desktop & OS install", 1);

        long computers = insertCategory(db, "Computer", "Laptops, desktops and accessories");
        long mobiles = insertCategory(db, "Mobile Phone", "Smartphones and tablets");

        insertService(db, computers, "Laptop screen replacement", 18500,
                "Replace cracked or dim laptop LCD/LED panels with genuine-grade parts.",
                "Before/after laptop panel swap");
        insertService(db, computers, "Laptop battery replacement", 12500,
                "Diagnose swelling batteries and fit a compatible replacement.",
                "Battery health check photos");
        insertService(db, computers, "OS install & data backup", 7500,
                "Clean Windows/Linux install with optional data backup.",
                "Setup complete screenshot");
        insertService(db, computers, "Motherboard diagnosis", 4500,
                "Board-level inspection. Repair quoted after diagnosis.",
                "Board inspection close-up");
        insertService(db, mobiles, "Phone screen replacement", 14500,
                "OLED/LCD replacement for popular Android and iPhone models.",
                "Cracked vs new screen");
        insertService(db, mobiles, "Battery replacement", 6500,
                "Restore battery health with certified cells.",
                "Battery swap sample");
        insertService(db, mobiles, "Charging port repair", 5500,
                "Clean or replace USB-C / Lightning ports.",
                "Port close-up");
        insertService(db, mobiles, "Water damage recovery", 8900,
                "Ultrasonic clean and component check after liquid damage.",
                "Board after cleaning");

        insertPart(db, "Laptop LCD 15.6\"", 6, colombo, computers);
        insertPart(db, "Laptop battery pack", 8, colombo, computers);
        insertPart(db, "Phone OLED panel", 10, colombo, mobiles);
        insertPart(db, "Phone battery cell", 14, colombo, mobiles);
        insertPart(db, "USB-C charging board", 7, colombo, mobiles);
        insertPart(db, "Laptop LCD 15.6\"", 4, galle, computers);
        insertPart(db, "Laptop battery pack", 5, galle, computers);
        insertPart(db, "Phone OLED panel", 8, galle, mobiles);
        insertPart(db, "Phone battery cell", 11, galle, mobiles);
        insertPart(db, "USB-C charging board", 5, galle, mobiles);

        ContentValues appt = new ContentValues();
        appt.put("customer_id", 1);
        appt.put("branch_id", colombo);
        appt.put("technician_id", 1);
        appt.put("service_id", 5);
        appt.put("device_note", "Samsung A54 — cracked front glass, touch still works.");
        appt.put("status", "COMPLETED");
        appt.put("created_at", "2026-07-02 10:15");
        long historyId = db.insert(T_APPOINTMENTS, null, appt);

        ContentValues pay = new ContentValues();
        pay.put("appointment_id", historyId);
        pay.put("amount", 14500);
        pay.put("method", "CARD");
        pay.put("paid", 1);
        pay.put("paid_at", "2026-07-04 16:40");
        db.insert(T_PAYMENTS, null, pay);
    }

    private void insertUser(SQLiteDatabase db, String name, String email, String password, String phone, String role) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("password", password);
        v.put("phone", phone);
        v.put("role", role);
        db.insert(T_USERS, null, v);
    }

    private long insertBranch(SQLiteDatabase db, String name, String address, String city,
                              double lat, double lng, String phone) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("address", address);
        v.put("city", city);
        v.put("latitude", lat);
        v.put("longitude", lng);
        v.put("phone", phone);
        return db.insert(T_BRANCHES, null, v);
    }

    private void insertTech(SQLiteDatabase db, String name, long branchId, String specialty, int available) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("branch_id", branchId);
        v.put("specialty", specialty);
        v.put("available", available);
        db.insert(T_TECHNICIANS, null, v);
    }

    private long insertCategory(SQLiteDatabase db, String name, String description) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", description);
        return db.insert(T_CATEGORIES, null, v);
    }

    private void insertService(SQLiteDatabase db, long categoryId, String name, double price,
                               String description, String hint) {
        ContentValues v = new ContentValues();
        v.put("category_id", categoryId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", description);
        v.put("sample_image_hint", hint);
        db.insert(T_SERVICES, null, v);
    }

    private void insertPart(SQLiteDatabase db, String name, int qty, long branchId, long categoryId) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", qty);
        v.put("branch_id", branchId);
        v.put("category_id", categoryId);
        db.insert(T_PARTS, null, v);
    }
}
