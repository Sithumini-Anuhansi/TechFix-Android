package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.List;

public class StaffPhotosActivity extends AppCompatActivity {
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_photos), true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        TechFixDao dao = new TechFixDao(this);
        SessionManager session = new SessionManager(this);
        List<Appointment> list;
        if (session.isStaff()) {
            list = dao.getTechnicianAppointments(session.getUserId(), "COMPLETED");
        } else if (session.isManager()) {
            list = dao.getAllAppointments("COMPLETED", session.getBranchId());
        } else {
            list = dao.getAllAppointments("COMPLETED", 0);
        }

        TextView empty = findViewById(R.id.txtEmpty);
        empty.setText(R.string.msg_empty_photos);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Appointment> adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(getString(R.string.label_service_info, item.serviceName, item.customerName));
            subtitle.setText(session.isStaff() ? getString(R.string.msg_tap_to_capture) : (item.branchName != null ? item.branchName : item.createdAt));
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
        if (session.isStaff() && !list.isEmpty()) {
            Toast.makeText(this, R.string.msg_photo_hint, Toast.LENGTH_LONG).show();
        }
    }
}
