package com.techfix.app.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

public class StaffAppointmentsActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Appointments", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    private void load() {
        List<Appointment> list = new TechFixDao(this).getAllAppointments();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        SimpleAdapter<Appointment> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.serviceName + " — " + item.customerName);
            subtitle.setText((item.branchName == null ? "Unassigned" : item.branchName) + " · " + item.createdAt);
            meta.setText(item.status);
        }, item -> {
            Intent intent = new Intent(this, StaffAppointmentDetailActivity.class);
            intent.putExtra("appointmentId", item.id);
            startActivity(intent);
        });
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
