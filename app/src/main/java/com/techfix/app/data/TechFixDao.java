package com.techfix.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.techfix.app.model.Appointment;
import com.techfix.app.model.Branch;
import com.techfix.app.model.Category;
import com.techfix.app.model.Payment;
import com.techfix.app.model.RepairImage;
import com.techfix.app.model.Service;
import com.techfix.app.model.SparePart;
import com.techfix.app.model.Technician;
import com.techfix.app.model.User;

import java.util.ArrayList;
import java.util.List;

public class TechFixDao {
    private final DatabaseHelper helper;

    public TechFixDao(Context context) {
        helper = new DatabaseHelper(context.getApplicationContext());
    }

    public User login(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT * FROM users WHERE email = ? AND password = ? LIMIT 1",
                new String[]{email.trim(), password});
        User user = null;
        if (c.moveToFirst()) {
            user = cursorToUser(c);
        }
        c.close();
        return user;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id FROM users WHERE email = ?", new String[]{email.trim()});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public long registerCustomer(String name, String email, String password, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email.trim());
        v.put("password", password);
        v.put("phone", phone);
        v.put("role", "CUSTOMER");
        return db.insert(DatabaseHelper.T_USERS, null, v);
    }

    public User getUser(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(id)});
        User user = null;
        if (c.moveToFirst()) {
            user = cursorToUser(c);
        }
        c.close();
        return user;
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM categories ORDER BY name", null);
        while (c.moveToNext()) {
            list.add(new Category(c.getLong(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("description"))));
        }
        c.close();
        return list;
    }

    public List<Service> searchServices(String query) {
        List<Service> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query.trim()) + "%";
        Cursor c = db.rawQuery(
                "SELECT s.*, cat.name AS category_name FROM services s "
                        + "JOIN categories cat ON cat.id = s.category_id "
                        + "WHERE s.name LIKE ? OR s.description LIKE ? OR cat.name LIKE ? "
                        + "ORDER BY cat.name, s.name",
                new String[]{q, q, q});
        while (c.moveToNext()) {
            list.add(cursorToService(c));
        }
        c.close();
        return list;
    }

    public Service getService(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT s.*, cat.name AS category_name FROM services s "
                        + "JOIN categories cat ON cat.id = s.category_id WHERE s.id = ?",
                new String[]{String.valueOf(id)});
        Service service = null;
        if (c.moveToFirst()) {
            service = cursorToService(c);
        }
        c.close();
        return service;
    }

    public List<Branch> getBranches() {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM branches ORDER BY city", null);
        while (c.moveToNext()) {
            list.add(cursorToBranch(c));
        }
        c.close();
        return list;
    }

    public Branch getBranch(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM branches WHERE id = ?", new String[]{String.valueOf(id)});
        Branch branch = null;
        if (c.moveToFirst()) {
            branch = cursorToBranch(c);
        }
        c.close();
        return branch;
    }

    /**
     * Branches that have at least one available technician and spare-part stock
     * for the given service category.
     */
    public List<Branch> getEligibleBranches(long categoryId) {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT b.* FROM branches b "
                        + "JOIN technicians t ON t.branch_id = b.id AND t.available = 1 "
                        + "JOIN spare_parts p ON p.branch_id = b.id AND p.category_id = ? AND p.quantity > 0 "
                        + "ORDER BY b.city",
                new String[]{String.valueOf(categoryId)});
        while (c.moveToNext()) {
            list.add(cursorToBranch(c));
        }
        c.close();
        return list;
    }

    public Technician getAvailableTechnician(long branchId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        // Randomize or pick least busy? For now just pick first available.
        Cursor c = db.rawQuery(
                "SELECT t.*, b.name AS branch_name FROM technicians t "
                        + "JOIN branches b ON b.id = t.branch_id "
                        + "WHERE t.branch_id = ? AND t.available = 1 ORDER BY RANDOM() LIMIT 1",
                new String[]{String.valueOf(branchId)});
        Technician tech = null;
        if (c.moveToFirst()) {
            tech = cursorToTechnician(c);
        }
        c.close();
        return tech;
    }

    public List<Technician> getTechniciansByBranch(long branchId, String query) {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query.trim()) + "%";
        Cursor c = db.rawQuery(
                "SELECT t.*, b.name AS branch_name FROM technicians t "
                        + "JOIN branches b ON b.id = t.branch_id "
                        + "WHERE t.branch_id = ? AND (t.name LIKE ? OR t.specialty LIKE ?) "
                        + "ORDER BY t.name", new String[]{String.valueOf(branchId), q, q});
        while (c.moveToNext()) {
            list.add(cursorToTechnician(c));
        }
        c.close();
        return list;
    }

    public List<Technician> getTechnicians(String query) {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query.trim()) + "%";
        Cursor c = db.rawQuery(
                "SELECT t.*, b.name AS branch_name FROM technicians t "
                        + "JOIN branches b ON b.id = t.branch_id "
                        + "WHERE t.name LIKE ? OR t.specialty LIKE ? OR b.name LIKE ? "
                        + "ORDER BY b.city, t.name", new String[]{q, q, q});
        while (c.moveToNext()) {
            list.add(cursorToTechnician(c));
        }
        c.close();
        return list;
    }

    public List<SparePart> getSpareParts() {
        List<SparePart> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT p.*, b.name AS branch_name, cat.name AS category_name "
                        + "FROM spare_parts p "
                        + "JOIN branches b ON b.id = p.branch_id "
                        + "JOIN categories cat ON cat.id = p.category_id "
                        + "ORDER BY b.city, p.name", null);
        while (c.moveToNext()) {
            SparePart p = new SparePart();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.name = c.getString(c.getColumnIndexOrThrow("name"));
            p.quantity = c.getInt(c.getColumnIndexOrThrow("quantity"));
            p.branchId = c.getLong(c.getColumnIndexOrThrow("branch_id"));
            p.categoryId = c.getLong(c.getColumnIndexOrThrow("category_id"));
            p.branchName = c.getString(c.getColumnIndexOrThrow("branch_name"));
            p.categoryName = c.getString(c.getColumnIndexOrThrow("category_name"));
            list.add(p);
        }
        c.close();
        return list;
    }

    public void updatePartQuantity(long partId, int quantity) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("quantity", Math.max(0, quantity));
        db.update(DatabaseHelper.T_PARTS, v, "id = ?", new String[]{String.valueOf(partId)});
    }

    public void updateBranch(long id, String name, String address, String city, double lat, double lng, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("address", address);
        v.put("city", city);
        v.put("latitude", lat);
        v.put("longitude", lng);
        v.put("phone", phone);
        db.update(DatabaseHelper.T_BRANCHES, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteBranch(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_BRANCHES, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateTechnician(long id, String name, long branchId, String specialty, int available) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("branch_id", branchId);
        v.put("specialty", specialty);
        v.put("available", available);
        db.update(DatabaseHelper.T_TECHNICIANS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTechnician(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_TECHNICIANS, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updateService(long id, long categoryId, String name, double price, String description) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("category_id", categoryId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", description);
        db.update(DatabaseHelper.T_SERVICES, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteService(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_SERVICES, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updatePart(long id, String name, int quantity, long branchId, long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", quantity);
        v.put("branch_id", branchId);
        v.put("category_id", categoryId);
        db.update(DatabaseHelper.T_PARTS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deletePart(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_PARTS, "id = ?", new String[]{String.valueOf(id)});
    }

    public long addCategory(String name, String description) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", description);
        return db.insert(DatabaseHelper.T_CATEGORIES, null, v);
    }

    public void updateCategory(long id, String name, String description) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", description);
        db.update(DatabaseHelper.T_CATEGORIES, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteCategory(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_CATEGORIES, "id = ?", new String[]{String.valueOf(id)});
    }

    public long addInventoryRequest(long staffId, long branchId, String itemName, int quantity, String reason) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("staff_id", staffId);
        v.put("branch_id", branchId);
        v.put("item_name", itemName);
        v.put("quantity", quantity);
        v.put("reason", reason);
        v.put("status", "PENDING");
        String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
        v.put("created_at", time);
        long id = db.insert(DatabaseHelper.T_INVENTORY_REQUESTS, null, v);
        
        // Notify Admin
        addNotification(1, 0, "New Stock Request", 
                "Staff requested " + quantity + "x " + itemName + " for branch ID: " + branchId, time);
        return id;
    }

    public List<java.util.Map<String, Object>> getInventoryRequests() {
        List<java.util.Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT r.*, u.name AS staff_name, b.name AS branch_name FROM inventory_requests r " +
                "JOIN users u ON u.id = r.staff_id " +
                "JOIN branches b ON b.id = r.branch_id ORDER BY r.id DESC", null);
        while (c.moveToNext()) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", c.getLong(c.getColumnIndexOrThrow("id")));
            map.put("staff_name", c.getString(c.getColumnIndexOrThrow("staff_name")));
            map.put("branch_name", c.getString(c.getColumnIndexOrThrow("branch_name")));
            map.put("item_name", c.getString(c.getColumnIndexOrThrow("item_name")));
            map.put("quantity", c.getInt(c.getColumnIndexOrThrow("quantity")));
            map.put("status", c.getString(c.getColumnIndexOrThrow("status")));
            map.put("created_at", c.getString(c.getColumnIndexOrThrow("created_at")));
            list.add(map);
        }
        c.close();
        return list;
    }

    public void updateInventoryRequestStatus(long id, String status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        db.update(DatabaseHelper.T_INVENTORY_REQUESTS, v, "id = ?", new String[]{String.valueOf(id)});

        // If approved, update spare parts quantity
        if ("APPROVED".equals(status)) {
            Cursor cReq = db.rawQuery("SELECT branch_id, item_name, quantity FROM inventory_requests WHERE id = ?", new String[]{String.valueOf(id)});
            if (cReq.moveToFirst()) {
                long bId = cReq.getLong(0);
                String name = cReq.getString(1);
                int qty = cReq.getInt(2);
                db.execSQL("UPDATE " + DatabaseHelper.T_PARTS + " SET quantity = quantity + ? WHERE branch_id = ? AND name = ?",
                        new Object[]{qty, bId, name});
            }
            cReq.close();
        }

        // Notify Staff
        Cursor c = db.rawQuery("SELECT staff_id, item_name FROM inventory_requests WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            long staffId = c.getLong(0);
            String item = c.getString(1);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
            addNotification(staffId, 0, "Stock Request Update", "Your request for " + item + " was " + status.toLowerCase(), time);
        }
        c.close();
    }

    public long addBranch(String name, String address, String city, double lat, double lng, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("address", address);
        v.put("city", city);
        v.put("latitude", lat);
        v.put("longitude", lng);
        v.put("phone", phone);
        return db.insert(DatabaseHelper.T_BRANCHES, null, v);
    }

    public long addTechnician(String name, long branchId, String specialty) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("branch_id", branchId);
        v.put("specialty", specialty);
        v.put("available", 1);
        return db.insert(DatabaseHelper.T_TECHNICIANS, null, v);
    }

    public long addService(long categoryId, String name, double price, String description) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("category_id", categoryId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", description);
        return db.insert(DatabaseHelper.T_SERVICES, null, v);
    }

    public long addPart(String name, int quantity, long branchId, long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", quantity);
        v.put("branch_id", branchId);
        v.put("category_id", categoryId);
        return db.insert(DatabaseHelper.T_PARTS, null, v);
    }

    public long insertAppointment(long customerId, long branchId, long technicianId, long serviceId,
                                  String deviceNote, String status, String createdAt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("customer_id", customerId);
        v.put("branch_id", branchId);
        v.put("technician_id", technicianId);
        v.put("service_id", serviceId);
        v.put("device_note", deviceNote);
        v.put("status", status);
        v.put("created_at", createdAt);
        long appointmentId = db.insert(DatabaseHelper.T_APPOINTMENTS, null, v);

        Service service = getService(serviceId);
        ContentValues pay = new ContentValues();
        pay.put("appointment_id", appointmentId);
        pay.put("amount", service == null ? 0 : service.price);
        pay.put("method", "CASH");
        pay.put("paid", 0);
        db.insert(DatabaseHelper.T_PAYMENTS, null, pay);

        // Notify Owner (Admin ID is 1)
        String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
        addNotification(1, appointmentId, "New Repair Request",
                "A new booking for " + (service != null ? service.name : "Repair") + " has been received. Technician: -",
                time);

        return appointmentId;
    }

    public List<Appointment> getAppointmentsForCustomer(long customerId, boolean historyOnly) {
        String extra = historyOnly
                ? " AND a.status IN ('COMPLETED','CANCELLED') "
                : " AND a.status NOT IN ('COMPLETED','CANCELLED') ";
        return queryAppointments("WHERE a.customer_id = ?" + extra + "ORDER BY a.id DESC",
                new String[]{String.valueOf(customerId)});
    }

    public List<Appointment> getAllAppointments(String query) {
        String q = "%" + (query == null ? "" : query.trim()) + "%";
        return queryAppointments("WHERE (u.name LIKE ? OR s.name LIKE ? OR a.status LIKE ?) "
                + "AND (a.status NOT IN ('COMPLETED') OR p.paid = 0) ORDER BY a.id DESC", new String[]{q, q, q});
    }

    public List<Appointment> getTechnicianAppointments(long userId, String query) {
        String q = "%" + (query == null ? "" : query.trim()) + "%";
        // Find technician name for this staff user
        String techName = "";
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cu = db.rawQuery("SELECT name FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cu.moveToFirst()) techName = cu.getString(0);
        cu.close();

        return queryAppointments("WHERE t.name = ? AND (u.name LIKE ? OR s.name LIKE ? OR a.status LIKE ?) ORDER BY a.id DESC",
                new String[]{techName, q, q, q});
    }

    public List<Appointment> getCompletedAppointments() {
        return queryAppointments("WHERE a.status = 'COMPLETED' AND p.paid = 1 ORDER BY a.id DESC", null);
    }

    public Appointment getAppointment(long id) {
        List<Appointment> list = queryAppointments("WHERE a.id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public void updatePassword(String email, String newPassword) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("password", newPassword);
        db.update(DatabaseHelper.T_USERS, v, "email = ?", new String[]{email.trim()});
    }

    public void updateProfile(long userId, String name, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("phone", phone);
        db.update(DatabaseHelper.T_USERS, v, "id = ?", new String[]{String.valueOf(userId)});
    }

    public List<User> getStaff() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT u.*, b.name AS branch_name FROM users u " +
                        "LEFT JOIN branches b ON b.id = u.branch_id " +
                        "WHERE u.role IN ('STAFF', 'ADMIN') ORDER BY u.name", null);
        while (c.moveToNext()) {
            User u = cursorToUser(c);
            int idx = c.getColumnIndex("branch_name");
            if (idx >= 0) u.branchName = c.getString(idx);
            list.add(u);
        }
        c.close();
        return list;
    }

    public long addUser(String name, String email, String password, String phone, String role, long branchId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email.trim());
        v.put("password", password);
        v.put("phone", phone);
        v.put("role", role);
        v.put("branch_id", branchId);
        return db.insert(DatabaseHelper.T_USERS, null, v);
    }

    public void updateUser(long id, String name, String email, String phone, String role, long branchId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email.trim());
        v.put("phone", phone);
        v.put("role", role);
        v.put("branch_id", branchId);
        db.update(DatabaseHelper.T_USERS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteUser(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_USERS, "id = ?", new String[]{String.valueOf(id)});
    }

    public void assignAppointment(long id, long branchId, long technicianId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("branch_id", branchId);
        v.put("technician_id", technicianId);
        v.put("status", "ASSIGNED");
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(id)});

        Appointment a = getAppointment(id);
        if (a != null) {
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
            
            // Notify customer
            addNotification(a.customerId, id, "Technician Assigned",
                    "A technician (" + a.technicianName + ") has been assigned to your " + a.serviceName + " repair.",
                    time);

            // Notify Technician (if they have a user account, otherwise we might skip or notify a generic tech user)
            // For now, let's assume we notify the technician user if we can find one by name or similar.
            // However, the tech table and user table are separate. 
            // In a better design, Technician would link to User. 
            // Since they are separate, we'll notify all STAFF/ADMIN about the assignment for now, 
            // or if we had a specific mapping, we'd use that.
            // For the requirement "technician receives request notification":
            // We'll search for a user with the same name as the technician to notify them.
            Cursor c = db.rawQuery("SELECT id FROM users WHERE name = ? AND role = 'STAFF' LIMIT 1", new String[]{a.technicianName});
            if (c.moveToFirst()) {
                addNotification(c.getLong(0), id, "New Job Assigned",
                        "You have been assigned to a new " + a.serviceName + " request for " + a.customerName + ".",
                        time);
            }
            c.close();
        }
    }

    public void addRepairImage(long appointmentId, String path, String caption) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("appointment_id", appointmentId);
        v.put("file_path", path);
        v.put("caption", caption);
        db.insert(DatabaseHelper.T_IMAGES, null, v);

        // Notify customer about new photo
        Appointment a = getAppointment(appointmentId);
        if (a != null) {
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
            addNotification(a.customerId, appointmentId, "Repair Photo Added",
                    "A new photo has been uploaded for your " + a.serviceName + " repair.",
                    time);
        }
    }

    public List<RepairImage> getImages(long appointmentId) {
        List<RepairImage> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM repair_images WHERE appointment_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(appointmentId)});
        while (c.moveToNext()) {
            RepairImage img = new RepairImage();
            img.id = c.getLong(c.getColumnIndexOrThrow("id"));
            img.appointmentId = appointmentId;
            img.filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
            img.caption = c.getString(c.getColumnIndexOrThrow("caption"));
            list.add(img);
        }
        c.close();
        return list;
    }

    public List<Payment> getPayments() {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT p.*, u.name AS customer_name, s.name AS service_name "
                        + "FROM payments p "
                        + "JOIN appointments a ON a.id = p.appointment_id "
                        + "JOIN users u ON u.id = a.customer_id "
                        + "JOIN services s ON s.id = a.service_id "
                        + "ORDER BY p.id DESC", null);
        while (c.moveToNext()) {
            Payment p = new Payment();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.appointmentId = c.getLong(c.getColumnIndexOrThrow("appointment_id"));
            p.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
            p.method = c.getString(c.getColumnIndexOrThrow("method"));
            p.paid = c.getInt(c.getColumnIndexOrThrow("paid"));
            p.paidAt = c.getString(c.getColumnIndexOrThrow("paid_at"));
            p.customerName = c.getString(c.getColumnIndexOrThrow("customer_name"));
            p.serviceName = c.getString(c.getColumnIndexOrThrow("service_name"));
            list.add(p);
        }
        c.close();
        return list;
    }

    public Payment getPaymentForAppointment(long appointmentId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM payments WHERE appointment_id = ? LIMIT 1",
                new String[]{String.valueOf(appointmentId)});
        Payment p = null;
        if (c.moveToFirst()) {
            p = new Payment();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.appointmentId = appointmentId;
            p.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
            p.method = c.getString(c.getColumnIndexOrThrow("method"));
            p.paid = c.getInt(c.getColumnIndexOrThrow("paid"));
            p.paidAt = c.getString(c.getColumnIndexOrThrow("paid_at"));
        }
        c.close();
        return p;
    }

    public void markPaid(long paymentId, String method, String paidAt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("paid", 1);
        v.put("method", method);
        v.put("paid_at", paidAt);
        db.update(DatabaseHelper.T_PAYMENTS, v, "id = ?", new String[]{String.valueOf(paymentId)});

        // Notify customer and owner
        SQLiteDatabase rdb = helper.getReadableDatabase();
        Cursor c = rdb.rawQuery("SELECT appointment_id FROM payments WHERE id = ?", new String[]{String.valueOf(paymentId)});
        if (c.moveToFirst()) {
            long apptId = c.getLong(0);
            Appointment a = getAppointment(apptId);
            if (a != null) {
                String msg = "Payment of " + com.techfix.app.ui.UiHelper.money(a.servicePrice) + " for " + a.serviceName + " has been confirmed.";
                addNotification(a.customerId, apptId, "Payment Confirmed", msg, paidAt);
                addNotification(1, apptId, "Payment Received", "Received " + msg + " from " + a.customerName + ".", paidAt);
            }
        }
        c.close();
    }

    public void updateAppointmentStatus(long id, String status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(id)});

        Appointment a = getAppointment(id);
        if (a != null) {
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
            String msg = "Your repair for " + a.serviceName + " is now " + status.replace("_", " ").toLowerCase() + ".";
            
            // Notify customer
            addNotification(a.customerId, id, "Repair Update", msg, time);
            
            // If COMPLETED, notify Owner as well
            if ("COMPLETED".equals(status)) {
                addNotification(1, id, "Task Completed", 
                        "Technician " + a.technicianName + " has completed the repair for " + a.customerName + ".", 
                        time);
            }
        }
    }

    public void addNotification(long userId, long appointmentId, String title, String message, String createdAt) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("appointment_id", appointmentId);
        v.put("title", title);
        v.put("message", message);
        v.put("created_at", createdAt);
        v.put("is_read", 0);
        db.insert(DatabaseHelper.T_NOTIFICATIONS, null, v);
    }

    public List<com.techfix.app.model.Notification> getNotifications(long userId) {
        List<com.techfix.app.model.Notification> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            com.techfix.app.model.Notification n = new com.techfix.app.model.Notification();
            n.id = c.getLong(c.getColumnIndexOrThrow("id"));
            n.userId = userId;
            n.appointmentId = c.isNull(c.getColumnIndexOrThrow("appointment_id")) 
                    ? 0 : c.getLong(c.getColumnIndexOrThrow("appointment_id"));
            n.title = c.getString(c.getColumnIndexOrThrow("title"));
            n.message = c.getString(c.getColumnIndexOrThrow("message"));
            n.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
            n.isRead = c.getInt(c.getColumnIndexOrThrow("is_read"));
            list.add(n);
        }
        c.close();
        return list;
    }

    public void markNotificationsRead(long userId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("is_read", 1);
        db.update(DatabaseHelper.T_NOTIFICATIONS, v, "user_id = ?", new String[]{String.valueOf(userId)});
    }

    public void markNotificationAsRead(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("is_read", 1);
        db.update(DatabaseHelper.T_NOTIFICATIONS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public java.util.Map<String, Object> getAdminStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status = 'PENDING'", null);
        if (c1.moveToFirst()) stats.put("pending_repairs", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED'", null);
        if (c2.moveToFirst()) stats.put("completed_repairs", c2.getInt(0));
        c2.close();
        
        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status = 'ASSIGNED'", null);
        if (c4.moveToFirst()) stats.put("ongoing_repairs", c4.getInt(0));
        c4.close();

        Cursor c3 = db.rawQuery("SELECT SUM(amount) FROM payments WHERE paid = 1", null);
        if (c3.moveToFirst()) stats.put("revenue", c3.getDouble(0));
        c3.close();

        return stats;
    }

    public java.util.Map<String, Object> getStaffStats(long userId) {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        
        // Find technician name for this staff user
        String techName = "";
        Cursor cu = db.rawQuery("SELECT name FROM users WHERE id = ?", new String[]{String.valueOf(userId)});
        if (cu.moveToFirst()) techName = cu.getString(0);
        cu.close();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM appointments a " +
                "LEFT JOIN technicians t ON t.id = a.technician_id " +
                "WHERE t.name = ? AND a.status NOT IN ('COMPLETED', 'CANCELLED')", new String[]{techName});
        if (c1.moveToFirst()) stats.put("assigned", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM appointments a " +
                "LEFT JOIN technicians t ON t.id = a.technician_id " +
                "WHERE t.name = ? AND a.status = 'COMPLETED'", new String[]{techName});
        if (c2.moveToFirst()) stats.put("completed", c2.getInt(0));
        c2.close();

        return stats;
    }

    private List<Appointment> queryAppointments(String whereOrder, String[] args) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT a.*, u.name AS customer_name, b.name AS branch_name, "
                        + "t.name AS technician_name, s.name AS service_name, s.price AS service_price, "
                        + "p.paid AS paid_status "
                        + "FROM appointments a "
                        + "JOIN users u ON u.id = a.customer_id "
                        + "LEFT JOIN branches b ON b.id = a.branch_id "
                        + "LEFT JOIN technicians t ON t.id = a.technician_id "
                        + "JOIN services s ON s.id = a.service_id "
                        + "LEFT JOIN payments p ON p.appointment_id = a.id "
                        + whereOrder, args);
        while (c.moveToNext()) {
            list.add(cursorToAppointment(c));
        }
        c.close();
        return list;
    }

    private User cursorToUser(Cursor c) {
        return new User(
                c.getLong(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("email")),
                c.getString(c.getColumnIndexOrThrow("password")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getString(c.getColumnIndexOrThrow("role")),
                c.isNull(c.getColumnIndexOrThrow("branch_id")) ? 0 : c.getLong(c.getColumnIndexOrThrow("branch_id")));
    }

    private Branch cursorToBranch(Cursor c) {
        Branch b = new Branch();
        b.id = c.getLong(c.getColumnIndexOrThrow("id"));
        b.name = c.getString(c.getColumnIndexOrThrow("name"));
        b.address = c.getString(c.getColumnIndexOrThrow("address"));
        b.city = c.getString(c.getColumnIndexOrThrow("city"));
        b.latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
        b.longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));
        b.phone = c.getString(c.getColumnIndexOrThrow("phone"));
        return b;
    }

    private Technician cursorToTechnician(Cursor c) {
        Technician t = new Technician();
        t.id = c.getLong(c.getColumnIndexOrThrow("id"));
        t.name = c.getString(c.getColumnIndexOrThrow("name"));
        t.branchId = c.getLong(c.getColumnIndexOrThrow("branch_id"));
        t.specialty = c.getString(c.getColumnIndexOrThrow("specialty"));
        t.available = c.getInt(c.getColumnIndexOrThrow("available"));
        int idx = c.getColumnIndex("branch_name");
        if (idx >= 0) {
            t.branchName = c.getString(idx);
        }
        return t;
    }

    private Service cursorToService(Cursor c) {
        Service s = new Service();
        s.id = c.getLong(c.getColumnIndexOrThrow("id"));
        s.categoryId = c.getLong(c.getColumnIndexOrThrow("category_id"));
        s.name = c.getString(c.getColumnIndexOrThrow("name"));
        s.price = c.getDouble(c.getColumnIndexOrThrow("price"));
        s.description = c.getString(c.getColumnIndexOrThrow("description"));
        s.sampleImageHint = c.getString(c.getColumnIndexOrThrow("sample_image_hint"));
        int idx = c.getColumnIndex("category_name");
        if (idx >= 0) {
            s.categoryName = c.getString(idx);
        }
        return s;
    }

    private Appointment cursorToAppointment(Cursor c) {
        Appointment a = new Appointment();
        a.id = c.getLong(c.getColumnIndexOrThrow("id"));
        a.customerId = c.getLong(c.getColumnIndexOrThrow("customer_id"));
        a.branchId = c.isNull(c.getColumnIndexOrThrow("branch_id"))
                ? 0 : c.getLong(c.getColumnIndexOrThrow("branch_id"));
        a.technicianId = c.isNull(c.getColumnIndexOrThrow("technician_id"))
                ? 0 : c.getLong(c.getColumnIndexOrThrow("technician_id"));
        a.serviceId = c.getLong(c.getColumnIndexOrThrow("service_id"));
        a.deviceNote = c.getString(c.getColumnIndexOrThrow("device_note"));
        a.status = c.getString(c.getColumnIndexOrThrow("status"));
        a.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
        a.customerName = c.getString(c.getColumnIndexOrThrow("customer_name"));
        a.branchName = c.getString(c.getColumnIndexOrThrow("branch_name"));
        a.technicianName = c.getString(c.getColumnIndexOrThrow("technician_name"));
        a.serviceName = c.getString(c.getColumnIndexOrThrow("service_name"));
        a.servicePrice = c.getDouble(c.getColumnIndexOrThrow("service_price"));
        int paidIdx = c.getColumnIndex("paid_status");
        if (paidIdx >= 0) {
            a.paid = c.getInt(paidIdx);
        }
        return a;
    }
}
