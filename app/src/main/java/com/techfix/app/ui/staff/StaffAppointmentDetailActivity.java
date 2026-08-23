package com.techfix.app.ui.staff;

import android.Manifest;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.model.Payment;
import com.techfix.app.ui.ImageAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.ImageHelper;
import com.techfix.app.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StaffAppointmentDetailActivity extends AppCompatActivity {
    private TechFixDao dao;
    private long appointmentId;
    private ImageAdapter imageAdapter;
    private File photoFile;

    private final ActivityResultLauncher<String> cameraPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    capture();
                } else {
                    Toast.makeText(this, R.string.msg_camera_permission, Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && photoFile != null) {
                    dao.addRepairImage(appointmentId, photoFile.getAbsolutePath(), "Staff repaired-device photo");
                    refreshImages();
                    BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    Toast.makeText(this, R.string.msg_photo_saved, Toast.LENGTH_SHORT).show();
                }
            });

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_staff_appointment_detail);
        UiHelper.setupToolbar(this, getString(R.string.title_repair_details), true);
        dao = new TechFixDao(this);
        appointmentId = getIntent().getLongExtra("appointmentId", 0);

        SessionManager session = new SessionManager(this);
        // Admin: Remove Assign Technician Text and button
        // Manager: Can still assign
        boolean isManager = session.isManager();
        boolean isAdmin = session.isAdmin();
        boolean isStaff = session.isStaff();

        if (isManager) {
            findViewById(R.id.layoutAssign).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.layoutStaffActions).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.btnCamera).setVisibility(android.view.View.VISIBLE);
        } else if (isAdmin) {
            findViewById(R.id.layoutAssign).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.layoutStaffActions).setVisibility(android.view.View.GONE);
            findViewById(R.id.btnCamera).setVisibility(android.view.View.GONE);
        } else {
            // Staff/Technician
            findViewById(R.id.layoutAssign).setVisibility(android.view.View.GONE);
            findViewById(R.id.layoutStaffActions).setVisibility(android.view.View.VISIBLE);
            findViewById(R.id.btnCamera).setVisibility(android.view.View.VISIBLE);
        }

        bind();

        Spinner status = findViewById(R.id.spinnerStatus);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.appointment_statuses, android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(statusAdapter);

        // Set current status in spinner
        Appointment a = dao.getAppointment(appointmentId);
        if (a != null) {
            for (int i = 0; i < statusAdapter.getCount(); i++) {
                if (statusAdapter.getItem(i).toString().equals(a.status)) {
                    status.setSelection(i);
                    break;
                }
            }
        }

        Spinner method = findViewById(R.id.spinnerMethod);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_dropdown_item);
        method.setAdapter(methodAdapter);

        findViewById(R.id.btnSaveStatus).setOnClickListener(v -> {
            String value = status.getSelectedItem().toString();
            dao.updateAppointmentStatus(appointmentId, value);
            Toast.makeText(this, R.string.msg_status_updated, Toast.LENGTH_SHORT).show();
            bind();
        });

        findViewById(R.id.btnMarkPaid).setOnClickListener(v -> {
            Payment pay = dao.getPaymentForAppointment(appointmentId);
            if (pay == null) {
                Toast.makeText(this, R.string.msg_no_payment, Toast.LENGTH_SHORT).show();
                return;
            }
            String paidAt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dao.markPaid(appointmentId, method.getSelectedItem().toString(), paidAt);
            Toast.makeText(this, R.string.msg_payment_recorded, Toast.LENGTH_SHORT).show();
            bind();
        });

        findViewById(R.id.btnCamera).setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                capture();
            } else {
                cameraPermission.launch(Manifest.permission.CAMERA);
            }
        });

        imageAdapter = new ImageAdapter();
        RecyclerView recycler = findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(imageAdapter);
        refreshImages();
    }

    private void setupAssignmentUI(Appointment a) {
        Spinner techSpin = findViewById(R.id.spinnerTech);

        // Filter technicians by the branch selected by the customer AND by service category
        java.util.List<com.techfix.app.model.Technician> techs = dao.getTechniciansByBranch(a.branchId, null, a.serviceId > 0 ? dao.getService(a.serviceId).categoryId : null);
        
        // If it's a manager, ensure they can only assign to their own branch technicians 
        // (though a.branchId should already be their branch if filtered correctly in the list)
        
        ArrayAdapter<com.techfix.app.model.Technician> tAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, techs);
        techSpin.setAdapter(tAdapter);
        
        // Pre-select if already assigned
        if (a.technicianId > 0) {
            for (int i = 0; i < techs.size(); i++) {
                if (techs.get(i).id == a.technicianId) {
                    techSpin.setSelection(i);
                    break;
                }
            }
        }

        findViewById(R.id.btnAssign).setOnClickListener(v -> {
            com.techfix.app.model.Technician t = (com.techfix.app.model.Technician) techSpin.getSelectedItem();
            if (t != null) {
                dao.assignAppointment(appointmentId, a.branchId, t.id);
                Toast.makeText(this, R.string.msg_assign_success, Toast.LENGTH_SHORT).show();
                bind();
                
                // If Manager is assigning, notify Admin
                SessionManager session = new SessionManager(this);
                if (session.isManager()) {
                    String time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(new java.util.Date());
                    // Find an admin to notify dynamically instead of hardcoding ID 1
                    android.database.sqlite.SQLiteDatabase db = new com.techfix.app.data.DatabaseHelper(this).getReadableDatabase();
                    android.database.Cursor cAdmin = db.rawQuery("SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1", null);
                    if (cAdmin.moveToFirst()) {
                        dao.addNotification(cAdmin.getLong(0), appointmentId, "Repair Assigned by Manager",
                                "Manager " + session.getName() + " assigned " + t.name + " to repair #" + appointmentId, time);
                    }
                    cAdmin.close();
                }
            } else {
                Toast.makeText(this, R.string.msg_no_tech_available, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bind() {
        Appointment a = dao.getAppointment(appointmentId);
        if (a == null) {
            finish();
            return;
        }

        SessionManager session = new SessionManager(this);

        // Security check: Staff can only see their own appointments
        if (session.isStaff()) {
            // Find technician ID for this staff user
            long currentTechId = 0;
            android.database.sqlite.SQLiteDatabase db = new com.techfix.app.data.DatabaseHelper(this).getReadableDatabase();
            android.database.Cursor cu = db.rawQuery("SELECT id FROM technicians WHERE user_id = ?", new String[]{String.valueOf(session.getUserId())});
            if (cu.moveToFirst()) currentTechId = cu.getLong(0);
            cu.close();
            db.close();

            if (a.technicianId != currentTechId) {
                android.widget.Toast.makeText(this, R.string.msg_unauthorized, android.widget.Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else if (session.isManager()) {
            // Manager security check: can only see appointments for their branch
            if (a.branchId != session.getBranchId()) {
                android.widget.Toast.makeText(this, R.string.msg_unauthorized, android.widget.Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        if (session.isAdmin() || session.isManager()) {
            setupAssignmentUI(a);
        }

        Payment pay = dao.getPaymentForAppointment(appointmentId);
        String paymentInfo = (pay == null ? getString(R.string.label_none) : 
                ("COMPLETED".equals(pay.status) ? getString(R.string.label_payment_paid, pay.method) : getString(R.string.label_payment_unpaid)));
        
        String details = getString(R.string.label_repair_details_summary,
                a.customerName,
                a.serviceName,
                UiHelper.money(this, a.servicePrice),
                n(a.branchName),
                n(a.technicianName),
                a.status,
                n(a.deviceName + ": " + a.issueDescription),
                paymentInfo);
        ((TextView) findViewById(R.id.txtDetails)).setText(details);

        // Display customer device photo if it exists
        if (a.devicePhoto != null && !a.devicePhoto.isEmpty()) {
            java.io.File file = new java.io.File(a.devicePhoto);
            if (file.exists()) {
                android.widget.ImageView imgDevice = new android.widget.ImageView(this);
                imgDevice.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 600));
                imgDevice.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                imgDevice.setPadding(0, 16, 0, 16);
                imgDevice.setImageURI(android.net.Uri.fromFile(file));
                ((LinearLayout) findViewById(R.id.txtDetails).getParent()).addView(imgDevice, 1);
            }
        }
    }

    private void refreshImages() {
        if (imageAdapter != null) {
            imageAdapter.submit(dao.getImages(appointmentId));
        }
    }

    private void capture() {
        try {
            photoFile = ImageHelper.createImageFile(this);
            cameraLauncher.launch(ImageHelper.uriFor(this, photoFile));
        } catch (IOException e) {
            Toast.makeText(this, R.string.msg_camera_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String n(String value) {
        return value == null || value.isEmpty() ? getString(R.string.label_none) : value;
    }
}
