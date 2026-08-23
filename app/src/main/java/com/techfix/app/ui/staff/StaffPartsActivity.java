package com.techfix.app.ui.staff;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Spare parts", true);
        
        android.view.ViewGroup searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setVisibility(View.VISIBLE);
        android.widget.EditText searchInput = findViewById(R.id.inputSearch);
        searchInput.setHint("Search by name or category...");
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { load(); }
            public void afterTextChanged(android.text.Editable s) {}
        });

        dao = new TechFixDao(this);
        session = new SessionManager(this);

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.branchName + " · " + item.categoryName);
            meta.setText("Qty: " + item.quantity);
        }, this::adjustQty);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        load();

        findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
    }

    private void showAddDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_add_part, null);
        android.widget.Spinner branchSpin = view.findViewById(R.id.spinnerBranch);
        android.widget.Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        android.widget.EditText name = view.findViewById(R.id.inputName);
        android.widget.EditText qty = view.findViewById(R.id.inputQuantity);

        List<com.techfix.app.model.Branch> branches = dao.getBranches();
        android.widget.ArrayAdapter<com.techfix.app.model.Branch> bAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, branches);
        branchSpin.setAdapter(bAdapter);

        List<com.techfix.app.model.Category> cats = dao.getCategories();
        android.widget.ArrayAdapter<com.techfix.app.model.Category> cAdapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cats);
        catSpin.setAdapter(cAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Spare Part")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    com.techfix.app.model.Branch b = (com.techfix.app.model.Branch) branchSpin.getSelectedItem();
                    com.techfix.app.model.Category c = (com.techfix.app.model.Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String qStr = qty.getText().toString();
                    if (b != null && c != null && !n.isEmpty() && !qStr.isEmpty()) {
                        dao.addPart(n, Integer.parseInt(qStr), b.id, c.id);
                        load();
                        Toast.makeText(this, "Part added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        List<SparePart> all = dao.getSpareParts();
        List<SparePart> list = new ArrayList<>();
        if ("ADMIN".equals(session.getRole())) {
            list = all;
        } else {
            for (SparePart p : all) {
                if (p.branchId == session.getBranchId()) list.add(p);
            }
        }
        adapter.submit(list);
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void adjustQty(SparePart part) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(part.quantity));
        input.setSelection(input.getText().length());

        new AlertDialog.Builder(this)
                .setTitle("Update Quantity: " + part.name)
                .setView(input)
                .setPositiveButton("Update", (dialog, which) -> {
                    String val = input.getText().toString();
                    if (!val.isEmpty()) {
                        dao.updatePartQuantity(part.id, Integer.parseInt(val));
                        load();
                    }
                })
                .setNeutralButton("Delete", (dialog, which) -> {
                    dao.deletePart(part.id);
                    load();
                    Toast.makeText(this, "Part deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
