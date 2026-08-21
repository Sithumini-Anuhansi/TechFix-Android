package com.techfix.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.User;
import com.techfix.app.ui.customer.CustomerHomeActivity;
import com.techfix.app.ui.staff.StaffDashboardActivity;
import com.techfix.app.util.SessionManager;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextInputEditText email = findViewById(R.id.inputEmail);
        TextInputEditText password = findViewById(R.id.inputPassword);
        MaterialButton login = findViewById(R.id.btnLogin);
        MaterialButton register = findViewById(R.id.btnRegister);

        login.setOnClickListener(v -> {
            String e = text(email);
            String p = text(password);
            if (e.isEmpty() || p.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }
            User user = new TechFixDao(this).login(e, p);
            if (user == null) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                return;
            }
            new SessionManager(this).save(user);
            if ("STAFF".equals(user.role)) {
                startActivity(new Intent(this, StaffDashboardActivity.class));
            } else {
                startActivity(new Intent(this, CustomerHomeActivity.class));
            }
            finish();
        });

        register.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
