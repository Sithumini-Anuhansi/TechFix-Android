package com.techfix.app.ui.staff;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.SparePart;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class StaffPartsActivity extends AppCompatActivity {
    private TechFixDao dao;
    private SimpleAdapter<SparePart> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Spare parts", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);
        dao = new TechFixDao(this);

        adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.branchName + " · " + item.categoryName);
            meta.setText("Qty: " + item.quantity);
        }, this::adjustQty);

        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        load();
    }

    private void load() {
        List<SparePart> list = dao.getSpareParts();
        adapter.submit(list);
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void adjustQty(SparePart part) {
        new AlertDialog.Builder(this)
                .setTitle(part.name)
                .setMessage("Update stock at " + part.branchName)
                .setPositiveButton("+1", (d, w) -> {
                    dao.updatePartQuantity(part.id, part.quantity + 1);
                    load();
                })
                .setNegativeButton("-1", (d, w) -> {
                    dao.updatePartQuantity(part.id, part.quantity - 1);
                    load();
                })
                .setNeutralButton("Close", null)
                .show();
        Toast.makeText(this, "Tap +1 or -1 to change quantity", Toast.LENGTH_SHORT).show();
    }
}
