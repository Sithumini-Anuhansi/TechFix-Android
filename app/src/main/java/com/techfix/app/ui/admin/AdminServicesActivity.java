package com.techfix.app.ui.admin;

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
import com.techfix.app.model.Category;
import com.techfix.app.model.Service;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

public class AdminServicesActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Service> adapter;
    private List<Service> allServices = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Manage Services", true);

        dao = new TechFixDao(this);
        categories = dao.getCategories();

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

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.description);
            meta.setText(UiHelper.money(item.price));
        }, this::showOptions);

        recycler.setAdapter(adapter);
        load();

        findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
        findViewById(R.id.fabAdd).setOnClickListener(v -> showAddDialog());
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String cat = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Service> filtered = new ArrayList<>();
        for (Service s : allServices) {
            boolean matchesQuery = s.name.toLowerCase().contains(query) || s.description.toLowerCase().contains(query);
            boolean matchesCat = cat.equals("All Categories") || (s.categoryName != null && s.categoryName.equals(cat));

            if (matchesQuery && matchesCat) {
                filtered.add(s);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);
        EditText price = view.findViewById(R.id.inputPrice);
        EditText desc = view.findViewById(R.id.inputDescription);

        ArrayAdapter<Category> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Add New Service")
                .setView(view)
                .setPositiveButton("Add", (dialog, which) -> {
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String pStr = price.getText().toString();
                    String d = desc.getText().toString();
                    if (c != null && !n.isEmpty() && !pStr.isEmpty()) {
                        dao.addService(c.id, n, Double.parseDouble(pStr), d);
                        load();
                        Toast.makeText(this, "Service added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void load() {
        allServices = dao.searchServices("");
        filter();
    }

    private void showOptions(Service service) {
        new AlertDialog.Builder(this)
                .setTitle(service.name)
                .setItems(new String[]{"Update", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showUpdateDialog(service);
                    } else if (which == 1) {
                        dao.deleteService(service.id);
                        load();
                        Toast.makeText(this, "Service deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void showUpdateDialog(Service service) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_service, null);
        Spinner catSpin = view.findViewById(R.id.spinnerCategory);
        EditText name = view.findViewById(R.id.inputName);
        EditText price = view.findViewById(R.id.inputPrice);
        EditText desc = view.findViewById(R.id.inputDescription);

        name.setText(service.name);
        price.setText(String.valueOf(service.price));
        desc.setText(service.description);

        ArrayAdapter<Category> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        catSpin.setAdapter(catAdapter);
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).id == service.categoryId) {
                catSpin.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Update Service")
                .setView(view)
                .setPositiveButton("Update", (dialog, which) -> {
                    Category c = (Category) catSpin.getSelectedItem();
                    String n = name.getText().toString();
                    String pStr = price.getText().toString();
                    String d = desc.getText().toString();
                    if (c != null && !n.isEmpty() && !pStr.isEmpty()) {
                        dao.updateService(service.id, c.id, n, Double.parseDouble(pStr), d);
                        load();
                        Toast.makeText(this, "Service updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
