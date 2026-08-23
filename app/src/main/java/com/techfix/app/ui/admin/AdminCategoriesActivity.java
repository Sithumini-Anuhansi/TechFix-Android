package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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
import com.techfix.app.model.Category;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class AdminCategoriesActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Category> adapter;
    private List<Category> allCategories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dao = new TechFixDao(this);
        UiHelper.setupToolbar(this, getString(R.string.title_manage_categories), true);

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.spinnerFilter).setVisibility(View.GONE);
        EditText inputSearch = findViewById(R.id.inputSearch);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.description);
            meta.setText(getString(R.string.label_id, item.id));
        }, item -> {
            new AlertDialog.Builder(this)
                    .setTitle(item.name)
                    .setItems(new String[]{getString(R.string.action_edit), getString(R.string.action_delete)}, (dialog, which) -> {
                        if (which == 0) showDialog(item);
                        else {
                            dao.deleteCategory(item.id);
                            load();
                        }
                    }).show();
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> showDialog(null));

        load();
    }

    private void load() {
        allCategories = dao.getCategories();
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        List<Category> filtered = new ArrayList<>();
        for (Category c : allCategories) {
            if (c.name.toLowerCase().contains(query) || c.description.toLowerCase().contains(query)) {
                filtered.add(c);
            }
        }
        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showDialog(Category item) {
        View v = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText inputName = v.findViewById(R.id.inputName);
        EditText inputDesc = v.findViewById(R.id.inputDesc);

        if (item != null) {
            inputName.setText(item.name);
            inputDesc.setText(item.description);
        }

        new AlertDialog.Builder(this)
                .setTitle(item == null ? R.string.dialog_add_category : R.string.dialog_update_category)
                .setView(v)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();
                    if (name.isEmpty()) return;

                    if (item == null) dao.addCategory(name, desc);
                    else dao.updateCategory(item.id, name, desc);
                    load();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
