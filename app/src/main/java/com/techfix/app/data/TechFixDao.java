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
        Cursor c = db.rawQuery(
                "SELECT t.*, b.name AS branch_name FROM technicians t "
                        + "JOIN branches b ON b.id = t.branch_id "
                        + "WHERE t.branch_id = ? AND t.available = 1 LIMIT 1",
                new String[]{String.valueOf(branchId)});
        Technician tech = null;
        if (c.moveToFirst()) {
            tech = cursorToTechnician(c);
        }
        c.close();
        return tech;
    }

    public List<Technician> getTechnicians() {
        List<Technician> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT t.*, b.name AS branch_name FROM technicians t "
                        + "JOIN branches b ON b.id = t.branch_id ORDER BY b.city, t.name", null);
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
        return appointmentId;
    }

    public List<Appointment> getAppointmentsForCustomer(long customerId, boolean historyOnly) {
        String extra = historyOnly
                ? " AND a.status IN ('COMPLETED','CANCELLED') "
                : " AND a.status NOT IN ('COMPLETED','CANCELLED') ";
        return queryAppointments("WHERE a.customer_id = ?" + extra + "ORDER BY a.id DESC",
                new String[]{String.valueOf(customerId)});
    }

    public List<Appointment> getAllAppointments() {
        return queryAppointments("ORDER BY a.id DESC", null);
    }

    public Appointment getAppointment(long id) {
        List<Appointment> list = queryAppointments("WHERE a.id = ?", new String[]{String.valueOf(id)});
        return list.isEmpty() ? null : list.get(0);
    }

    public void updateAppointmentStatus(long id, String status) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("status", status);
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void assignAppointment(long id, long branchId, long technicianId) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("branch_id", branchId);
        v.put("technician_id", technicianId);
        v.put("status", "ASSIGNED");
        db.update(DatabaseHelper.T_APPOINTMENTS, v, "id = ?", new String[]{String.valueOf(id)});
    }

    public void addRepairImage(long appointmentId, String path, String caption) {
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("appointment_id", appointmentId);
        v.put("file_path", path);
        v.put("caption", caption);
        db.insert(DatabaseHelper.T_IMAGES, null, v);
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
    }

    public SQLiteDatabase getReadable() {
        return helper.getReadableDatabase();
    }

    private List<Appointment> queryAppointments(String whereOrder, String[] args) {
        List<Appointment> list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT a.*, u.name AS customer_name, b.name AS branch_name, "
                        + "t.name AS technician_name, s.name AS service_name, s.price AS service_price "
                        + "FROM appointments a "
                        + "JOIN users u ON u.id = a.customer_id "
                        + "LEFT JOIN branches b ON b.id = a.branch_id "
                        + "LEFT JOIN technicians t ON t.id = a.technician_id "
                        + "JOIN services s ON s.id = a.service_id "
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
                c.getString(c.getColumnIndexOrThrow("role")));
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
        return a;
    }
}
