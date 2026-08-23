package com.techfix.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.ui.staff.StaffAppointmentsActivity;
import com.techfix.app.ui.staff.StaffBranchesActivity;
import com.techfix.app.ui.staff.StaffPartsActivity;
import com.techfix.app.ui.staff.StaffPaymentsActivity;
import com.techfix.app.util.SessionManager;

import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {
    private TechFixDao dao;
    private TextView txtStatPending, txtStatCompleted, txtStatRevenue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        dao = new TechFixDao(this);
        SessionManager session = new SessionManager(this);
        UiHelper.setupToolbar(this, "Dashboard", false);

        TextView welcome = findViewById(R.id.txtWelcome);
        welcome.setText(getString(R.string.hi_user, session.getName(), "Administrator"));

        txtStatPending = findViewById(R.id.txtStatPending);
        txtStatCompleted = findViewById(R.id.txtStatCompleted);
        txtStatRevenue = findViewById(R.id.txtStatRevenue);

        loadStats();

        findViewById(R.id.btnAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, StaffAppointmentsActivity.class)));
        
        findViewById(R.id.btnStaff).setOnClickListener(v ->
                startActivity(new Intent(this, AdminStaffActivity.class)));
        
        findViewById(R.id.btnBranches).setOnClickListener(v ->
                startActivity(new Intent(this, AdminBranchesActivity.class)));

        findViewById(R.id.btnCategories).setOnClickListener(v ->
                startActivity(new Intent(this, AdminCategoriesActivity.class)));
        
        findViewById(R.id.btnServices).setOnClickListener(v ->
                startActivity(new Intent(this, AdminServicesActivity.class)));
        
        findViewById(R.id.btnParts).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPartsActivity.class)));
        
        findViewById(R.id.btnPayments).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPaymentsActivity.class)));

        findViewById(R.id.btnStockRequests).setOnClickListener(v ->
                startActivity(new Intent(this, AdminInventoryRequestsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        Map<String, Object> stats = dao.getAdminStats();
        txtStatPending.setText(String.valueOf(stats.getOrDefault("pending_repairs", 0)));
        txtStatCompleted.setText(String.valueOf(stats.getOrDefault("completed_repairs", 0)));
        double revenue = (double) stats.getOrDefault("revenue", 0.0);
        txtStatRevenue.setText(UiHelper.money(revenue));
    }
}
