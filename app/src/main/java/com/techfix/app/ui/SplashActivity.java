package com.techfix.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.techfix.app.ui.auth.LoginActivity;
import com.techfix.app.ui.customer.CustomerHomeActivity;
import com.techfix.app.ui.staff.StaffDashboardActivity;
import com.techfix.app.util.SessionManager;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SessionManager session = new SessionManager(this);
            Intent next;
            if (session.isLoggedIn()) {
                next = session.isStaff()
                        ? new Intent(this, StaffDashboardActivity.class)
                        : new Intent(this, CustomerHomeActivity.class);
            } else {
                next = new Intent(this, LoginActivity.class);
            }
            startActivity(next);
            finish();
        }, 900);
    }
}
