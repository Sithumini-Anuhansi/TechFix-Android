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
import com.techfix.app.model.Branch;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;

import java.util.List;

public class StaffBranchesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Branches", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        List<Branch> list = new TechFixDao(this).getBranches();
        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Branch> adapter = new SimpleAdapter<>((item, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.address);
            meta.setText(item.phone + " · " + item.latitude + ", " + item.longitude);
        }, item -> {});
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
