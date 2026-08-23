package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Technician;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.List;

public class StaffTechniciansActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Technician> adapter;
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Technicians", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        dao = new TechFixDao(this);
        session = new SessionManager(this);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.specialty);
            meta.setText(item.branchName + (item.available == 1 ? " · Available" : " · Busy"));
        }, item -> showOptions(item));

        recycler.setAdapter(adapter);
        load();

        findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_technician, null);
        android.widget.Spinner branchSpin = view.findViewById(R.id.spinnerBranch);
        android.widget.EditText name = view.findViewById(R.id.inputName);
        android.widget.EditText spec = view.findViewById(R.id.inputSpecialty);

        List<com.techfix.app.model.Branch> branches = dao.getBranches();
        android.widget.ArrayAdapter<com.techfix.app.model.Branch> bAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches);
        branchSpin.setAdapter(bAdapter);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Technician")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    com.techfix.app.model.Branch b = (com.techfix.app.model.Branch) branchSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String s = spec.getText().toString();
                    if (b != null && !n.isEmpty()) {
                        dao.addTechnician(n, b.id, s);
                        load();
                        android.widget.Toast.makeText(this, "Technician added", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        List<Technician> list;
        if ("ADMIN".equals(session.getRole())) {
            list = dao.getTechnicians(null);
        } else {
            list = dao.getTechniciansByBranch(session.getBranchId(), null);
        }
        adapter.submit(list);
        findViewById(R.id.txtEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showOptions(Technician tech) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(tech.name)
                .setItems(new String[]{"Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        dao.deleteTechnician(tech.id);
                        load();
                        android.widget.Toast.makeText(this, "Technician deleted", android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
