package com.techfix.app.ui.staff;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.techfix.app.model.Appointment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class StaffAppointmentsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Appointment> adapter;
    private List<Appointment> allItems = new ArrayList<>();
    private String filterStatus;
    private SessionManager session;

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_repairs), true);
        
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        filterStatus = getIntent().getStringExtra("filter_status");

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {
                getString(R.string.all_items),
                getString(R.string.status_pending),
                getString(R.string.status_assigned),
                getString(R.string.status_in_progress),
                getString(R.string.status_completed),
                getString(R.string.status_cancelled)
        };
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerFilter.setAdapter(spinAdapter);

        if (filterStatus != null) {
            for (int i = 0; i < statuses.length; i++) {
                if (statuses[i].equalsIgnoreCase(filterStatus)) {
                    spinnerFilter.setSelection(i);
                    break;
                }
            }
        }

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

        SwipeRefreshLayout swipe = findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(() -> {
            load();
            swipe.setRefreshing(false);
        });

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(getString(R.string.label_service_info, item.serviceName, item.customerName));
            String branchText = (item.branchName == null ? getString(R.string.status_unassigned) : item.branchName);
            String techText = (item.technicianName == null ? "Unassigned" : item.technicianName);
            subtitle.setText(getString(R.string.label_history_meta, branchText + " (" + techText + ")", item.createdAt));
            meta.setText(item.status);
        }, item -> {
            Intent intent = new Intent(this, StaffAppointmentDetailActivity.class);
            intent.putExtra("appointmentId", item.id);
            startActivity(intent);
        });
        recycler.setAdapter(adapter);
    }

    private void load() {
        String status = filterStatus;
        if (status == null || status.equalsIgnoreCase(getString(R.string.all_items)) || status.equalsIgnoreCase("ALL")) {
            status = "ALL";
        }
        
        if (session.isStaff()) {
            allItems = dao.getTechnicianAppointments(session.getUserId(), status);
        } else if (session.isManager()) {
            allItems = dao.getAllAppointments(status, session.getBranchId());
        } else {
            // Admin or other
            long branchId = getIntent().getLongExtra("branch_id", 0);
            allItems = dao.getAllAppointments(status, branchId);
        }

        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String status = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();
        
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : allItems) {
            boolean matchesQuery = a.customerName.toLowerCase().contains(query) || 
                                 a.serviceName.toLowerCase().contains(query);
            boolean matchesStatus = status.equals(getString(R.string.all_items)) || a.status.equalsIgnoreCase(status);
            
            // Branch isolation check for Managers just in case (though load() should handle it)
            boolean matchesBranch = !session.isManager() || a.branchId == session.getBranchId();

            if (matchesQuery && matchesStatus && matchesBranch) {
                filtered.add(a);
            }
        }
        
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.submit(filtered);
    }
}
