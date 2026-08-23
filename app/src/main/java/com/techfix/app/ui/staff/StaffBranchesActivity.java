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
import com.techfix.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class StaffBranchesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        UiHelper.setupToolbar(this, "Branches", true);
        findViewById(R.id.searchLayout).setVisibility(View.GONE);

        TechFixDao dao = new TechFixDao(this);
        SessionManager session = new SessionManager(this);
        List<Branch> list = dao.getBranches();

        TextView empty = findViewById(R.id.txtEmpty);
        empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);

        SimpleAdapter<Branch> adapter = new SimpleAdapter<>((item, image, title, subtitle, meta) -> {
            title.setText(item.name);
            subtitle.setText(item.address);
            meta.setText(item.phone + " · " + item.latitude + ", " + item.longitude);
            
            if (item.id == session.getBranchId()) {
                ((View)title.getParent().getParent()).setBackgroundResource(R.drawable.bg_card_highlight);
            } else {
                ((View)title.getParent().getParent()).setBackgroundResource(android.R.color.transparent);
            }
        }, item -> {});
        RecyclerView recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
        adapter.submit(list);
    }
}
