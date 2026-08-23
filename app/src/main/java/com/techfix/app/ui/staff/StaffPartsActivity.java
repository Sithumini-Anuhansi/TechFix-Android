package com.techfix.app.ui.staff;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Spare parts", true);
        
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        categories = dao.getCategories();
        branches = dao.getBranches();

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        List<String> catNames = new ArrayList<>();
        catNames.add("All Categories");
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

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String cat = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();
        String preBranch = getIntent().getStringExtra("filter_branch");

        List<SparePart> filtered = new ArrayList<>();
        for (SparePart p : allParts) {
            boolean matchesQuery = p.name.toLowerCase().contains(query) || (p.categoryName != null && p.categoryName.toLowerCase().contains(query));
            boolean matchesCat = cat.equals("All Categories") || (p.categoryName != null && p.categoryName.equals(cat));
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

        ArrayAdapter<Category> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(cAdapter);

        new AlertDialog.Builder(this)
                .setTitle("Add Spare Part")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    Branch b = (Branch) branchSpin.getSelectedItem();
                    Category c = (Category) catSpin.getSelectedItem();
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
        allParts = new ArrayList<>();
        if ("ADMIN".equals(session.getRole())) {
            allParts = all;
        } else {
            for (SparePart p : all) {
                if (p.branchId == session.getBranchId()) allParts.add(p);
            }
        }
        filter();
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
