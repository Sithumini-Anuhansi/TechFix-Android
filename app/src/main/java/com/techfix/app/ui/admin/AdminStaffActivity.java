package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.model.User;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class AdminStaffActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<User> adapter;
    private List<User> allStaff = new ArrayList<>();
    private List<Branch> branches = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_manage_staff), true);

        dao = new TechFixDao(this);
        branches = dao.getBranches();

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        List<String> branchNames = new ArrayList<>();
        branchNames.add(getString(R.string.all_branches));
        for (Branch b : branches) branchNames.add(b.name);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branchNames);
        spinnerFilter.setAdapter(spinAdapter);

        String preFilter = getIntent().getStringExtra("filter_branch");
        if (preFilter != null) {
            for (int i = 0; i < branchNames.size(); i++) {
                if (branchNames.get(i).equals(preFilter)) {
                    spinnerFilter.setSelection(i);
                    break;
                }
            }
        }

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filter(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(getString(R.string.label_history_meta, item.email, item.phone));
            meta.setText(item.role + (item.branchName != null ? " (" + item.branchName + ")" : ""));
        }, this::showOptions);

        recycler.setAdapter(adapter);
        load();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> showAddDialog());
    }

    private void load() {
        allStaff = dao.getStaff();
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String branch = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<User> filtered = new ArrayList<>();
        for (User u : allStaff) {
            boolean matchesQuery = u.name.toLowerCase().contains(query) || u.email.toLowerCase().contains(query);
            boolean matchesBranch = branch.equals(getString(R.string.all_branches)) || (u.branchName != null && u.branchName.equals(branch));

            if (matchesQuery && matchesBranch) {
                filtered.add(u);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_staff, null);
        EditText name = view.findViewById(R.id.inputName);
        EditText email = view.findViewById(R.id.inputEmail);
        EditText pass = view.findViewById(R.id.inputPassword);
        EditText phone = view.findViewById(R.id.inputPhone);
        Spinner spinBranch = view.findViewById(R.id.spinBranch);
        Spinner spinRole = view.findViewById(R.id.spinRole);

        String[] roles = {"STAFF", "MANAGER"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRole.setAdapter(roleAdapter);

        ArrayAdapter<Branch> branchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, branches);
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinBranch.setAdapter(branchAdapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_staff)
                .setView(view)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String n = name.getText().toString();
                    String e = email.getText().toString();
                    String p = pass.getText().toString();
                    String ph = phone.getText().toString();
                    String r = spinRole.getSelectedItem().toString();
                    Branch b = (Branch) spinBranch.getSelectedItem();
                    if (n.isEmpty() || e.isEmpty() || p.isEmpty() || b == null) {
                        Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.addUser(n, e, p, ph, r, b.id);
                    load();
                    Toast.makeText(this, R.string.msg_staff_added, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showOptions(User user) {
        new AlertDialog.Builder(this)
                .setTitle(user.name)
                .setItems(new String[]{getString(R.string.action_update), getString(R.string.action_delete)}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(user);
                    } else if (which == 1) {
                        dao.deleteUser(user.id);
                        load();
                        Toast.makeText(this, R.string.msg_staff_deleted, Toast.LENGTH_SHORT).show();
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
        Spinner spinRole = view.findViewById(R.id.spinRole);

        // Hide password for update
        view.findViewById(R.id.layoutPassword).setVisibility(View.GONE);

        name.setText(user.name);
        email.setText(user.email);
        phone.setText(user.phone);

        String[] roles = {"STAFF", "MANAGER"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinRole.setAdapter(roleAdapter);
        for (int i = 0; i < roles.length; i++) {
            if (roles[i].equals(user.role)) {
                spinRole.setSelection(i);
                break;
            }
        }

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
                .setTitle(R.string.dialog_update_staff)
                .setView(view)
                .setPositiveButton(R.string.action_update, (dialog, which) -> {
                    String n = name.getText().toString();
                    String e = email.getText().toString();
                    String ph = phone.getText().toString();
                    String r = spinRole.getSelectedItem().toString();
                    Branch b = (Branch) spinBranch.getSelectedItem();
                    if (n.isEmpty() || e.isEmpty() || b == null) {
                        Toast.makeText(this, R.string.msg_fill_fields, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dao.updateUser(user.id, n, e, ph, r, b.id);
                    load();
                    Toast.makeText(this, R.string.msg_staff_updated, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
