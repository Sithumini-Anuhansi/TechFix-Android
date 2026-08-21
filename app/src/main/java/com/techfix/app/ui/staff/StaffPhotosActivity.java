package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class StaffPhotosActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Repaired device photos", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        List<Appointment> list = new TechFixDao(this).getAllAppointments();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setText("Open an appointment to capture photos with the camera");
        empty.setVisibility(View.VISIBLE);

        SimpleAdapter<Appointment> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.serviceName + " — " + item.customerName);
            subtitle.setText("Tap to open and capture after-repair photos");
            meta.setText(item.status);
        }, item -> {
            android.content.Intent intent = new android.content.Intent(this, StaffAppointmentDetailActivity.class);
            intent.putExtra("appointmentId", item.id);
            startActivity(intent);
        });
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
        if (!list.isEmpty()) {
            Toast.makeText(this, "Choose a job, then use Capture repaired device photo", Toast.LENGTH_LONG).show();
        }
    }
}
