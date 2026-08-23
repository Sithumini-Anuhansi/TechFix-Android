package com.techfix.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.techfix.app.model.Appointment;
import com.techfix.app.model.Branch;
import com.techfix.app.model.Category;
import com.techfix.app.model.Notification;
import com.techfix.app.model.Payment;
import com.techfix.app.model.RepairImage;
import com.techfix.app.model.Service;
import com.techfix.app.model.SparePart;
import com.techfix.app.model.Technician;
import com.techfix.app.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TechFixDao {
    private final DatabaseHelper helper;

    public TechFixDao(Context context) {
        helper = new DatabaseHelper(context);
    }

    public User login(String email, String password) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE email = ? AND password = ?", new String[]{email, password});
        if (c.moveToFirst()) {
            User u = cursorToUser(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT 1 FROM users WHERE email = ?", new String[]{email});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public long registerCustomer(String name, String email, String password, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("password", password);
        v.put("phone", phone);
        v.put("role", "CUSTOMER");
        return db.insert(DatabaseHelper.T_USERS, null, v);
    }

    public User getUser(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM users WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            User u = cursorToUser(c);
            c.close();
            return u;
        }
        c.close();
        return null;
    }

    public List<Category> getCategories() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM categories", null);
        while (c.moveToNext()) {
            Category cat = new Category();
            cat.id = c.getLong(c.getColumnIndexOrThrow("id"));
            cat.name = c.getString(c.getColumnIndexOrThrow("name"));
            cat.description = c.getString(c.getColumnIndexOrThrow("description"));
            list.add(cat);
        }
        c.close();
        return list;
    }

    public List<Service> searchServices(String query) {
        List<Service> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query) + "%";
        Cursor c = db.rawQuery("SELECT s.*, c.name AS category_name FROM services s " +
                "JOIN categories c ON c.id = s.category_id " +
                "WHERE s.name LIKE ? OR c.name LIKE ?", new String[]{q, q});
        while (c.moveToNext()) {
            list.add(cursorToService(c));
        }
        c.close();
        return list;
    }

    public Service getService(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT s.*, c.name AS category_name FROM services s " +
                "JOIN categories c ON c.id = s.category_id WHERE s.id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            Service s = cursorToService(c);
            c.close();
            return s;
        }
        c.close();
        return null;
    }

    public List<Branch> getBranches() {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM branches", null);
        while (c.moveToNext()) {
            list.add(cursorToBranch(c));
        }
        c.close();
        return list;
    }

    public Branch getBranch(long id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM branches WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            Branch b = cursorToBranch(c);
            c.close();
            return b;
        }
        c.close();
        return null;
    }

    public List<Branch> getEligibleBranches(long categoryId) {
        List<Branch> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        // A branch is eligible if it has at least one technician specializing in this category and enough spare parts
        Cursor c = db.rawQuery("SELECT DISTINCT b.* FROM branches b " +
                "JOIN technicians t ON t.branch_id = b.id " +
                "JOIN spare_parts p ON p.branch_id = b.id " +
                "WHERE p.category_id = ? AND p.quantity > 0 " +
                "AND t.specialty_category_id = ? AND t.available = 1", 
                new String[]{String.valueOf(categoryId), String.valueOf(categoryId)});
        while (c.moveToNext()) {
            list.add(cursorToBranch(c));
        }
        c.close();
        return list;
    }

    public Technician getAvailableTechnician(long branchId, long categoryId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT t.*, u.name FROM technicians t " +
                "JOIN users u ON t.user_id = u.id " +
                "WHERE t.branch_id = ? AND t.specialty_category_id = ? AND t.available = 1 LIMIT 1",
                new String[]{String.valueOf(branchId), String.valueOf(categoryId)});
        if (c.moveToFirst()) {
            Technician t = cursorToTechnician(c);
            c.close();
            return t;
        }
        c.close();
        return null;
    }

    public List<Technician> getTechniciansByBranch(long branchId, String query, Long categoryId) {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query) + "%";
        
        StringBuilder sql = new StringBuilder("SELECT t.*, u.name, b.name AS branch_name FROM technicians t ")
                .append("JOIN users u ON t.user_id = u.id ")
                .append("JOIN branches b ON b.id = t.branch_id ")
                .append("WHERE t.branch_id = ? AND u.name LIKE ? ");
        
        List<String> args = new ArrayList<>();
        args.add(String.valueOf(branchId));
        args.add(q);
        
        if (categoryId != null && categoryId > 0) {
            sql.append("AND t.specialty_category_id = ? ");
            args.add(String.valueOf(categoryId));
        }
        
        Cursor c = db.rawQuery(sql.toString(), args.toArray(new String[0]));
        while (c.moveToNext()) {
            list.add(cursorToTechnician(c));
        }
        c.close();
        return list;
    }

    public List<Technician> getTechnicians(String query) {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String q = "%" + (query == null ? "" : query) + "%";
        Cursor c = db.rawQuery("SELECT t.*, u.name, b.name AS branch_name FROM technicians t " +
                "JOIN users u ON t.user_id = u.id " +
                "JOIN branches b ON b.id = t.branch_id " +
                "WHERE u.name LIKE ?", new String[]{q});
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
                        + "WHERE p.branch_id > 0 "
                        + "ORDER BY b.name, p.name", null);
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

    public void updatePartQuantity(long id, int newQty) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("quantity", newQty);
        db.update(DatabaseHelper.T_PARTS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void updatePart(long id, String name, int qty, long branchId, long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", qty);
        v.put("branch_id", branchId);
        v.put("category_id", categoryId);
        db.update(DatabaseHelper.T_PARTS, v, "id = ?", new String[]{String.valueOf(id)});
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

    public void updateTechnician(long id, long branchId, long specialtyCategoryId, int available) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("branch_id", branchId);
        v.put("specialty_category_id", specialtyCategoryId);
        v.put("available", available);
        db.update(DatabaseHelper.T_TECHNICIANS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteTechnician(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_TECHNICIANS, "id = ?", new String[]{String.valueOf(id)});
    }

        public void updateService(long id, long catId, String name, double price, String desc, String hint) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("category_id", catId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", desc);
        v.put("sample_image_hint", hint);
        db.update(DatabaseHelper.T_SERVICES, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteService(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_SERVICES, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<SparePart> getGlobalSpareParts() {
        List<SparePart> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COALESCE(MAX(CASE WHEN p.branch_id = 0 THEN p.id END), MIN(p.id)) as id, " +
                        "p.name, p.category_id, cat.name AS category_name, SUM(p.quantity) as total_qty " +
                        "FROM spare_parts p " +
                        "JOIN categories cat ON cat.id = p.category_id " +
                        "GROUP BY p.name, p.category_id " +
                        "ORDER BY p.name", null);
        while (c.moveToNext()) {
            SparePart p = new SparePart();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.name = c.getString(c.getColumnIndexOrThrow("name"));
            p.quantity = c.getInt(c.getColumnIndexOrThrow("total_qty"));
            p.branchId = 0;
            p.categoryId = c.getLong(c.getColumnIndexOrThrow("category_id"));
            p.categoryName = c.getString(c.getColumnIndexOrThrow("category_name"));
            list.add(p);
        }
        c.close();
        return list;
    }

    public long addGlobalPart(long categoryId, String name) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", 0);
        v.put("branch_id", 0);
        v.put("category_id", categoryId);
        return db.insert(DatabaseHelper.T_PARTS, null, v);
    }

    public void updateGlobalPart(long id, long catId, String name) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("category_id", catId);
        v.put("name", name);
        db.update(DatabaseHelper.T_PARTS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deletePart(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_PARTS, "id = ?", new String[]{String.valueOf(id)});
    }

    public long addCategory(String name, String desc) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        return db.insert(DatabaseHelper.T_CATEGORIES, null, v);
    }

    public void updateCategory(long id, String name, String desc) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("description", desc);
        db.update(DatabaseHelper.T_CATEGORIES, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteCategory(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_CATEGORIES, "id = ?", new String[]{String.valueOf(id)});
    }

    public long addInventoryRequest(long managerId, long branchId, String item, int qty, String reason) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("manager_id", managerId);
        v.put("branch_id", branchId);
        v.put("item_name", item);
        v.put("quantity", qty);
        v.put("status", "PENDING");
        v.put("created_at", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date()));
        long id = db.insert(DatabaseHelper.T_INVENTORY_REQUESTS, null, v);

        // Notify Admin
        Cursor c = db.rawQuery("SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1", null);
        if (c.moveToFirst()) {
            addNotification(c.getLong(0), 0, "New Stock Request", "A new request for " + qty + "x " + item + " has been submitted", v.getAsString("created_at"));
        }
        c.close();
        return id;
    }

    public List<Map<String, Object>> getInventoryRequests(long branchId) {
        List<Map<String, Object>> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT r.*, b.name AS branch_name, u.name AS manager_name FROM inventory_requests r " +
                "JOIN branches b ON b.id = r.branch_id " +
                "JOIN users u ON u.id = r.manager_id";
        String[] args = null;
        if (branchId > 0) {
            sql += " WHERE r.branch_id = ?";
            args = new String[]{String.valueOf(branchId)};
        }
        sql += " ORDER BY r.id DESC";
        Cursor c = db.rawQuery(sql, args);
        while (c.moveToNext()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getLong(c.getColumnIndexOrThrow("id")));
            map.put("branch_id", c.getLong(c.getColumnIndexOrThrow("branch_id")));
            map.put("branch_name", c.getString(c.getColumnIndexOrThrow("branch_name")));
            map.put("manager_name", c.getString(c.getColumnIndexOrThrow("manager_name")));
            map.put("item_name", c.getString(c.getColumnIndexOrThrow("item_name")));
            map.put("quantity", c.getInt(c.getColumnIndexOrThrow("quantity")));
            map.put("status", c.getString(c.getColumnIndexOrThrow("status")));
            map.put("created_at", c.getString(c.getColumnIndexOrThrow("created_at")));
            list.add(map);
        }
        c.close();
        return list;
    }

    public List<Map<String, Object>> getInventoryRequests() {
        return getInventoryRequests(0);
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
                int qtyToAdd = cReq.getInt(2);
                
                // Try to find existing record in this branch
                Cursor cExist = db.rawQuery("SELECT id, quantity FROM spare_parts WHERE branch_id = ? AND name = ?", new String[]{String.valueOf(bId), name});
                if (cExist.moveToFirst()) {
                    long partId = cExist.getLong(0);
                    int currentQty = cExist.getInt(1);
                    ContentValues vUpd = new ContentValues();
                    vUpd.put("quantity", currentQty + qtyToAdd);
                    db.update(DatabaseHelper.T_PARTS, vUpd, "id = ?", new String[]{String.valueOf(partId)});
                } else {
                    // Not found, we need a category_id to insert. Let's find it from global parts
                    long catId = 1; // Default
                    Cursor cCat = db.rawQuery("SELECT category_id FROM spare_parts WHERE name = ? AND branch_id = 0 LIMIT 1", new String[]{name});
                    if (cCat.moveToFirst()) catId = cCat.getLong(0);
                    cCat.close();
                    
                    ContentValues vPart = new ContentValues();
                    vPart.put("name", name);
                    vPart.put("quantity", qtyToAdd);
                    vPart.put("branch_id", bId);
                    vPart.put("category_id", catId);
                    db.insert(DatabaseHelper.T_PARTS, null, vPart);
                }
                cExist.close();
            }
            cReq.close();
        }

        // Notify Manager
        Cursor c = db.rawQuery("SELECT manager_id, branch_id, item_name FROM inventory_requests WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            long managerId = c.getLong(0);
            String item = c.getString(2);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date());

            addNotification(managerId, 0, "Stock Request Update", "Your request for " + item + " was " + status.toLowerCase(), time);
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

    public long addService(long categoryId, String name, double price, String desc, String hint) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("category_id", categoryId);
        v.put("name", name);
        v.put("price", price);
        v.put("description", desc);
        v.put("sample_image_hint", hint);
        return db.insert(DatabaseHelper.T_SERVICES, null, v);
    }

    public long addPart(String name, int qty, long branchId, long categoryId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("quantity", qty);
        v.put("branch_id", branchId);
        v.put("category_id", categoryId);
        return db.insert(DatabaseHelper.T_PARTS, null, v);
    }

    public long insertAppointment(long customerId, long branchId, long technicianId, long serviceId, String deviceName, String issue, String photo, String status, String time) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("customer_id", customerId);
        v.put("branch_id", branchId > 0 ? branchId : null);
        v.put("technician_id", technicianId > 0 ? technicianId : null);
        v.put("service_id", serviceId);
        v.put("device_name", deviceName);
        v.put("issue_description", issue);
        v.put("device_photo", photo);
        v.put("status", status);
        v.put("created_at", time);
        long id = db.insert(DatabaseHelper.T_APPOINTMENTS, null, v);

        // Initial Payment record
        Service s = getService(serviceId);
        if (s != null) {
            ContentValues p = new ContentValues();
            p.put("appointment_id", id);
            p.put("amount", s.price);
            p.put("status", "PENDING");
            db.insert(DatabaseHelper.T_PAYMENTS, null, p);
        }

        // Notify Branch Manager if branch is assigned
        if (branchId > 0) {
            Cursor c = db.rawQuery("SELECT id FROM users WHERE branch_id = ? AND (role = 'MANAGER' OR role = 'BRANCH_MANAGER') LIMIT 1", new String[]{String.valueOf(branchId)});
            if (c.moveToFirst()) {
                addNotification(c.getLong(0), id, "New Repair Assigned", "New request for " + (s != null ? s.name : "repair"), time);
            }
            c.close();
        }

        return id;
    }

    public List<Appointment> getAppointmentsForCustomer(long customerId, boolean onlyActive) {
        String where = "WHERE a.customer_id = " + customerId;
        if (onlyActive) {
            where += " AND a.status NOT IN ('COMPLETED', 'CANCELLED')";
        } else {
            where += " AND a.status IN ('COMPLETED', 'CANCELLED')";
        }
        return queryAppointments(where + " ORDER BY a.id DESC", null);
    }

    public List<Appointment> getAllAppointments(String status, long branchId) {
        String where = (status == null || status.equals("ALL")) ? "" : "WHERE a.status = '" + status + "'";
        if (branchId > 0) {
            if (where.isEmpty()) where = "WHERE a.branch_id = " + branchId;
            else where += " AND a.branch_id = " + branchId;
        }
        return queryAppointments(where + " ORDER BY a.id DESC", null);
    }

    public List<Appointment> getTechnicianAppointments(long staffUserId, String status) {
        // Find technician ID for this staff user
        long techId = 0;
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cu = db.rawQuery("SELECT id FROM technicians WHERE user_id = ?", new String[]{String.valueOf(staffUserId)});
        if (cu.moveToFirst()) techId = cu.getLong(0);
        cu.close();

        String where = "WHERE a.technician_id = ?";
        if (!status.equals("ALL")) where += " AND a.status = '" + status + "'";
        return queryAppointments(where + " ORDER BY a.id DESC", new String[]{String.valueOf(techId)});
    }

    public List<Appointment> getCompletedAppointments() {
        return queryAppointments("WHERE a.status = 'COMPLETED' ORDER BY a.id DESC", null);
    }

    public Appointment getAppointment(long id) {
        List<Appointment> list = queryAppointments("WHERE a.id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public void updatePassword(String email, String newPass) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("password", newPass);
        db.update(DatabaseHelper.T_USERS, v, "email = ?", new String[]{email});
    }

    public void updateProfile(long id, String name, String phone) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("phone", phone);
        db.update(DatabaseHelper.T_USERS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public List<User> getStaff() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT u.*, b.name as branch_name FROM users u LEFT JOIN branches b ON u.branch_id = b.id WHERE u.role IN ('STAFF', 'MANAGER')", null);
        while (c.moveToNext()) {
            User user = cursorToUser(c);
            user.branchName = c.getString(c.getColumnIndexOrThrow("branch_name"));
            list.add(user);
        }
        c.close();
        return list;
    }

    public long addUser(String name, String email, String pass, String phone, String role, long branchId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("password", pass);
        v.put("phone", phone);
        v.put("role", role);
        v.put("branch_id", branchId);
        return db.insert(DatabaseHelper.T_USERS, null, v);
    }

    public void updateUser(long id, String name, String email, String phone, String role, long branchId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("email", email);
        v.put("phone", phone);
        v.put("role", role);
        v.put("branch_id", branchId);
        db.update(DatabaseHelper.T_USERS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteUser(long id) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DatabaseHelper.T_USERS, "id = ?", new String[]{String.valueOf(id)});
    }

    public void assignAppointment(long apptId, long branchId, long techId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("branch_id", branchId);
        v.put("technician_id", techId);
        v.put("status", "ASSIGNED");
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(apptId)});

        // Notify Technician (based on user_id)
        Cursor cTech = db.rawQuery("SELECT user_id FROM technicians WHERE id = ?", new String[]{String.valueOf(techId)});
        if (cTech.moveToFirst()) {
            long techUserId = cTech.getLong(0);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date());
            addNotification(techUserId, apptId, "New Repair Assigned", "You have been assigned a new repair task.", time);
        }
        cTech.close();

        // Notify Customer
        Cursor c = db.rawQuery("SELECT customer_id FROM appointments WHERE id = ?", new String[]{String.valueOf(apptId)});
        if (c.moveToFirst()) {
            long custId = c.getLong(0);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date());
            addNotification(custId, apptId, "Technician Assigned", "A technician has been assigned to your repair.", time);
        }
        c.close();
    }

    public void addRepairImage(long apptId, String path, String caption) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("appointment_id", apptId);
        v.put("file_path", path);
        v.put("caption", caption);
        db.insert(DatabaseHelper.T_IMAGES, null, v);

        // Notify Customer
        Cursor c = db.rawQuery("SELECT customer_id FROM appointments WHERE id = ?", new String[]{String.valueOf(apptId)});
        if (c.moveToFirst()) {
            long custId = c.getLong(0);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date());
            addNotification(custId, apptId, "Repair Update", "A new photo has been added to your repair progress.", time);
        }
        c.close();
    }

    public List<RepairImage> getImages(long apptId) {
        List<RepairImage> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM repair_images WHERE appointment_id = ?", new String[]{String.valueOf(apptId)});
        while (c.moveToNext()) {
            RepairImage img = new RepairImage();
            img.id = c.getLong(c.getColumnIndexOrThrow("id"));
            img.appointmentId = c.getLong(c.getColumnIndexOrThrow("appointment_id"));
            img.filePath = c.getString(c.getColumnIndexOrThrow("file_path"));
            img.caption = c.getString(c.getColumnIndexOrThrow("caption"));
            list.add(img);
        }
        c.close();
        return list;
    }

    public List<Payment> getPayments(long branchId) {
        List<Payment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sql = "SELECT p.*, u.name AS customer_name, s.name AS service_name, b.name AS branch_name " +
                "FROM payments p " +
                "JOIN appointments a ON a.id = p.appointment_id " +
                "JOIN users u ON u.id = a.customer_id " +
                "JOIN services s ON s.id = a.service_id " +
                "LEFT JOIN branches b ON b.id = a.branch_id ";
        
        String[] args = null;
        if (branchId > 0) {
            sql += " WHERE a.branch_id = ?";
            args = new String[]{String.valueOf(branchId)};
        }
        
        sql += " ORDER BY p.id DESC";
        
        Cursor c = db.rawQuery(sql, args);
        while (c.moveToNext()) {
            Payment p = new Payment();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.appointmentId = c.getLong(c.getColumnIndexOrThrow("appointment_id"));
            p.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
            p.method = c.getString(c.getColumnIndexOrThrow("method"));
            p.status = c.getString(c.getColumnIndexOrThrow("status"));
            p.paidAt = c.getString(c.getColumnIndexOrThrow("paid_at"));
            p.customerName = c.getString(c.getColumnIndexOrThrow("customer_name"));
            p.serviceName = c.getString(c.getColumnIndexOrThrow("service_name"));
            p.branchName = c.getString(c.getColumnIndexOrThrow("branch_name"));
            list.add(p);
        }
        c.close();
        return list;
    }

    public Payment getPaymentForAppointment(long apptId) {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM payments WHERE appointment_id = ?", new String[]{String.valueOf(apptId)});
        if (c.moveToFirst()) {
            Payment p = new Payment();
            p.id = c.getLong(c.getColumnIndexOrThrow("id"));
            p.appointmentId = c.getLong(c.getColumnIndexOrThrow("appointment_id"));
            p.amount = c.getDouble(c.getColumnIndexOrThrow("amount"));
            p.method = c.getString(c.getColumnIndexOrThrow("method"));
            p.status = c.getString(c.getColumnIndexOrThrow("status"));
            p.paidAt = c.getString(c.getColumnIndexOrThrow("paid_at"));
            c.close();
            return p;
        }
        c.close();
        return null;
    }

    public void markPaid(long apptId, String method, String time) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("method", method);
        v.put("status", "COMPLETED");
        v.put("paid_at", time);
        db.update(DatabaseHelper.T_PAYMENTS, v, "appointment_id = ?", new String[]{String.valueOf(apptId)});

        // Notify Admin and Customer
        Cursor c = db.rawQuery("SELECT u.name, s.name, p.amount, a.customer_id FROM appointments a " +
                "JOIN users u ON u.id = a.customer_id " +
                "JOIN services s ON s.id = a.service_id " +
                "JOIN payments p ON p.appointment_id = a.id " +
                "WHERE a.id = ?", new String[]{String.valueOf(apptId)});
        if (c.moveToFirst()) {
            String name = c.getString(0);
            String service = c.getString(1);
            double amount = c.getDouble(2);
            long custId = c.getLong(3);
            
            Cursor cAdmin = db.rawQuery("SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1", null);
            if (cAdmin.moveToFirst()) {
                addNotification(cAdmin.getLong(0), apptId, "Payment Received", "Received " + amount + " from " + name + " for " + service, time);
            }
            cAdmin.close();

            addNotification(custId, apptId, "Payment Confirmed", "Your payment of LKR " + amount + " for " + service + " has been confirmed.", time);
        }
        c.close();
    }

    public void updateAppointmentStatus(long id, String status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(id)});

        // Notify Customer
        Cursor c = db.rawQuery("SELECT customer_id FROM appointments WHERE id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) {
            long custId = c.getLong(0);
            String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new java.util.Date());
            addNotification(custId, id, "Repair Status Update", "Your repair status is now: " + status, time);
        }
        c.close();
    }

    public void addNotification(long userId, long apptId, String title, String msg, String time) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("appointment_id", apptId > 0 ? apptId : null);
        v.put("title", title);
        v.put("message", msg);
        v.put("created_at", time);
        v.put("is_read", 0);
        db.insert(DatabaseHelper.T_NOTIFICATIONS, null, v);
    }

    public List<Notification> getNotifications(long userId) {
        List<Notification> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM notifications WHERE user_id = ? ORDER BY id DESC", new String[]{String.valueOf(userId)});
        while (c.moveToNext()) {
            Notification n = new Notification();
            n.id = c.getLong(c.getColumnIndexOrThrow("id"));
            n.userId = c.getLong(c.getColumnIndexOrThrow("user_id"));
            n.appointmentId = c.isNull(c.getColumnIndexOrThrow("appointment_id")) ? 0 : c.getLong(c.getColumnIndexOrThrow("appointment_id"));
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

    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status = 'PENDING'", null);
        if (c1.moveToFirst()) stats.put("pending_repairs", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status = 'COMPLETED'", null);
        if (c2.moveToFirst()) stats.put("completed_repairs", c2.getInt(0));
        c2.close();
        
        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE status IN ('ASSIGNED', 'IN_PROGRESS')", null);
        if (c4.moveToFirst()) stats.put("ongoing_repairs", c4.getInt(0));
        c4.close();

        Cursor c3 = db.rawQuery("SELECT SUM(amount) FROM payments WHERE status = 'COMPLETED'", null);
        if (c3.moveToFirst()) stats.put("revenue", c3.getDouble(0));
        c3.close();

        Cursor c5 = db.rawQuery("SELECT COUNT(*) FROM inventory_requests WHERE status = 'PENDING'", null);
        if (c5.moveToFirst()) stats.put("pending_stock", c5.getInt(0));
        c5.close();

        Cursor c6 = db.rawQuery("SELECT SUM(amount) FROM payments WHERE status = 'COMPLETED' AND date(paid_at) = date('now')", null);
        if (c6.moveToFirst()) stats.put("daily_revenue", c6.getDouble(0));
        c6.close();

        return stats;
    }

    public Map<String, Object> getStaffStats(long userId) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        
        // Find technician ID for this staff user
        long techId = 0;
        Cursor cu = db.rawQuery("SELECT id FROM technicians WHERE user_id = ?", new String[]{String.valueOf(userId)});
        if (cu.moveToFirst()) techId = cu.getLong(0);
        cu.close();

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE technician_id = ?", new String[]{String.valueOf(techId)});
        if (c1.moveToFirst()) stats.put("total", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE technician_id = ? AND status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')", new String[]{String.valueOf(techId)});
        if (c2.moveToFirst()) stats.put("pending", c2.getInt(0));
        c2.close();

        Cursor c3 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE technician_id = ? AND status = 'COMPLETED'", new String[]{String.valueOf(techId)});
        if (c3.moveToFirst()) stats.put("completed", c3.getInt(0));
        c3.close();

        return stats;
    }

    public Map<String, Object> getBranchManagerStats(long branchId) {
        Map<String, Object> stats = new HashMap<>();
        SQLiteDatabase db = helper.getReadableDatabase();

        String[] branchArg = new String[]{String.valueOf(branchId)};

        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE branch_id = ? AND status = 'PENDING'", branchArg);
        if (c1.moveToFirst()) stats.put("pending_repairs", c1.getInt(0));
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE branch_id = ? AND status = 'COMPLETED'", branchArg);
        if (c2.moveToFirst()) stats.put("completed_repairs", c2.getInt(0));
        c2.close();

        Cursor c4 = db.rawQuery("SELECT COUNT(*) FROM appointments WHERE branch_id = ? AND status IN ('ASSIGNED', 'IN_PROGRESS')", branchArg);
        if (c4.moveToFirst()) stats.put("ongoing_repairs", c4.getInt(0));
        c4.close();

        Cursor c3 = db.rawQuery("SELECT SUM(amount) FROM payments p JOIN appointments a ON p.appointment_id = a.id WHERE a.branch_id = ? AND p.status = 'COMPLETED'", branchArg);
        if (c3.moveToFirst()) stats.put("revenue", c3.getDouble(0));
        c3.close();

        Cursor c5 = db.rawQuery("SELECT COUNT(*) FROM inventory_requests WHERE branch_id = ? AND status = 'PENDING'", branchArg);
        if (c5.moveToFirst()) stats.put("pending_stock", c5.getInt(0));
        c5.close();

        Cursor c6 = db.rawQuery("SELECT SUM(amount) FROM payments p JOIN appointments a ON p.appointment_id = a.id WHERE a.branch_id = ? AND p.status = 'COMPLETED' AND date(p.paid_at) = date('now')", branchArg);
        if (c6.moveToFirst()) stats.put("daily_revenue", c6.getDouble(0));
        c6.close();

        return stats;
    }

    private List<Appointment> queryAppointments(String whereOrder, String[] args) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT a.*, u.name AS customer_name, b.name AS branch_name, "
                        + "ut.name AS technician_name, s.name AS service_name, s.price AS service_price, "
                        + "p.status AS payment_status "
                        + "FROM appointments a "
                        + "JOIN users u ON u.id = a.customer_id "
                        + "LEFT JOIN branches b ON b.id = a.branch_id "
                        + "LEFT JOIN technicians t ON t.id = a.technician_id "
                        + "LEFT JOIN users ut ON t.user_id = ut.id "
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
        t.available = c.getInt(c.getColumnIndexOrThrow("available"));
        int idx = c.getColumnIndex("branch_name");
        if (idx >= 0) {
            t.branchName = c.getString(idx);
        }
        int sidx = c.getColumnIndex("specialty_category_id");
        if (sidx >= 0) {
            // Mapping category ID to specialty string if needed, or just keeping the ID
            // For now, let's just keep the name from User join
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
        a.deviceName = c.getString(c.getColumnIndexOrThrow("device_name"));
        a.issueDescription = c.getString(c.getColumnIndexOrThrow("issue_description"));
        a.devicePhoto = c.getString(c.getColumnIndexOrThrow("device_photo"));
        a.status = c.getString(c.getColumnIndexOrThrow("status"));
        a.createdAt = c.getString(c.getColumnIndexOrThrow("created_at"));
        a.customerName = c.getString(c.getColumnIndexOrThrow("customer_name"));
        a.branchName = c.getString(c.getColumnIndexOrThrow("branch_name"));
        a.technicianName = c.getString(c.getColumnIndexOrThrow("technician_name"));
        a.serviceName = c.getString(c.getColumnIndexOrThrow("service_name"));
        a.servicePrice = c.getDouble(c.getColumnIndexOrThrow("service_price"));
        int paidIdx = c.getColumnIndex("payment_status");
        if (paidIdx >= 0) {
            a.paymentStatus = c.getString(paidIdx);
        }
        return a;
    }
}
