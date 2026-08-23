package com.techfix.app.ui.staff;

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
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import java.util.Map;

public class StaffStockRequestsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Map<String, Object>> adapter;
    private List<Map<String, Object>> allRequests = new ArrayList<>();
    private SessionManager session;

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dao = new TechFixDao(this);
        session = new SessionManager(this);
        UiHelper.setupToolbar(this, getString(R.string.title_stock_requests), true);

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {
                getString(R.string.all_statuses),
                getString(R.string.status_pending),
                getString(R.string.status_approved),
                getString(R.string.status_rejected)
        };
        ArrayAdapter<String> spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, statuses);
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

        SwipeRefreshLayout swipe = findViewById(R.id.swipeRefresh);
        swipe.setOnRefreshListener(() -> {
            load();
            swipe.setRefreshing(false);
        });

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            Object qtyObj = item.get("quantity");
            int qty = 0;
            if (qtyObj instanceof Integer) qty = (Integer) qtyObj;
            else if (qtyObj instanceof Long) qty = ((Long) qtyObj).intValue();

            title.setText(getString(R.string.label_qty_x, qty, item.get("item_name")));
            subtitle.setText(getString(R.string.label_history_meta, item.get("branch_name"), (String) item.get("created_at")));
            meta.setText((String) item.get("status"));
        }, item -> {
            // Optional: Show details or reason
            String reason = (String) item.get("reason");
            if (reason != null && !reason.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_request_details)
                        .setMessage(getString(R.string.label_reason, reason))
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);

        if (session.isStaff() || session.isManager()) {
            findViewById(R.id.fabAdd).setVisibility(View.VISIBLE);
            findViewById(R.id.fabAdd).setOnClickListener(v -> {
                Intent intent = new Intent(this, StaffPartsActivity.class);
                intent.putExtra("mode", "request");
                startActivity(intent);
            });
        } else {
            findViewById(R.id.fabAdd).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        List<Map<String, Object>> all = dao.getInventoryRequests(session.getBranchId());
        allRequests = new ArrayList<>(all);
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String status = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> r : allRequests) {
            String itemName = ((String) r.get("item_name")).toLowerCase();
            String rStatus = (String) r.get("status");
            
            boolean matchesQuery = itemName.contains(query);
            boolean matchesStatus = status.equals(getString(R.string.all_statuses)) || rStatus.equals(status);

            if (matchesQuery && matchesStatus) {
                filtered.add(r);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
