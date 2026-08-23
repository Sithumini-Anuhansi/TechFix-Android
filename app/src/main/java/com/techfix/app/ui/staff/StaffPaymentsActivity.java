package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.view.View;
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

import java.util.List;

public class StaffPaymentsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<Payment> adapter;
    private List<Payment> allPayments;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Payments", true);
        
        android.view.ViewGroup searchLayout = findViewById(R.id.searchLayout);
        searchLayout.setVisibility(View.VISIBLE);
        android.widget.EditText searchInput = findViewById(R.id.inputSearch);
        searchInput.setHint("Filter by status (Paid/Awaiting)...");
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) { filter(s.toString()); }
            public void afterTextChanged(android.text.Editable s) {}
        });

        dao = new TechFixDao(this);
        allPayments = dao.getPayments();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(allPayments.isEmpty() ? View.VISIBLE : View.GONE);

        adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.customerName + " — " + item.serviceName);
            subtitle.setText(item.paid == 1 ? "Paid " + item.method + " at " + item.paidAt : "Awaiting payment");
            meta.setText(UiHelper.money(item.amount));
        }, item -> {});
        
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(allPayments);
    }

    private void filter(String query) {
        java.util.ArrayList<Payment> filtered = new java.util.ArrayList<>();
        for (Payment p : allPayments) {
            String status = p.paid == 1 ? "paid" : "awaiting";
            if (status.contains(query.toLowerCase()) || p.customerName.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(p);
            }
        }
        adapter.submit(filtered);
    }
}
