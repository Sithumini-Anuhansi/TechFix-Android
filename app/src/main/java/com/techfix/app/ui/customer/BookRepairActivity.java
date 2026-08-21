package com.techfix.app.ui.customer;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.model.Service;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.BranchAssigner;
import com.techfix.app.util.ImageHelper;
import com.techfix.app.util.LocationHelper;
import com.techfix.app.util.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class BookRepairActivity extends AppCompatActivity {
    private TechFixDao dao;
    private Service service;
    private List<Branch> eligible = new ArrayList<>();
    private File photoFile;
    private Double lat;
    private Double lng;

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissions);
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
                if (ok && photoFile != null) {
                    ImageView preview = findViewById(R.id.imgPreview);
                    preview.setVisibility(ImageView.VISIBLE);
                    preview.setImageBitmap(BitmapFactory.decodeFile(photoFile.getAbsolutePath()));
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_repair);
        UiHelper.setupToolbar(this, "Book repair", true);

        dao = new TechFixDao(this);
        long serviceId = getIntent().getLongExtra("serviceId", 0);
        service = dao.getService(serviceId);
        if (service == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txtService)).setText(service.name + " — " + UiHelper.money(service.price));
        eligible = dao.getEligibleBranches(service.categoryId);
        populateBranches();

        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CAMERA
        });

        findViewById(R.id.btnPhoto).setOnClickListener(v -> takePhoto());
        findViewById(R.id.btnSubmit).setOnClickListener(v -> submit());
    }

    private void onPermissions(Map<String, Boolean> result) {
        if (LocationHelper.hasPermission(this)) {
            LocationHelper.requestCurrent(this, new LocationHelper.LocationCallback() {
                @Override
                public void onLocation(double latitude, double longitude) {
                    lat = latitude;
                    lng = longitude;
                    Branch nearest = LocationHelper.nearest(latitude, longitude, eligible);
                    TextView hint = findViewById(R.id.txtAssignHint);
                    if (nearest != null) {
                        double km = LocationHelper.haversineKm(latitude, longitude, nearest.latitude, nearest.longitude);
                        hint.setText(String.format(Locale.US,
                                "GPS ready. Nearest eligible branch: %s (%.1f km)", nearest.name, km));
                    } else {
                        hint.setText("GPS ready. No eligible branch found for this category.");
                    }
                }

                @Override
                public void onUnavailable() {
                    ((TextView) findViewById(R.id.txtAssignHint)).setText(
                            "Location unavailable. Choose a branch below.");
                }
            });
        } else {
            ((TextView) findViewById(R.id.txtAssignHint)).setText(
                    "Location permission denied. Choose a branch below.");
        }
    }

    private void populateBranches() {
        List<String> labels = new ArrayList<>();
        labels.add("Auto — nearest branch with GPS");
        for (Branch b : eligible) {
            labels.add(b.name + " (" + b.city + ")");
        }
        Spinner spinner = findViewById(R.id.spinnerBranch);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels);
        spinner.setAdapter(adapter);
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            return;
        }
        try {
            photoFile = ImageHelper.createImageFile(this);
            cameraLauncher.launch(ImageHelper.uriFor(this, photoFile));
        } catch (IOException e) {
            Toast.makeText(this, "Could not open camera", Toast.LENGTH_SHORT).show();
        }
    }

    private void submit() {
        TextInputEditText noteInput = findViewById(R.id.inputNote);
        String note = noteInput.getText() == null ? "" : noteInput.getText().toString().trim();
        if (note.isEmpty()) {
            Toast.makeText(this, "Describe the device and issue", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eligible.isEmpty()) {
            Toast.makeText(this, "No branch currently has a technician and parts for this service", Toast.LENGTH_LONG).show();
            return;
        }

        Spinner spinner = findViewById(R.id.spinnerBranch);
        Long manualId = null;
        if (spinner.getSelectedItemPosition() > 0) {
            manualId = eligible.get(spinner.getSelectedItemPosition() - 1).id;
        }

        BranchAssigner.Result result = BranchAssigner.assign(dao, service.categoryId, lat, lng, manualId);
        if (result == null) {
            Toast.makeText(this, "Could not assign a branch", Toast.LENGTH_SHORT).show();
            return;
        }

        String created = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        long customerId = new SessionManager(this).getUserId();
        long appointmentId = dao.insertAppointment(customerId, result.branch.id, result.technician.id,
                service.id, note, "ASSIGNED", created);

        if (photoFile != null && photoFile.exists()) {
            dao.addRepairImage(appointmentId, photoFile.getAbsolutePath(), "Customer device photo");
        }

        String msg = "Assigned to " + result.branch.name + " / " + result.technician.name;
        if (result.usedGps) {
            msg += String.format(Locale.US, " (%.1f km)", result.distanceKm);
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, MyAppointmentsActivity.class));
        finish();
    }
}
