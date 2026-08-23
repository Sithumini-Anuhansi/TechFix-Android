package com.techfix.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.User;
import com.techfix.app.util.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        UiHelper.setupToolbar(this, "Profile Settings", true);

        SessionManager session = new SessionManager(this);
        TechFixDao dao = new TechFixDao(this);
        User user = dao.getUser(session.getUserId());

        TextInputEditText nameInput = findViewById(R.id.inputName);
        TextInputEditText emailInput = findViewById(R.id.inputEmail);
        TextInputEditText phoneInput = findViewById(R.id.inputPhone);
        MaterialButton updateBtn = findViewById(R.id.btnUpdate);

        if (user != null) {
            nameInput.setText(user.name);
            emailInput.setText(user.email);
            phoneInput.setText(user.phone);
        }

        updateBtn.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String phone = phoneInput.getText().toString();
            
            if (name.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            dao.updateProfile(session.getUserId(), name, phone);
            
            // Update session
            user.name = name;
            session.save(user);
            
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
