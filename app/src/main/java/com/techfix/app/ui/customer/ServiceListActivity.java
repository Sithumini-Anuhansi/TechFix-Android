package com.techfix.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Category;
import com.techfix.app.model.Service;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class ServiceListActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Service> adapter;
    private TextView empty;
    private List<Service> allItems = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_repair_services), true);
        dao = new TechFixDao(this);
        empty = findViewById(R.id.txtEmpty);
        categories = dao.getCategories();

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        List<String> catNames = new ArrayList<>();
        catNames.add(getString(R.string.all_categories));
        for (Category c : categories) catNames.add(c.name);
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, catNames);
        spinnerFilter.setAdapter(spinAdapter);

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(getString(R.string.label_service_info, item.categoryName, item.description));
            meta.setText(UiHelper.money(this, item.price));

            image.setVisibility(View.VISIBLE);
            if (item.name.toLowerCase().contains("iphone")) {
                image.setImageResource(R.drawable.ic_phone);
            } else if (item.name.toLowerCase().contains("laptop") || item.categoryName.equalsIgnoreCase("Computer")) {
                image.setImageResource(R.drawable.ic_computer);
            } else {
                image.setImageResource(R.drawable.ic_build);
            }
        }, item -> {
            Intent intent = new Intent(this, ServiceDetailActivity.class);
            intent.putExtra("serviceId", item.id);
            startActivity(intent);
        });

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filter(); }
            @Override public void afterTextChanged(Editable s) {}
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { filter(); }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        load();
    }

    private void load() {
        allItems = dao.searchServices("");
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String cat = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Service> filtered = new ArrayList<>();
        for (Service s : allItems) {
            boolean matchesQuery = s.name.toLowerCase().contains(query) || s.description.toLowerCase().contains(query);
            boolean matchesCat = cat.equals(getString(R.string.all_categories)) || s.categoryName.equals(cat);

            if (matchesQuery && matchesCat) {
                filtered.add(s);
            }
        }

        adapter.submit(filtered);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText(R.string.msg_no_services);
    }
}
