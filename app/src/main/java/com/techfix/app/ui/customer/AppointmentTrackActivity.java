package com.techfix.app.ui.customer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.model.Payment;
import com.techfix.app.ui.ImageAdapter;
import com.techfix.app.ui.UiHelper;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class AppointmentTrackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_track);
        UiHelper.setupToolbar(this, getString(R.string.title_track_repair), true);

        long id = getIntent().getLongExtra("appointmentId", 0);
        TechFixDao dao = new TechFixDao(this);
        Appointment a = dao.getAppointment(id);
        if (a == null) {
            finish();
            return;
        }

        Payment pay = dao.getPaymentForAppointment(id);
        ((TextView) findViewById(R.id.txtStatus)).setText(a.status);
        String paymentStr = (pay == null ? getString(R.string.label_none) :
                ("COMPLETED".equals(pay.status) ? getString(R.string.label_payment_paid, pay.method) : getString(R.string.label_payment_unpaid)));
        
        String details = getString(R.string.label_track_summary,
                a.serviceName,
                UiHelper.money(this, a.servicePrice),
                n(a.branchName),
                n(a.technicianName),
                a.createdAt,
                n(a.deviceName + ": " + a.issueDescription),
                paymentStr);
        ((TextView) findViewById(R.id.txtDetails)).setText(details);

        ImageAdapter images = new ImageAdapter();
        RecyclerView recycler = findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(images);
        images.submit(dao.getImages(id));
    }

    private String n(String value) {
        return value == null || value.isEmpty() ? getString(R.string.label_none) : value;
    }
}
