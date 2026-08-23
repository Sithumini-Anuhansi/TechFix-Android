package com.techfix.app.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.ui.SimpleAdapter;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.SessionManager;

import java.util.List;

public class CustomerHomeActivity extends AppCompatActivity {
    private TechFixDao dao;

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_customer_home);
        UiHelper.setupToolbar(this, getString(R.string.title_dashboard), false);

        dao = new TechFixDao(this);
        SessionManager session = new SessionManager(this);
        TextView welcome = findViewById(R.id.txtWelcome);
        welcome.setText(getString(R.string.hi_user, session.getName(), getString(R.string.role_customer)));

        findViewById(R.id.cardServices).setOnClickListener(v ->
                startActivity(new Intent(this, ServiceListActivity.class)));
        findViewById(R.id.cardAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, MyAppointmentsActivity.class)));
        findViewById(R.id.cardHistory).setOnClickListener(v ->
                startActivity(new Intent(this, RepairHistoryActivity.class)));
        findViewById(R.id.cardMap).setOnClickListener(v ->
                startActivity(new Intent(this, BranchMapActivity.class)));

        setupBranches();
    }

    private void setupBranches() {
        RecyclerView recycler = findViewById(R.id.recyclerBranches);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        
        SimpleAdapter<Branch> adapter = new SimpleAdapter<>((branch, image, title, subtitle, meta) -> {
            title.setText(branch.name);
            subtitle.setText(getString(R.string.label_branch_full_address, branch.address, branch.city));
            meta.setText(branch.phone);
            image.setImageResource(R.drawable.ic_build);
        }, branch -> {
            Intent intent = new Intent(this, BranchMapActivity.class);
            intent.putExtra("branch_id", branch.id);
            startActivity(intent);
        });
        
        recycler.setAdapter(adapter);
        List<Branch> branches = dao.getBranches();
        adapter.submit(branches);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    @Override
    protected void onResume() {
        super.onResume();
        UiHelper.updateNotificationBadge(this, findViewById(R.id.toolbar));
    }
}
