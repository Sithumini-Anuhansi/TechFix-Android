package com.techfix.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Service;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class ServiceListActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Service> adapter;
    private TextView empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Repair services", true);
        dao = new TechFixDao(this);
        empty = findViewById(R.id.txtEmpty);

        adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.categoryName + " — " + item.description);
            meta.setText(UiHelper.money(item.price));
        }, item -> {
            Intent intent = new Intent(this, ServiceDetailActivity.class);
            intent.putExtra("serviceId", item.id);
            startActivity(intent);
        });

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        TextInputEditText search = findViewById(R.id.inputSearch);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                load(s == null ? "" : s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        load("");
    }

    private void load(String query) {
        List<Service> list = dao.searchServices(query);
        adapter.submit(list);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText("No matching services");
    }
}
