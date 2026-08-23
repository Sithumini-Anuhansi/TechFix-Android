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
import com.techfix.app.model.Notification;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.ui.staff.StaffAppointmentDetailActivity;
import com.techfix.app.ui.staff.StaffPaymentsActivity;
import com.techfix.app.util.SessionManager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificationsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        UiHelper.setupToolbar(this, "Notifications", true);

        long userId = new SessionManager(this).getUserId();
        TechFixDao dao = new TechFixDao(this);
        List<Notification> list = dao.getNotifications(userId);
        
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Notification> adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.title);
            subtitle.setText(item.message);
            meta.setText(item.createdAt);
            // Optionally change color if unread
            if (item.isRead == 0) {
                title.setTextColor(getResources().getColor(R.color.primary));
            } else {
                title.setTextColor(getResources().getColor(android.R.color.black));
            }
        }, item -> {
            String role = new SessionManager(this).getRole();
            if (item.appointmentId > 0) {
                if ("CUSTOMER".equals(role)) {
                    Intent intent = new Intent(this, AppointmentTrackActivity.class);
                    intent.putExtra("appointmentId", item.appointmentId);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(this, StaffAppointmentDetailActivity.class);
                    intent.putExtra("appointment_id", item.appointmentId);
                    startActivity(intent);
                }
            } else if (!"CUSTOMER".equals(role)) {
                // For Staff/Admin, if it's a payment notification, go to payments
                if (item.title.contains("Payment")) {
                    startActivity(new Intent(this, StaffPaymentsActivity.class));
                }
            }
        });

        RecyclerView recycler = findViewById(R.id.recyclerNotifications);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
        
        dao.markNotificationsRead(userId);
    }
}
