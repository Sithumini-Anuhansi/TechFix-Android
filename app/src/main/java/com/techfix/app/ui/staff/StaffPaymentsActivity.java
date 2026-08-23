package com.techfix.app.ui.staff;

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

import java.util.ArrayList;
import java.util.List;

public class StaffPaymentsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Payment> adapter;
    private List<Payment> allPayments = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Payments", true);
        
        findViewById(R.id.filterLayout).setVisibility(View.VISIBLE);
        EditText inputSearch = findViewById(R.id.inputSearch);
        Spinner spinnerFilter = findViewById(R.id.spinnerFilter);

        String[] statuses = {"All Payments", "Paid", "Awaiting"};
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
            title.setText(item.customerName + " — " + item.serviceName);
            subtitle.setText(item.paid == 1 ? "Paid " + item.method + " at " + item.paidAt : "Awaiting payment");
            meta.setText(UiHelper.money(item.amount));
        }, item -> {});
        
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        
        load();
    }

    private void load() {
        allPayments = dao.getPayments();
        filter();
    }

    private void filter() {
        String query = ((EditText) findViewById(R.id.inputSearch)).getText().toString().toLowerCase();
        String statusFilter = ((Spinner) findViewById(R.id.spinnerFilter)).getSelectedItem().toString();

        List<Payment> filtered = new ArrayList<>();
        for (Payment p : allPayments) {
            boolean matchesQuery = p.customerName.toLowerCase().contains(query) || p.serviceName.toLowerCase().contains(query);
            boolean matchesStatus = statusFilter.equals("All Payments") 
                    || (statusFilter.equals("Paid") && p.paid == 1)
                    || (statusFilter.equals("Awaiting") && p.paid == 0);

            if (matchesQuery && matchesStatus) {
                filtered.add(filtered.size(), p);
            }
        }
        adapter.submit(filtered);
        findViewById(R.id.txtEmpty).setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
