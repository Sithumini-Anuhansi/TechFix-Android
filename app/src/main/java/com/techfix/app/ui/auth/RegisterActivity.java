package com.techfix.app.ui.auth;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.User;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

public class RegisterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UiHelper.setupToolbar(this, "Create account", true);

        TextInputEditText name = findViewById(R.id.inputName);
        TextInputEditText email = findViewById(R.id.inputEmail);
        TextInputEditText phone = findViewById(R.id.inputPhone);
        TextInputEditText password = findViewById(R.id.inputPassword);
        MaterialButton register = findViewById(R.id.btnRegister);

        register.setOnClickListener(v -> {
            String n = text(name);
            String e = text(email);
            String ph = text(phone);
            String p = text(password);
            if (n.isEmpty() || e.isEmpty() || p.length() < 6) {
                Toast.makeText(this, "Name, email and a password of 6+ characters are required", Toast.LENGTH_LONG).show();
                return;
            }
            TechFixDao dao = new TechFixDao(this);
            if (dao.emailExists(e)) {
                Toast.makeText(this, "That email is already registered", Toast.LENGTH_SHORT).show();
                return;
            }
            long id = dao.registerCustomer(n, e, p, ph);
            User user = dao.getUser(id);
            new SessionManager(this).save(user);
            Toast.makeText(this, "Welcome to TechFix", Toast.LENGTH_SHORT).show();
            startActivity(new android.content.Intent(this, com.techfix.app.ui.customer.CustomerHomeActivity.class));
            finish();
        });
    }

    private String text(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
