package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.model.Category;
import com.techfix.app.model.SparePart;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StaffPartsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<SparePart> adapter;
    private SessionManager session;
    private List<SparePart> allParts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<Branch> branches = new ArrayList<>();
    private String mode;

    @OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_spare_parts), true);
        
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        mode = getIntent().getStringExtra("mode");
        categories = dao.getCategories();
        branches = dao.getBranches();

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        List<String> catNames = new ArrayList<>();
        catNames.add(getString(R.string.all_categories));
        for (Category c : categories) catNames.add(c.name);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catNames);
        spinnerFilter.setAdapter(spinAdapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filter(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            String bName = item.branchId == 0 ? "Global Catalog" : item.branchName;
            subtitle.setText(getString(R.string.branch_category_sep, bName, item.categoryName));
            meta.setText(getString(R.string.label_qty, item.quantity));

            // Highlight Global Catalog items with a subtle background color
            if (item.branchId == 0) {
                ((View) title.getParent()).setBackgroundColor(androidx.core.content.ContextCompat.getColor(this, R.color.background_light));
            } else {
                ((View) title.getParent()).setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }
        }, item -> {
            if ("request".equals(mode)) {
                showStockRequestDialog(item.name);
            } else if (session.isAdmin() || session.isManager()) {
                showOptions(item);
            } else {
                adjustQty(item);
            }
        });

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        load();

        if ("request".equals(mode)) {
            findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
            findViewById(R.id.fabAdd).setOnClickListener(v -> showStockRequestDialog(null));
        } else if (session.isAdmin() || session.isManager()) {
            findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
            findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
        } else {
            findViewById(R.id.fabAdd).setVisibility(View.GONE);
        }
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String cat = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();
        String preBranch = getIntent().getStringExtra("filter_branch");

        List<SparePart> filtered = new ArrayList<>();
        for (SparePart p : allParts) {
            boolean matchesQuery = p.name.toLowerCase().contains(query) || (p.categoryName != null && p.categoryName.toLowerCase().contains(query));
            boolean matchesCat = cat.equals(getString(R.string.all_categories)) || (p.categoryName != null && p.categoryName.equals(cat));
            boolean matchesBranch = preBranch == null || (p.branchName != null && p.branchName.equals(preBranch));

            if (matchesQuery && matchesCat && matchesBranch) {
                filtered.add(p);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_part, null);
        Spinner branchSpin = view.findViewById(R.id.spinnerBranch);
        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);
        EditText qty = view.findViewById(R.id.inputQuantity);

        ArrayAdapter<Branch> bAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches);
        branchSpin.setAdapter(bAdapter);

        if (session.isManager()) {
            for (int i = 0; i < branches.size(); i++) {
                if (branches.get(i).id == session.getBranchId()) {
                    branchSpin.setSelection(i);
                    branchSpin.setEnabled(false);
                    break;
                }
            }
        }

        ArrayAdapter<Category> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(cAdapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_part)
                .setView(view)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    Branch b = (Branch) branchSpin.getSelectedItem();
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String qStr = qty.getText().toString();
                    if (b != null && c != null && !n.isEmpty() && !qStr.isEmpty()) {
                        dao.addPart(n, Integer.parseInt(qStr), b.id, c.id);
                        load();
                        Toast.makeText(this, R.string.msg_part_added, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void load() {
        List<SparePart> all = dao.getSpareParts();
        allParts = new ArrayList<>();
        
        // Include "Global" parts (branch_id=0) so they can be seen/requested
        for (SparePart p : all) {
            if (p.branchId == 0) {
                allParts.add(p);
            } else if ("ADMIN".equals(session.getRole())) {
                allParts.add(p);
            } else if (p.branchId == session.getBranchId()) {
                allParts.add(p);
            }
        }
        filter();
    }

    private void adjustQty(SparePart part) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(part.quantity));
        input.setSelection(input.getText().length());
        input.setPadding(40, 40, 40, 40);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_update_qty, part.name))
                .setView(input)
                .setPositiveButton(R.string.action_update, (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        dao.updatePartQuantity(part.id, Integer.parseInt(val));
                        load();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showOptions(SparePart part) {
        if (session.isManager() && part.branchId != session.getBranchId() && part.branchId != 0) {
            // Manager can only manage parts in their branch or request from global catalog
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(part.name)
                .setItems(R.array.part_options, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(part);
                    } else if (which == 1) {
                        adjustQty(part);
                    } else if (which == 2) {
                        dao.deletePart(part.id);
                        load();
                        Toast.makeText(this, R.string.msg_part_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showUpdateDialog(SparePart part) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_part, null);
        Spinner branchSpin = view.findViewById(R.id.spinnerBranch);
        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);
        EditText qty = view.findViewById(R.id.inputQuantity);

        ArrayAdapter<Branch> bAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches);
        branchSpin.setAdapter(bAdapter);

        ArrayAdapter<Category> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(cAdapter);

        name.setText(part.name);
        qty.setText(String.valueOf(part.quantity));
        
        for (int i = 0; i < branches.size(); i++) {
            if (branches.get(i).id == part.branchId) {
                branchSpin.setSelection(i);
                if (session.isManager()) branchSpin.setEnabled(false);
                break;
            }
        }
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).id == part.categoryId) {
                catSpin.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_update_part)
                .setView(view)
                .setPositiveButton(R.string.action_update, (dialog, which) -> {
                    Branch b = (Branch) branchSpin.getSelectedItem();
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String qStr = qty.getText().toString();
                    if (b != null && c != null && !n.isEmpty() && !qStr.isEmpty()) {
                        dao.updatePart(part.id, n, Integer.parseInt(qStr), b.id, c.id);
                        load();
                        Toast.makeText(this, R.string.msg_part_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showStockRequestDialog(String itemName) {
        View v = getLayoutInflater().inflate(R.layout.dialog_stock_request, null);
        EditText inputItem = v.findViewById(R.id.inputItem);
        EditText inputQty = v.findViewById(R.id.inputQty);
        EditText inputReason = v.findViewById(R.id.inputReason);

        if (itemName != null) {
            inputItem.setText(itemName);
            inputQty.requestFocus();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_request_stock)
                .setView(v)
                .setPositiveButton(R.string.action_submit, (dialog, which) -> {
                    String item = inputItem.getText().toString().trim();
                    String qtyStr = inputQty.getText().toString().trim();
                    String reason = inputReason.getText().toString().trim();

                    if (item.isEmpty() || qtyStr.isEmpty()) return;
                    int qty = Integer.parseInt(qtyStr);
                    
                    dao.addInventoryRequest(session.getUserId(), session.getBranchId(), item, qty, reason);
                    Toast.makeText(this, R.string.msg_request_submitted, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
