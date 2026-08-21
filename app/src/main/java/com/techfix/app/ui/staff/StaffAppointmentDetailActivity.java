package com.techfix.app.ui.staff;

import android.Manifest;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.model.Payment;
import com.techfix.app.ui.ImageAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.ImageHelper;

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
                    Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && photoFile != null) {
                    dao.addRepairImage(appointmentId, photoFile.getAbsolutePath(), "Staff repaired-device photo");
                    refreshImages();
                    BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                    Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_appointment_detail);
        UiHelper.setupToolbar(this, "Handle repair", true);
        dao = new TechFixDao(this);
        appointmentId = getIntent().getLongExtra("appointmentId", 0);
        bind();

        Spinner status = findViewById(R.id.spinnerStatus);
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(this,
                R.array.appointment_statuses, android.R.layout.simple_spinner_dropdown_item);
        status.setAdapter(statusAdapter);

        Spinner method = findViewById(R.id.spinnerMethod);
        ArrayAdapter<CharSequence> methodAdapter = ArrayAdapter.createFromResource(this,
                R.array.payment_methods, android.R.layout.simple_spinner_dropdown_item);
        method.setAdapter(methodAdapter);

        findViewById(R.id.btnSaveStatus).setOnClickListener(v -> {
            String value = status.getSelectedItem().toString();
            dao.updateAppointmentStatus(appointmentId, value);
            Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show();
            bind();
        });

        findViewById(R.id.btnMarkPaid).setOnClickListener(v -> {
            Payment pay = dao.getPaymentForAppointment(appointmentId);
            if (pay == null) {
                Toast.makeText(this, "No payment record", Toast.LENGTH_SHORT).show();
                return;
            }
            String paidAt = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            dao.markPaid(pay.id, method.getSelectedItem().toString(), paidAt);
            Toast.makeText(this, "Payment recorded", Toast.LENGTH_SHORT).show();
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

    private void bind() {
        Appointment a = dao.getAppointment(appointmentId);
        if (a == null) {
            finish();
            return;
        }
        Payment pay = dao.getPaymentForAppointment(appointmentId);
        String details = "Customer: " + a.customerName + "\n"
                + "Service: " + a.serviceName + " (" + UiHelper.money(a.servicePrice) + ")\n"
                + "Branch: " + n(a.branchName) + "\n"
                + "Technician: " + n(a.technicianName) + "\n"
                + "Status: " + a.status + "\n"
                + "Note: " + n(a.deviceNote) + "\n"
                + "Payment: " + (pay == null ? "—" : (pay.paid == 1 ? "Paid " + pay.method : "Unpaid"));
        ((TextView) findViewById(R.id.txtDetails)).setText(details);
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
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private String n(String value) {
        return value == null || value.isEmpty() ? "—" : value;
    }
}
