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
import com.techfix.app.model.Technician;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class StaffTechniciansActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Technicians", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        List<Technician> list = new TechFixDao(this).getTechnicians();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Technician> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.specialty);
            meta.setText(item.branchName + (item.available == 1 ? " · Available" : " · Busy"));
        }, item -> {});
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
