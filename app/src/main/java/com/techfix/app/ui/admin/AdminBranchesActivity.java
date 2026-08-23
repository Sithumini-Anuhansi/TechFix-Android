package com.techfix.app.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class AdminBranchesActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Branch> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Manage Branches", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        dao = new TechFixDao(this);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.address);
            meta.setText(String.format("%s · %s", item.city, item.phone));
        }, this::showOptions);

        recycler.setAdapter(adapter);
        load();

        findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_branch, null);
        EditText name = view.findViewById(R.id.inputName);
        EditText addr = view.findViewById(R.id.inputAddress);
        EditText city = view.findViewById(R.id.inputCity);
        EditText phone = view.findViewById(R.id.inputPhone);
        EditText lat = view.findViewById(R.id.inputLat);
        EditText lng = view.findViewById(R.id.inputLng);

        new AlertDialog.Builder(this)
                .setTitle("Add New Branch")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String n = name.getText().toString();
                    String a = addr.getText().toString();
                    String c = city.getText().toString();
                    String p = phone.getText().toString();
                    double la = 0, ln = 0;
                    try {
                        la = Double.parseDouble(lat.getText().toString());
                        ln = Double.parseDouble(lng.getText().toString());
                    } catch (Exception ignored) {}

                    if (n.isEmpty() || a.isEmpty() || c.isEmpty()) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.addBranch(n, a, c, la, ln, p);
                    load();
                    Toast.makeText(this, "Branch added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        List<Branch> list = dao.getBranches();
        adapter.submit(list);
        findViewById(R.id.txtEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showOptions(Branch branch) {
        new AlertDialog.Builder(this)
                .setTitle(branch.name)
                .setItems(new String[]{"View Staff", "View Spare Parts", "Update", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, AdminStaffActivity.class);
                        intent.putExtra("filter_branch", branch.name);
                        startActivity(intent);
                    } else if (which == 1) {
                        Intent intent = new Intent(this, StaffPartsActivity.class);
                        intent.putExtra("filter_branch", branch.name);
                        startActivity(intent);
                    } else if (which == 2) {
                        showUpdateDialog(branch);
                    } else if (which == 3) {
                        dao.deleteBranch(branch.id);
                        load();
                        Toast.makeText(this, "Branch deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showUpdateDialog(Branch branch) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_branch, null);
        EditText name = view.findViewById(R.id.inputName);
        EditText addr = view.findViewById(R.id.inputAddress);
        EditText city = view.findViewById(R.id.inputCity);
        EditText phone = view.findViewById(R.id.inputPhone);
        EditText lat = view.findViewById(R.id.inputLat);
        EditText lng = view.findViewById(R.id.inputLng);

        name.setText(branch.name);
        addr.setText(branch.address);
        city.setText(branch.city);
        phone.setText(branch.phone);
        lat.setText(String.valueOf(branch.latitude));
        lng.setText(String.valueOf(branch.longitude));

        new AlertDialog.Builder(this)
                .setTitle("Update Branch")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String n = name.getText().toString();
                    String a = addr.getText().toString();
                    String c = city.getText().toString();
                    String p = phone.getText().toString();
                    double la = branch.latitude, ln = branch.longitude;
                    try {
                        la = Double.parseDouble(lat.getText().toString());
                        ln = Double.parseDouble(lng.getText().toString());
                    } catch (Exception ignored) {}

                    if (n.isEmpty() || a.isEmpty() || c.isEmpty()) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.updateBranch(branch.id, n, a, c, la, ln, p);
                    load();
                    Toast.makeText(this, "Branch updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
