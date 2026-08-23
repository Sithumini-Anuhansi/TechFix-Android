package com.techfix.app.ui.admin;

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
import com.techfix.app.model.Appointment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.ui.staff.StaffAppointmentDetailActivity;
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@OptIn(markerClass = ExperimentalBadgeUtils.class)
public class AdminPendingRequestsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Map<String, Object>> adapter;
    private List<Map<String, Object>> allItems = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        
        session = new SessionManager(this);
        String mode = getIntent().getStringExtra("mode");
        String title = session.isManager() ? getString(R.string.title_pending_repairs) : getString(R.string.title_pending_requests);
        UiHelper.setupToolbar(this, title, true);
        
        dao = new TechFixDao(this);

        if (session.isManager()) {
            findViewById(R.id.filterLayout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
            EditText inputSearch = findViewById(R.id.inputSearch);
            Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

            String[] types = {getString(R.string.all_types), getString(R.string.type_stock), getString(R.string.type_repair)};
            ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, types);
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
        }

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SimpleAdapter<>((item, image, txtTitle, txtSubtitle, txtMeta) -> {
            txtTitle.setText((String) item.get("title"));
            txtSubtitle.setText((String) item.get("subtitle"));
            txtMeta.setText((String) item.get("type"));
            image.setImageResource(getString(R.string.type_stock).equals(item.get("type")) ? R.drawable.ic_notifications : R.drawable.ic_build);
        }, item -> {
            if (getString(R.string.type_stock).equals(item.get("type"))) {
                startActivity(new Intent(this, AdminInventoryRequestsActivity.class));
            } else {
                Intent intent = new Intent(this, StaffAppointmentDetailActivity.class);
                intent.putExtra("appointmentId", (long) item.get("id"));
                if (item.containsKey("branchId")) {
                    intent.putExtra("branchId", (long) item.get("branchId"));
                }
                startActivity(intent);
            }
        });
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        allItems.clear();
        long branchId = session.isManager() ? session.getBranchId() : 0;
        String mode = getIntent().getStringExtra("mode");
        
        // Load Pending Stock Requests (Only for Admins or if not in REPAIRS_ONLY mode)
        if (!"REPAIRS_ONLY".equals(mode)) {
            List<Map<String, Object>> stock = dao.getInventoryRequests(branchId);
            for (Map<String, Object> s : stock) {
                if (getString(R.string.status_pending).equals(s.get("status"))) {
                    Object qtyObj = s.get("quantity");
                    int qty = 0;
                    if (qtyObj instanceof Integer) qty = (Integer) qtyObj;
                    else if (qtyObj instanceof Long) qty = ((Long) qtyObj).intValue();

                    Map<String, Object> map = new HashMap<>();
                    map.put("id", s.get("id"));
                    map.put("title", getString(R.string.label_qty_x, qty, s.get("item_name")));
                    map.put("subtitle", getString(R.string.label_history_meta, s.get("branch_name"), s.get("manager_name")));
                    map.put("type", getString(R.string.type_stock));
                    allItems.add(map);
                }
            }
        }

        // Load Pending Repair Requests
        List<Appointment> repairs = dao.getAllAppointments("PENDING", branchId);
        for (Appointment a : repairs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", a.id);
            map.put("title", getString(R.string.label_service_info, a.serviceName, a.customerName));
            map.put("subtitle", a.createdAt);
            map.put("branchId", a.branchId);
            map.put("type", getString(R.string.type_repair));
            allItems.add(map);
        }
        
        filter();
    }

    private void filter() {
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);
        
        String query = inputSearch.getText() != null ? inputSearch.getText().toString().toLowerCase() : "";
        String type = (spinnerFilter.getSelectedItem() != null) ? spinnerFilter.getSelectedItem().toString() : getString(R.string.all_types);

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> item : allItems) {
            String title = (String) item.get("title");
            String itemType = (String) item.get("type");
            
            boolean matchesQuery = title != null && title.toLowerCase().contains(query);
            boolean matchesType = type.equals(getString(R.string.all_types)) || type.equals(itemType);

            if (matchesQuery && matchesType) {
                filtered.add(item);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
