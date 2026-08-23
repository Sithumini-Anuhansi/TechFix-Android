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
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Appointment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class MyAppointmentsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Appointment> adapter;
    private List<Appointment> allItems = new ArrayList<>();
    private TextView empty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "My appointments", true);
        
        dao = new TechFixDao(this);
        empty = findViewById(R.id.txtEmpty);

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {"All Statuses", "PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED", "CANCELLED"};
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerFilter.setAdapter(spinAdapter);

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.serviceName);
            subtitle.setText((item.branchName == null ? "Unassigned" : item.branchName) + " — " + item.createdAt);
            meta.setText(item.status);
        }, item -> {
            Intent intent = new Intent(this, AppointmentTrackActivity.class);
            intent.putExtra("appointmentId", item.id);
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
        long userId = new SessionManager(this).getUserId();
        allItems = dao.getAppointmentsForCustomer(userId, true);
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String status = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : allItems) {
            boolean matchesQuery = a.serviceName.toLowerCase().contains(query) || 
                                 (a.branchName != null && a.branchName.toLowerCase().contains(query));
            boolean matchesStatus = status.equals("All Statuses") || a.status.equalsIgnoreCase(status);

            if (matchesQuery && matchesStatus) {
                filtered.add(filtered.size(), a);
            }
        }

        adapter.submit(filtered);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        empty.setText("No matching appointments");
    }
}
