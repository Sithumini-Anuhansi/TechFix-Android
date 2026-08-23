package com.techfix.app.ui.staff;

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
import com.techfix.app.model.Payment;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StaffPaymentsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Payment> adapter;
    private List<Payment> allPayments = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, getString(R.string.title_payments), true);
        
        session = new SessionManager(this);
        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {
                getString(R.string.all_payments),
                getString(R.string.status_paid),
                getString(R.string.status_awaiting_payment)
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

        dao = new TechFixDao(this);
        
        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(getString(R.string.label_service_info, item.customerName, item.serviceName));
            String info = (item.branchName != null ? item.branchName + " - " : "");
            if ("COMPLETED".equals(item.status)) {
                info += getString(R.string.label_payment_paid_info, item.method, item.paidAt);
            } else {
                info += getString(R.string.status_awaiting_payment);
            }
            subtitle.setText(info);
            meta.setText(UiHelper.money(this, item.amount));
        }, item -> {});
        
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        
        load();
    }

    private void load() {
        long branchId = 0;
        if (session.isManager()) {
            branchId = session.getBranchId();
        } else {
            branchId = getIntent().getLongExtra("branch_id", 0);
        }
        allPayments = dao.getPayments(branchId);
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        int statusPos = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItemPosition();

        List<Payment> filtered = new ArrayList<>();
        for (Payment p : allPayments) {
            boolean matchesQuery = p.customerName.toLowerCase().contains(query) || p.serviceName.toLowerCase().contains(query);
            boolean matchesStatus = statusPos == 0
                    || (statusPos == 1 && "COMPLETED".equals(p.status))
                    || (statusPos == 2 && "PENDING".equals(p.status));

            if (matchesQuery && matchesStatus) {
                filtered.add(p);
            }
        }
        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
