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

        findViewById(R.id.btnForgot).setOnClickListener(v -> {
            String e = text(email);
            if (e.isEmpty()) {
                Toast.makeText(this, "Enter your email first", Toast.LENGTH_SHORT).show();
                return;
            }
            if (new TechFixDao(this).emailExists(e)) {
                // In a real app, send email. For CW, show a dialog to reset.
                showResetDialog(e);
            } else {
                Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
            }
        });

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
            if ("ADMIN".equals(user.role)) {
                startActivity(new Intent(this, com.techfix.app.ui.admin.AdminDashboardActivity.class));
            } else if ("STAFF".equals(user.role)) {
                startActivity(new Intent(this, StaffDashboardActivity.class));
            } else {
                startActivity(new Intent(this, CustomerHomeActivity.class));
            }
            finish();
        });

        register.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void showResetDialog(String email) {
        android.view.View view = getLayoutInflater().inflate(R.layout.dialog_reset_password, null);
        TextInputEditText passInput = view.findViewById(R.id.inputNewPassword);
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle("Reset Password")
                .setMessage("Enter a new password for " + email)
                .setView(view)
                .setPositiveButton("Reset", (d, w) -> {
                    String newPass = passInput.getText().toString();
                    if (newPass.length() < 4) {
                        Toast.makeText(this, "Password too short", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new TechFixDao(this).updatePassword(email, newPass);
                    Toast.makeText(this, "Password updated. Please login.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
