package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import com.techfix.app.model.Category;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class AdminCategoriesActivity extends AppCompatActivity {
    private TechFixDao dao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dao = new TechFixDao(this);
        UiHelper.setupToolbar(this, "Manage Categories", true);

        findViewById(R.id.searchLayout).setVisibility(View.GONE);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(v -> showDialog(null));

        load();
    }

    private void load() {
        List<Category> list = dao.getCategories();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Category> adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.description);
            meta.setText("ID: " + item.id);
        }, item -> {
            new AlertDialog.Builder(this)
                    .setTitle(item.name)
                    .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                        if (which == 0) showDialog(item);
                        else {
                            dao.deleteCategory(item.id);
                            load();
                        }
                    }).show();
        });
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setAdapter(adapter);
        adapter.submit(list);
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
                .setTitle(item == null ? "Add Category" : "Edit Category")
                .setView(v)
                .setPositiveButton("Save", (dialog, which) -> {
                    String name = inputName.getText().toString().trim();
                    String desc = inputDesc.getText().toString().trim();
                    if (name.isEmpty()) return;

                    if (item == null) dao.addCategory(name, desc);
                    else dao.updateCategory(item.id, name, desc);
                    load();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
