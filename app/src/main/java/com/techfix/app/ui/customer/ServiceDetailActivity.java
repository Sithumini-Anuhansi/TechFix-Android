package com.techfix.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Service;
import com.techfix.app.ui.UiHelper;

public class ServiceDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_detail);
        UiHelper.setupToolbar(this, "Service", true);

        long serviceId = getIntent().getLongExtra("serviceId", 0);
        Service service = new TechFixDao(this).getService(serviceId);
        if (service == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txtCategory)).setText(service.categoryName);
        ((TextView) findViewById(R.id.txtName)).setText(service.name);
        ((TextView) findViewById(R.id.txtPrice)).setText(UiHelper.money(service.price));
        ((TextView) findViewById(R.id.txtDescription)).setText(service.description);
        ((TextView) findViewById(R.id.txtSample)).setText("Sample repaired-device photos: " + service.sampleImageHint);

        MaterialButton book = findViewById(R.id.btnBook);
        book.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookRepairActivity.class);
            intent.putExtra("serviceId", service.id);
            startActivity(intent);
        });
    }
}
