package com.techfix.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "techfix.db";
    public static final int DB_VERSION = 26;

    // Table Names
    public static final String T_USERS = "users";
    public static final String T_BRANCHES = "branches";
    public static final String T_TECHNICIANS = "technicians";
    public static final String T_CATEGORIES = "categories";
    public static final String T_SERVICES = "services";
    public static final String T_PARTS = "spare_parts";
    public static final String T_APPOINTMENTS = "appointments";
    public static final String T_IMAGES = "repair_images";
    public static final String T_PAYMENTS = "payments";
    public static final String T_NOTIFICATIONS = "notifications";
    public static final String T_INVENTORY_REQUESTS = "inventory_requests";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Branches
        db.execSQL("CREATE TABLE " + T_BRANCHES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "address TEXT,"
                + "city TEXT,"
                + "latitude REAL,"
                + "longitude REAL,"
                + "phone TEXT)");

        // 2. Users (Admin, Manager, Staff/Technician, Customer)
        db.execSQL("CREATE TABLE " + T_USERS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "email TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "phone TEXT,"
                + "role TEXT NOT NULL," // ADMIN, MANAGER, STAFF, CUSTOMER
                + "branch_id INTEGER," // Link to T_BRANCHES
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id))");

        // 3. Technicians (Sub-profile for STAFF role users)
        db.execSQL("CREATE TABLE " + T_TECHNICIANS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL UNIQUE,"
                + "branch_id INTEGER NOT NULL,"
                + "specialty_category_id INTEGER,"
                + "available INTEGER DEFAULT 1,"
                + "FOREIGN KEY(user_id) REFERENCES " + T_USERS + "(id),"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id),"
                + "FOREIGN KEY(specialty_category_id) REFERENCES " + T_CATEGORIES + "(id))");

        // 4. Categories
        db.execSQL("CREATE TABLE " + T_CATEGORIES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "description TEXT)");

        // 5. Services
        db.execSQL("CREATE TABLE " + T_SERVICES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "category_id INTEGER NOT NULL,"
                + "name TEXT NOT NULL,"
                + "price REAL NOT NULL,"
                + "description TEXT,"
                + "sample_image_hint TEXT,"
                + "FOREIGN KEY(category_id) REFERENCES " + T_CATEGORIES + "(id))");

        // 6. Spare Parts
        db.execSQL("CREATE TABLE " + T_PARTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "name TEXT NOT NULL,"
                + "quantity INTEGER NOT NULL,"
                + "branch_id INTEGER NOT NULL,"
                + "category_id INTEGER NOT NULL,"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id),"
                + "FOREIGN KEY(category_id) REFERENCES " + T_CATEGORIES + "(id))");

        // 7. Appointments
        db.execSQL("CREATE TABLE " + T_APPOINTMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "customer_id INTEGER NOT NULL,"
                + "branch_id INTEGER NOT NULL,"
                + "service_id INTEGER NOT NULL,"
                + "technician_id INTEGER," // ID from T_TECHNICIANS, assigned by Manager
                + "device_name TEXT,"
                + "issue_description TEXT,"
                + "device_photo TEXT," // Customer uploaded photo
                + "status TEXT NOT NULL," // PENDING, IN_PROGRESS, COMPLETED, CANCELLED
                + "created_at TEXT NOT NULL,"
                + "FOREIGN KEY(customer_id) REFERENCES " + T_USERS + "(id),"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id),"
                + "FOREIGN KEY(service_id) REFERENCES " + T_SERVICES + "(id),"
                + "FOREIGN KEY(technician_id) REFERENCES " + T_TECHNICIANS + "(id))");

        // 8. Repair Images (Progress/Completed photos by Staff)
        db.execSQL("CREATE TABLE " + T_IMAGES + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "appointment_id INTEGER NOT NULL,"
                + "file_path TEXT NOT NULL,"
                + "caption TEXT,"
                + "FOREIGN KEY(appointment_id) REFERENCES " + T_APPOINTMENTS + "(id))");

        // 9. Payments
        db.execSQL("CREATE TABLE " + T_PAYMENTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "appointment_id INTEGER NOT NULL,"
                + "amount REAL NOT NULL,"
                + "status TEXT NOT NULL," // PENDING, COMPLETED
                + "method TEXT," // CASH, CARD
                + "paid_at TEXT,"
                + "FOREIGN KEY(appointment_id) REFERENCES " + T_APPOINTMENTS + "(id))");

        // 10. Notifications
        db.execSQL("CREATE TABLE " + T_NOTIFICATIONS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "user_id INTEGER NOT NULL,"
                + "appointment_id INTEGER,"
                + "title TEXT,"
                + "message TEXT,"
                + "is_read INTEGER DEFAULT 0,"
                + "created_at TEXT,"
                + "FOREIGN KEY(user_id) REFERENCES " + T_USERS + "(id),"
                + "FOREIGN KEY(appointment_id) REFERENCES " + T_APPOINTMENTS + "(id))");

        // 11. Inventory/Stock Requests
        db.execSQL("CREATE TABLE " + T_INVENTORY_REQUESTS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "manager_id INTEGER NOT NULL,"
                + "branch_id INTEGER NOT NULL,"
                + "item_name TEXT NOT NULL,"
                + "quantity INTEGER NOT NULL,"
                + "status TEXT NOT NULL," // PENDING, APPROVED, REJECTED
                + "created_at TEXT NOT NULL,"
                + "FOREIGN KEY(manager_id) REFERENCES " + T_USERS + "(id),"
                + "FOREIGN KEY(branch_id) REFERENCES " + T_BRANCHES + "(id))");

        seedData(db);
    }

    private void seedData(SQLiteDatabase db) {
        // 1. BRANCHES (Colombo and Galle Only)
        long colomboId = insertBranch(db, "TechFix Colombo", "No 12, Galle Road, Colombo 03", "Colombo", 6.9271, 79.8612, "0112345678");
        long galleId = insertBranch(db, "TechFix Galle", "No 45, Matara Road, Galle", "Galle", 6.0535, 80.2210, "0912345678");

        // 2. ADMIN
        insertUser(db, "Aruna Perera", "admin@techfix.lk", "admin123", "0771112223", "ADMIN", 0);

        // 3. BRANCH MANAGERS
        long mColombo = insertUser(db, "Nimal Silva", "nimal.manager@techfix.lk", "manager123", "0772223334", "MANAGER", colomboId);
        long mGalle = insertUser(db, "Sunil Fernando", "sunil.manager@techfix.lk", "manager123", "0773334445", "MANAGER", galleId);

        // 4. CATEGORIES
        long catMobile = insertCategory(db, "Mobile Repair", "Smartphone and tablet repairs");
        long catLaptop = insertCategory(db, "Laptop Repair", "Notebook and desktop services");

        // 5. SERVICES
        insertService(db, catMobile, "Screen Replacement", 9500.0, "High-quality display repair for smartphones");
        insertService(db, catMobile, "Battery Swap", 4500.0, "Original battery installation");
        insertService(db, catLaptop, "Keyboard Repair", 6500.0, "Laptop key replacement and repair");
        insertService(db, catLaptop, "Windows/OS Install", 3000.0, "Clean OS setup and optimization");

        // 6. STAFF / TECHNICIANS (Representing both User and Technician profiles)
        // Colombo Staff
        long t1User = insertUser(db, "Kamal Gunasekara", "kamal.tech@techfix.lk", "tech123", "0714445556", "STAFF", colomboId);
        long t1 = insertTech(db, t1User, colomboId, (int)catMobile);
        
        long t2User = insertUser(db, "Punsiri Wickramasinghe", "punsiri.tech@techfix.lk", "tech123", "0715556667", "STAFF", colomboId);
        long t2 = insertTech(db, t2User, colomboId, (int)catLaptop);

        long t5User = insertUser(db, "Supun Abeysekara", "supun.tech@techfix.lk", "tech123", "0718889990", "STAFF", colomboId);
        insertTech(db, t5User, colomboId, (int)catMobile);

        long t6User = insertUser(db, "Ruwan Pathirana", "ruwan.tech@techfix.lk", "tech123", "0719990001", "STAFF", colomboId);
        insertTech(db, t6User, colomboId, (int)catLaptop);

        long t9User = insertUser(db, "Tharindu Perera", "tharindu.tech@techfix.lk", "tech123", "0710001112", "STAFF", colomboId);
        insertTech(db, t9User, colomboId, (int)catMobile);

        long t10User = insertUser(db, "Gayan Mendis", "gayan.tech@techfix.lk", "tech123", "0710002223", "STAFF", colomboId);
        insertTech(db, t10User, colomboId, (int)catLaptop);

        long t13User = insertUser(db, "Malith Perera", "malith.tech@techfix.lk", "tech123", "0710005556", "STAFF", colomboId);
        insertTech(db, t13User, colomboId, (int)catMobile);

        long t14User = insertUser(db, "Chathura Silva", "chathura.tech@techfix.lk", "tech123", "0710006667", "STAFF", colomboId);
        insertTech(db, t14User, colomboId, (int)catLaptop);

        // Galle Staff
        long t3User = insertUser(db, "Kasun Rajapaksha", "kasun.tech@techfix.lk", "tech123", "0716667778", "STAFF", galleId);
        long t3 = insertTech(db, t3User, galleId, (int)catMobile);

        long t4User = insertUser(db, "Nuwan Hettiarachchi", "nuwan.tech@techfix.lk", "tech123", "0717778889", "STAFF", galleId);
        long t4 = insertTech(db, t4User, galleId, (int)catLaptop);

        long t7User = insertUser(db, "Asanka Jayasuriya", "asanka.tech@techfix.lk", "tech123", "0712221110", "STAFF", galleId);
        insertTech(db, t7User, galleId, (int)catMobile);

        long t8User = insertUser(db, "Indika Silva", "indika.tech@techfix.lk", "tech123", "0713332221", "STAFF", galleId);
        insertTech(db, t8User, galleId, (int)catLaptop);

        long t11User = insertUser(db, "Saman Kumara", "saman.tech@techfix.lk", "tech123", "0710003334", "STAFF", galleId);
        insertTech(db, t11User, galleId, (int)catMobile);

        long t12User = insertUser(db, "Dinesh Fernando", "dinesh.tech@techfix.lk", "tech123", "0710004445", "STAFF", galleId);
        insertTech(db, t12User, galleId, (int)catLaptop);

        long t15User = insertUser(db, "Pathum Nissanka", "pathum.tech@techfix.lk", "tech123", "0710007778", "STAFF", galleId);
        insertTech(db, t15User, galleId, (int)catMobile);

        long t16User = insertUser(db, "Wanindu Hasaranga", "wanindu.tech@techfix.lk", "tech123", "0710008889", "STAFF", galleId);
        insertTech(db, t16User, galleId, (int)catLaptop);

        // 7. SPARE PARTS
        insertPart(db, "iPhone 13 Display", 10, colomboId, catMobile);
        insertPart(db, "Samsung S22 Battery", 15, colomboId, catMobile);
        insertPart(db, "Dell Inspiron Keyboard", 5, galleId, catLaptop);
        insertPart(db, "MacBook Battery", 3, galleId, catLaptop);

        // 8. CUSTOMERS
        long c1 = insertUser(db, "Dilini Perera", "dilini@gmail.com", "customer123", "0751112223", "CUSTOMER", 0);
        long c2 = insertUser(db, "Roshan Kumara", "roshan@gmail.com", "customer123", "0752223334", "CUSTOMER", 0);

        // 9. INITIAL APPOINTMENTS (Seed diverse statuses)
        // PENDING (Waiting for Manager assignment)
        long a1 = insertAppointment(db, c1, colomboId, 1, 0, "iPhone 13", "Cracked front glass", "", "PENDING", "2023-11-20 09:00");
        insertPayment(db, a1, 9500.0, "PENDING", "CASH");
        
        // IN_PROGRESS (Assigned to Kasun at Galle)
        long a2 = insertAppointment(db, c2, galleId, 2, t3, "Samsung S22", "Not holding charge", "", "IN_PROGRESS", "2023-11-21 10:30");
        insertPayment(db, a2, 4500.0, "PENDING", "CARD");
        
        // COMPLETED (Assigned to Punsiri at Colombo)
        long a3 = insertAppointment(db, c1, colomboId, 4, t2, "HP Laptop", "Software upgrade needed", "", "COMPLETED", "2023-11-15 14:00");
        insertPayment(db, a3, 3000.0, "COMPLETED", "CASH");

        // 10. STOCK REQUESTS
        insertInventoryRequest(db, mColombo, colomboId, "MacBook M1 Screens", 2, "PENDING", "2023-11-22 12:00");
        insertInventoryRequest(db, mGalle, galleId, "iPhone 14 Charging Ports", 5, "APPROVED", "2023-11-10 09:00");
    }

    private long insertUser(SQLiteDatabase db, String name, String email, String password, String phone, String role, long branchId) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("password", password);
        v.put("phone", phone);
        v.put("role", role);
        v.put("branch_id", branchId);
        return db.insert(T_USERS, null, v);
    }

    private long insertBranch(SQLiteDatabase db, String name, String address, String city, double lat, double lng, String phone) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("address", address);
        v.put("city", city);
        v.put("latitude", lat);
        v.put("longitude", lng);
        v.put("phone", phone);
        return db.insert(T_BRANCHES, null, v);
    }

    private long insertTech(SQLiteDatabase db, long userId, long branchId, int specialtyCatId) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("branch_id", branchId);
        v.put("specialty_category_id", specialtyCatId);
        v.put("available", 1);
        return db.insert(T_TECHNICIANS, null, v);
    }

    private long insertCategory(SQLiteDatabase db, String name, String desc) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        return db.insert(T_CATEGORIES, null, v);
    }

    private void insertService(SQLiteDatabase db, long catId, String name, double price, String desc) {
        ContentValues v = new ContentValues();
        v.put("category_id", catId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", desc);
        db.insert(T_SERVICES, null, v);
    }

    private void insertPart(SQLiteDatabase db, String name, int qty, long branchId, long catId) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", qty);
        v.put("branch_id", branchId);
        v.put("category_id", catId);
        db.insert(T_PARTS, null, v);
    }

    private long insertAppointment(SQLiteDatabase db, long customerId, long branchId, long serviceId, long techId, String device, String issue, String photo, String status, String date) {
        ContentValues v = new ContentValues();
        v.put("customer_id", customerId);
        v.put("branch_id", branchId);
        v.put("service_id", serviceId);
        if (techId > 0) v.put("technician_id", techId);
        v.put("device_name", device);
        v.put("issue_description", issue);
        v.put("device_photo", photo);
        v.put("status", status);
        v.put("created_at", date);
        return db.insert(T_APPOINTMENTS, null, v);
    }

    private void insertPayment(SQLiteDatabase db, long apptId, double amount, String status, String method) {
        ContentValues v = new ContentValues();
        v.put("appointment_id", apptId);
        v.put("amount", amount);
        v.put("status", status);
        v.put("method", method);
        db.insert(T_PAYMENTS, null, v);
    }

    private void insertInventoryRequest(SQLiteDatabase db, long managerId, long branchId, String item, int qty, String status, String date) {
        ContentValues v = new ContentValues();
        v.put("manager_id", managerId);
        v.put("branch_id", branchId);
        v.put("item_name", item);
        v.put("quantity", qty);
        v.put("status", status);
        v.put("created_at", date);
        db.insert(T_INVENTORY_REQUESTS, null, v);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_INVENTORY_REQUESTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PAYMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_IMAGES);
        db.execSQL("DROP TABLE IF EXISTS " + T_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_PARTS);
        db.execSQL("DROP TABLE IF EXISTS " + T_SERVICES);
        db.execSQL("DROP TABLE IF EXISTS " + T_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + T_TECHNICIANS);
        db.execSQL("DROP TABLE IF EXISTS " + T_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + T_BRANCHES);
        onCreate(db);
    }
}
