package com.techfix.app.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.techfix.app.model.Branch;

import java.util.List;

public class LocationHelper {
    public interface LocationCallback {
        void onLocation(double lat, double lng);

        void onUnavailable();
    }

    public static boolean hasPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCurrent(Context context, LocationCallback callback) {
        if (!hasPermission(context)) {
            callback.onUnavailable();
            return;
        }
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(context);
        CancellationTokenSource token = new CancellationTokenSource();
        try {
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocation(location.getLatitude(), location.getLongitude());
                        } else {
                            client.getLastLocation().addOnSuccessListener(last -> {
                                if (last != null) {
                                    callback.onLocation(last.getLatitude(), last.getLongitude());
                                } else {
                                    callback.onUnavailable();
                                }
                            }).addOnFailureListener(e -> callback.onUnavailable());
                        }
                    })
                    .addOnFailureListener(e -> callback.onUnavailable());
        } catch (SecurityException e) {
            callback.onUnavailable();
        }
    }

    public static double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lng1, lat2, lng2, result);
        return result[0] / 1000.0;
    }

    public static Branch nearest(double lat, double lng, List<Branch> branches) {
        Branch best = null;
        double bestKm = Double.MAX_VALUE;
        for (Branch branch : branches) {
            double km = haversineKm(lat, lng, branch.latitude, branch.longitude);
            if (km < bestKm) {
                bestKm = km;
                best = branch;
            }
        }
        return best;
    }
}
