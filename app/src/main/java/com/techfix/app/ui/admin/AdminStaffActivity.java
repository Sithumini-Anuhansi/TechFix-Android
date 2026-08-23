package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.model.User;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminStaffActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<User> adapter;
    private List<Branch> branches = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Manage Staff", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        dao = new TechFixDao(this);
        branches = dao.getBranches();
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.email + " · " + item.phone);
            meta.setText(item.role + (item.branchName != null ? " (" + item.branchName + ")" : ""));
        }, this::showOptions);

        recycler.setAdapter(adapter);
        load();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_staff, null);
        EditText name = view.findViewById(R.id.inputName);
        EditText email = view.findViewById(R.id.inputEmail);
        EditText pass = view.findViewById(R.id.inputPassword);
        EditText phone = view.findViewById(R.id.inputPhone);
        Spinner spinBranch = view.findViewById(R.id.spinBranch);

        ArrayAdapter<Branch> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBranch.setAdapter(branchAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Staff")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    String n = name.getText().toString();
                    String e = email.getText().toString();
                    String p = pass.getText().toString();
                    String ph = phone.getText().toString();
                    Branch b = (Branch) spinBranch.getSelectedItem();
                    if (n.isEmpty() || e.isEmpty() || p.isEmpty() || b == null) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.addUser(n, e, p, ph, "STAFF", b.id);
                    load();
                    Toast.makeText(this, "Staff added", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        List<User> list = dao.getStaff();
        adapter.submit(list);
        findViewById(R.id.txtEmpty).setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showOptions(User user) {
        new AlertDialog.Builder(this)
                .setTitle(user.name)
                .setItems(new String[]{"Update", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(user);
                    } else if (which == 1) {
                        dao.deleteUser(user.id);
                        load();
                        Toast.makeText(this, "Staff deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showUpdateDialog(User user) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_staff, null);
        EditText name = view.findViewById(R.id.inputName);
        EditText email = view.findViewById(R.id.inputEmail);
        EditText pass = view.findViewById(R.id.inputPassword);
        EditText phone = view.findViewById(R.id.inputPhone);
        Spinner spinBranch = view.findViewById(R.id.spinBranch);

        // Hide password for update
        view.findViewById(R.id.layoutPassword).setVisibility(View.GONE);

        name.setText(user.name);
        email.setText(user.email);
        phone.setText(user.phone);

        ArrayAdapter<Branch> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBranch.setAdapter(branchAdapter);
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).id == user.branchId) {
                spinBranch.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Staff")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    String n = name.getText().toString();
                    String e = email.getText().toString();
                    String ph = phone.getText().toString();
                    Branch b = (Branch) spinBranch.getSelectedItem();
                    if (n.isEmpty() || e.isEmpty() || b == null) {
                        Toast.makeText(this, "Fill required fields", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.updateUser(user.id, n, e, ph, user.role, b.id);
                    load();
                    Toast.makeText(this, "Staff updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
