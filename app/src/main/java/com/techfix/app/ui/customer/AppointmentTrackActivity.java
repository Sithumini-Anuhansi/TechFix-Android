package com.techfix.app.ui.customer;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.model.Payment;
import com.techfix.app.ui.ImageAdapter;
import com.techfix.app.ui.UiHelper;

public class AppointmentTrackActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_track);
        UiHelper.setupToolbar(this, "Track repair", true);

        long id = getIntent().getLongExtra("appointmentId", 0);
        TechFixDao dao = new TechFixDao(this);
        Appointment a = dao.getAppointment(id);
        if (a == null) {
            finish();
            return;
        }

        Payment pay = dao.getPaymentForAppointment(id);
        ((TextView) findViewById(R.id.txtStatus)).setText(a.status);
        String details = "Service: " + a.serviceName + "\n"
                + "Price: " + UiHelper.money(a.servicePrice) + "\n"
                + "Branch: " + n(a.branchName) + "\n"
                + "Technician: " + n(a.technicianName) + "\n"
                + "Booked: " + a.createdAt + "\n"
                + "Device: " + n(a.deviceNote) + "\n"
                + "Payment: " + (pay == null ? "—" : (pay.paid == 1 ? "Paid (" + pay.method + ")" : "Unpaid"));
        ((TextView) findViewById(R.id.txtDetails)).setText(details);

        ImageAdapter images = new ImageAdapter();
        RecyclerView recycler = findViewById(R.id.recyclerImages);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(images);
        images.submit(dao.getImages(id));
    }

    private String n(String value) {
        return value == null || value.isEmpty() ? "—" : value;
    }
}
