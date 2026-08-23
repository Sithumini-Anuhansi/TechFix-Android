package com.techfix.app.ui.admin;

import android.os.Bundle;
import android.view.View;
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

import java.util.List;
import java.util.Map;

public class AdminInventoryRequestsActivity extends AppCompatActivity {
    private TechFixDao dao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        dao = new TechFixDao(this);
        UiHelper.setupToolbar(this, "Stock Requests", true);

        findViewById(R.id.searchLayout).setVisibility(View.GONE);
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        load();
    }

    private void load() {
        List<Map<String, Object>> list = dao.getInventoryRequests();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Map<String, Object>> adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
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
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
