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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Payments", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        List<Payment> list = new TechFixDao(this).getPayments();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Payment> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.customerName + " — " + item.serviceName);
            subtitle.setText(item.paid == 1 ? "Paid " + item.method + " at " + item.paidAt : "Awaiting payment");
            meta.setText(UiHelper.money(item.amount));
        }, item -> {});
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
