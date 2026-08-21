package com.techfix.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.R;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

public class CustomerHomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_home);
        UiHelper.setupToolbar(this, "TechFix", false);

        SessionManager session = new SessionManager(this);
        TextView welcome = findViewById(R.id.txtWelcome);
        welcome.setText("Hi, " + session.getName());

        findViewById(R.id.cardServices).setOnClickListener(v ->
                startActivity(new Intent(this, ServiceListActivity.class)));
        findViewById(R.id.cardAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, MyAppointmentsActivity.class)));
        findViewById(R.id.cardHistory).setOnClickListener(v ->
                startActivity(new Intent(this, RepairHistoryActivity.class)));
        findViewById(R.id.cardMap).setOnClickListener(v ->
                startActivity(new Intent(this, BranchMapActivity.class)));
    }
}
