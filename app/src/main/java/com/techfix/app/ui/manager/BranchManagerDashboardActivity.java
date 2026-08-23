package com.techfix.app.ui.manager;

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
import com.techfix.app.ui.admin.AdminPendingRequestsActivity;
import com.techfix.app.ui.staff.StaffAppointmentsActivity;
import com.techfix.app.ui.staff.StaffBranchesActivity;
import com.techfix.app.ui.staff.StaffPartsActivity;
import com.techfix.app.ui.staff.StaffPaymentsActivity;
import com.techfix.app.ui.staff.StaffStockRequestsActivity;
import com.techfix.app.ui.staff.StaffTechniciansActivity;
import com.techfix.app.util.SessionManager;

import java.util.Map;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class BranchManagerDashboardActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SessionManager session;
    private TextView txtStatPending, txtStatOngoing, txtStatCompleted, txtStatRevenue, txtStatPendingStock, txtStatDailyRevenue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_branch_manager_dashboard);

        dao = new TechFixDao(this);
        session = new SessionManager(this);
        UiHelper.setupToolbar(this, getString(R.string.title_dashboard), false);

        TextView welcome = findViewById(R.id.txtWelcome);
        welcome.setText(getString(R.string.hi_user, session.getName(), getString(R.string.role_branch_manager)));

        txtStatPending = findViewById(R.id.txtStatPending);
        txtStatOngoing = findViewById(R.id.txtStatOngoing);
        txtStatCompleted = findViewById(R.id.txtStatCompleted);
        txtStatRevenue = findViewById(R.id.txtStatRevenue);
        txtStatPendingStock = findViewById(R.id.txtStatPendingStock);
        txtStatDailyRevenue = findViewById(R.id.txtStatDailyRevenue);

        loadStats();

        findViewById(R.id.btnPendingRequests).setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminPendingRequestsActivity.class);
            intent.putExtra("mode", "REPAIRS_ONLY");
            startActivity(intent);
        });

        findViewById(R.id.btnManageRepairs).setOnClickListener(v -> {
            Intent intent = new Intent(this, StaffAppointmentsActivity.class);
            intent.putExtra("filter_status", "ALL");
            startActivity(intent);
        });

        findViewById(R.id.btnTechnicians).setOnClickListener(v ->
                startActivity(new Intent(this, StaffTechniciansActivity.class)));

        findViewById(R.id.btnBranches).setOnClickListener(v ->
                startActivity(new Intent(this, StaffBranchesActivity.class)));

        findViewById(R.id.btnSpareParts).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPartsActivity.class)));

        findViewById(R.id.btnPayments).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPaymentsActivity.class)));

        findViewById(R.id.btnStockRequests).setOnClickListener(v ->
                startActivity(new Intent(this, StaffStockRequestsActivity.class)));

        findViewById(R.id.btnManageServices).setOnClickListener(v ->
                startActivity(new Intent(this, com.techfix.app.ui.admin.AdminServicesActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
        UiHelper.updateNotificationBadge(this, findViewById(R.id.toolbar));
    }

    private void loadStats() {
        Map<String, Object> stats = dao.getBranchManagerStats(session.getBranchId());
        txtStatPending.setText(String.valueOf(stats.getOrDefault("pending_repairs", 0)));
        txtStatOngoing.setText(String.valueOf(stats.getOrDefault("ongoing_repairs", 0)));
        txtStatCompleted.setText(String.valueOf(stats.getOrDefault("completed_repairs", 0)));
        txtStatPendingStock.setText(String.valueOf(stats.getOrDefault("pending_stock", 0)));

        double revenue = (double) stats.getOrDefault("revenue", 0.0);
        txtStatRevenue.setText(UiHelper.money(this, revenue));

        double dailyRev = (double) stats.getOrDefault("daily_revenue", 0.0);
        txtStatDailyRevenue.setText(UiHelper.money(this, dailyRev));
    }
}
