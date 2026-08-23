package com.techfix.app.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.Map;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class StaffDashboardActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SessionManager session;
    private TextView txtStatTotal, txtStatAssigned, txtStatCompleted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_staff_dashboard);
        
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        UiHelper.setupToolbar(this, getString(R.string.title_dashboard), false);
        
        TextView welcome = findViewById(R.id.txtWelcome);
        String roleLabel = getString(R.string.role_staff);
        if (session.isAdmin()) roleLabel = getString(R.string.role_admin);
        else if (session.isManager()) roleLabel = getString(R.string.role_branch_manager);

        welcome.setText(getString(R.string.hi_user, session.getName(), roleLabel));

        txtStatTotal = findViewById(R.id.txtStatTotal);
        txtStatAssigned = findViewById(R.id.txtStatAssigned);
        txtStatCompleted = findViewById(R.id.txtStatCompleted);

        loadStats();

        findViewById(R.id.btnAppointments).setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra("filter_status", "ALL");
            startActivity(intent);
        });
        
        // Only allow technicians/staff to see the Technicians menu if they need to see colleagues,
        // but typically staff just sees their own tasks.
        // For now, let's keep it but restrict access.

        findViewById(R.id.btnTechnicians).setOnClickListener(v ->
                startActivity(new Intent(this, StaffTechniciansActivity.class)));
        
        if (session.isStaff()) {
            findViewById(R.id.btnPhotos).setOnClickListener(v ->
                    startActivity(new Intent(this, StaffPhotosActivity.class)));
        } else {
            findViewById(R.id.btnPhotos).setVisibility(android.view.View.GONE);
        }

        findViewById(R.id.btnBranches).setOnClickListener(v ->
                startActivity(new Intent(this, StaffBranchesActivity.class)));
    }

    private void showStockRequestDialog() {
        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_stock_request, null);
        android.widget.EditText inputItem = v.findViewById(R.id.inputItem);
        android.widget.EditText inputQty = v.findViewById(R.id.inputQty);
        android.widget.EditText inputReason = v.findViewById(R.id.inputReason);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_request_stock)
                .setView(v)
                .setPositiveButton(R.string.action_submit, (dialog, which) -> {
                    String item = inputItem.getText().toString().trim();
                    String qtyStr = inputQty.getText().toString().trim();
                    String reason = inputReason.getText().toString().trim();

                    if (item.isEmpty() || qtyStr.isEmpty()) return;
                    int qty = Integer.parseInt(qtyStr);
                    
                    dao.addInventoryRequest(session.getUserId(), session.getBranchId(), item, qty, reason);
                    android.widget.Toast.makeText(this, R.string.msg_request_submitted, android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        UiHelper.updateNotificationBadge(this, findViewById(R.id.toolbar));
    }

    private void loadStats() {
        Map<String, Object> stats;
        if (session.isManager()) {
            stats = dao.getBranchManagerStats(session.getBranchId());
        } else {
            stats = dao.getStaffStats(session.getUserId());
        }
        txtStatTotal.setText(String.valueOf(stats.getOrDefault("total", 0)));
        txtStatAssigned.setText(String.valueOf(stats.getOrDefault("pending", 0)));
        txtStatCompleted.setText(String.valueOf(stats.getOrDefault("completed", 0)));
    }
}
