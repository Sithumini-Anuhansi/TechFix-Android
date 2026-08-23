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
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Category;
import com.techfix.app.model.SparePart;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminSparePartsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<SparePart> adapter;
    private List<SparePart> allParts = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @OptIn(markerClass = com.google.android.material.badge.ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_spare_parts), true);
        
        dao = new TechFixDao(this);
        categories = dao.getCategories();

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
            subtitle.setText(item.categoryName);
            meta.setVisibility(View.VISIBLE);
            meta.setText(getString(R.string.label_qty_total, item.quantity));
        }, this::showOptions);

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

        List<SparePart> filtered = new ArrayList<>();
        for (SparePart p : allParts) {
            boolean matchesQuery = p.name.toLowerCase().contains(query);
            boolean matchesCat = cat.equals(getString(R.string.all_categories)) || (p.categoryName != null && p.categoryName.equals(cat));

            if (matchesQuery && matchesCat) {
                filtered.add(p);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void load() {
        allParts = dao.getGlobalSpareParts();
        filter();
    }

    private void showOptions(SparePart part) {
        new AlertDialog.Builder(this)
                .setTitle(part.name)
                .setItems(new String[]{getString(R.string.action_update), getString(R.string.action_delete)}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(part);
                    } else if (which == 1) {
                        dao.deletePart(part.id);
                        load();
                        Toast.makeText(this, R.string.msg_part_deleted, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_part, null);
        view.findViewById(R.id.spinnerBranch).setVisibility(View.GONE);
        ((View) view.findViewById(R.id.inputQuantity).getParent()).setVisibility(View.GONE);
        
        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);

        ArrayAdapter<Category> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(cAdapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_part)
                .setView(view)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    if (c != null && !n.isEmpty()) {
                        dao.addGlobalPart(c.id, n);
                        load();
                        Toast.makeText(this, R.string.msg_part_added, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showUpdateDialog(SparePart part) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_part, null);
        view.findViewById(R.id.spinnerBranch).setVisibility(View.GONE);
        ((View) view.findViewById(R.id.inputQuantity).getParent()).setVisibility(View.GONE);

        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);

        name.setText(part.name);
        
        ArrayAdapter<Category> cAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(cAdapter);
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
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    if (c != null && !n.isEmpty()) {
                        dao.updateGlobalPart(part.id, c.id, n);
                        load();
                        Toast.makeText(this, R.string.msg_part_updated, Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
