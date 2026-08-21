package com.techfix.app.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

public class StaffDashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_dashboard);
        UiHelper.setupToolbar(this, "Staff dashboard", false);
        ((TextView) findViewById(R.id.txtWelcome)).setText("Hello, " + new SessionManager(this).getName());

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
    }
}
