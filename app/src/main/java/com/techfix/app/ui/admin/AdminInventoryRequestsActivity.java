package com.techfix.app.ui.admin;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminInventoryRequestsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Map<String, Object>> adapter;
    private List<Map<String, Object>> allRequests = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dao = new TechFixDao(this);
        UiHelper.setupToolbar(this, "Stock Requests", true);

        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {"All Requests", "PENDING", "APPROVED", "REJECTED"};
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

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.get("quantity") + "x " + item.get("item_name"));
            subtitle.setText(item.get("branch_name") + " · By " + item.get("staff_name"));
            meta.setText((String) item.get("status"));
        }, item -> {
            if ("PENDING".equals(item.get("status"))) {
                new AlertDialog.Builder(this)
                        .setTitle("Fulfill Request?")
                        .setMessage("Approve or Reject this stock request for " + item.get("item_name") + "?")
                        .setPositiveButton("Approve", (dialog, which) -> {
                            dao.updateInventoryRequestStatus((long) item.get("id"), "APPROVED");
                            load();
                        })
                        .setNegativeButton("Reject", (dialog, which) -> {
                            dao.updateInventoryRequestStatus((long) item.get("id"), "REJECTED");
                            load();
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
            }
        });
        recycler.setAdapter(adapter);
        load();
    }

    private void load() {
        allRequests = dao.getInventoryRequests();
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String status = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Map<String, Object>> filtered = new ArrayList<>();
        for (Map<String, Object> r : allRequests) {
            boolean matchesQuery = String.valueOf(r.get("item_name")).toLowerCase().contains(query) ||
                    String.valueOf(r.get("branch_name")).toLowerCase().contains(query);
            boolean matchesStatus = status.equals("All Requests") || String.valueOf(r.get("status")).equals(status);

            if (matchesQuery && matchesStatus) {
                filtered.add(r);
            }
        }

        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
