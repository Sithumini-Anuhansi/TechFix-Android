package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Service;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class AdminServicesActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Service> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Manage Services", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        dao = new TechFixDao(this);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.description);
            meta.setText(UiHelper.money(item.price));
        }, item -> showOptions(item));

        recycler.setAdapter(adapter);
        load();

        findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_service, null);
        android.widget.Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        android.widget.EditText name = view.findViewById(R.id.inputName);
        android.widget.EditText price = view.findViewById(R.id.inputPrice);
        android.widget.EditText desc = view.findViewById(R.id.inputDescription);

        java.util.List<com.techfix.app.model.Category> cats = dao.getCategories();
        android.widget.ArrayAdapter<com.techfix.app.model.Category> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        catSpin.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Service")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    com.techfix.app.model.Category c = (com.techfix.app.model.Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String pStr = price.getText().toString();
                    String d = desc.getText().toString();
                    if (c != null && !n.isEmpty() && !pStr.isEmpty()) {
                        dao.addService(c.id, n, Double.parseDouble(pStr), d);
                        load();
                        Toast.makeText(this, "Service added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        List<Service> list = dao.searchServices("");
        adapter.submit(list);
        findViewById(R.id.txtEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showOptions(Service service) {
        new AlertDialog.Builder(this)
                .setTitle(service.name)
                .setItems(new String[]{"Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        dao.deleteService(service.id);
                        load();
                        Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
