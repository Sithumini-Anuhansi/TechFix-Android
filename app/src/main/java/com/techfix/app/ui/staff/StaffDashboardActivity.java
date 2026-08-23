package com.techfix.app.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.Map;

public class StaffDashboardActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SessionManager session;
    private TextView txtStatAssigned, txtStatCompleted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        UiHelper.setupToolbar(this, "Dashboard", false);
        
        TextView welcome = findViewById(R.id.txtWelcome);
        String roleLabel = "ADMIN".equals(session.getRole()) ? "Administrator" : "TechFix Staff";
        welcome.setText(getString(R.string.hi_user, session.getName(), roleLabel));

        txtStatAssigned = findViewById(R.id.txtStatAssigned);
        txtStatCompleted = findViewById(R.id.txtStatCompleted);

        loadStats();

        findViewById(R.id.btnAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, StaffAppointmentsActivity.class)));
        findViewById(R.id.btnTechnicians).setOnClickListener(v ->
                startActivity(new Intent(this, StaffTechniciansActivity.class)));
        findViewById(R.id.btnParts).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPartsActivity.class)));
        findViewById(R.id.btnPayments).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPaymentsActivity.class)));
        findViewById(R.id.btnPhotos).setOnClickListener(v ->
                startActivity(new Intent(this, StaffPhotosActivity.class)));
        findViewById(R.id.btnBranches).setOnClickListener(v ->
                startActivity(new Intent(this, StaffBranchesActivity.class)));

        findViewById(R.id.btnStockRequest).setOnClickListener(v -> showStockRequestDialog());
    }

    private void showStockRequestDialog() {
        android.view.View v = getLayoutInflater().inflate(R.layout.dialog_stock_request, null);
        android.widget.EditText inputItem = v.findViewById(R.id.inputItem);
        android.widget.EditText inputQty = v.findViewById(R.id.inputQty);
        android.widget.EditText inputReason = v.findViewById(R.id.inputReason);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Request Stock")
                .setView(v)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String item = inputItem.getText().toString().trim();
                    String qtyStr = inputQty.getText().toString().trim();
                    String reason = inputReason.getText().toString().trim();

                    if (item.isEmpty() || qtyStr.isEmpty()) return;
                    int qty = Integer.parseInt(qtyStr);
                    
                    dao.addInventoryRequest(session.getUserId(), session.getBranchId(), item, qty, reason);
                    android.widget.Toast.makeText(this, "Request submitted to Admin", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStats();
    }

    private void loadStats() {
        Map<String, Object> stats = dao.getStaffStats(session.getUserId());
        txtStatAssigned.setText(String.valueOf(stats.getOrDefault("assigned", 0)));
        txtStatCompleted.setText(String.valueOf(stats.getOrDefault("completed", 0)));
    }
}
