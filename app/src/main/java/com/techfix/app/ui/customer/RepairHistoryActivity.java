package com.techfix.app.ui.customer;

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
import com.techfix.app.util.SessionManager;

import java.util.List;

public class RepairHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Repair history", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        long userId = new SessionManager(this).getUserId();
        List<Appointment> list = new TechFixDao(this).getAppointmentsForCustomer(userId, true);
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setText("No previous repairs");
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Appointment> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.serviceName);
            subtitle.setText((item.branchName == null ? "" : item.branchName + " — ") + item.createdAt);
            meta.setText(item.status + " · " + UiHelper.money(item.servicePrice));
        }, item -> {
            Intent intent = new Intent(this, AppointmentTrackActivity.class);
            intent.putExtra("appointmentId", item.id);
            startActivity(intent);
        });
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
