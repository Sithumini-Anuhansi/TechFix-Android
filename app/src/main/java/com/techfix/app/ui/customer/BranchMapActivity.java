package com.techfix.app.ui.customer;

import android.Manifest;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.ui.UiHelper;
import com.techfix.app.util.LocationHelper;

import java.util.List;

public class BranchMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap map;
    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> enableMyLocation());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_branch_map);
        UiHelper.setupToolbar(this, "Branches", true);
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (fragment != null) {
            fragment.getMapAsync(this);
        }
        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        List<Branch> branches = new TechFixDao(this).getBranches();
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        for (Branch branch : branches) {
            LatLng pos = new LatLng(branch.latitude, branch.longitude);
            map.addMarker(new MarkerOptions().position(pos).title(branch.name).snippet(branch.address));
            bounds.include(pos);
        }
        if (!branches.isEmpty()) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(branches.get(0).latitude, branches.get(0).longitude), 7.2f));
        }
        enableMyLocation();
        if (LocationHelper.hasPermission(this)) {
            LocationHelper.requestCurrent(this, new LocationHelper.LocationCallback() {
                @Override
                public void onLocation(double lat, double lng) {
                    LatLng me = new LatLng(lat, lng);
                    map.addMarker(new MarkerOptions().position(me).title("You"));
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(me, 8.5f));
                }

                @Override
                public void onUnavailable() {
                }
            });
        }
    }

    private void enableMyLocation() {
        if (map == null || !LocationHelper.hasPermission(this)) {
            return;
        }
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) {
        }
    }
}
