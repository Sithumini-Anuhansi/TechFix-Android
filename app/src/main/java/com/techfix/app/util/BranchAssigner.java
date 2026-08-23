package com.techfix.app.util;

import com.techfix.app.data.TechFixDao;
import com.techfix.app.model.Branch;
import com.techfix.app.model.Technician;

import java.util.List;

public class BranchAssigner {
    public static class Result {
        public final Branch branch;
        public final Technician technician;
        public final double distanceKm;
        public final boolean usedGps;

        public Result(Branch branch, Technician technician, double distanceKm, boolean usedGps) {
            this.branch = branch;
            this.technician = technician;
            this.distanceKm = distanceKm;
            this.usedGps = usedGps;
        }
    }

    public static Result assign(TechFixDao dao, long categoryId, Double lat, Double lng, Long manualBranchId) {
        List<Branch> eligible = dao.getEligibleBranches(categoryId);
        if (eligible.isEmpty()) {
            return null;
        }
        Branch chosen = null;
        double km = -1;
        boolean gps = false;
        if (manualBranchId != null) {
            for (Branch b : eligible) {
                if (b.id == manualBranchId) {
                    chosen = b;
                    break;
                }
            }
        }
        if (chosen == null && lat != null && lng != null) {
            chosen = LocationHelper.nearest(lat, lng, eligible);
            if (chosen != null) {
                km = LocationHelper.haversineKm(lat, lng, chosen.latitude, chosen.longitude);
                gps = true;
            }
        }
        if (chosen == null) {
            chosen = eligible.get(0);
        }
        Technician tech = dao.getAvailableTechnician(chosen.id);
        if (tech == null) {
            return null;
        }
        return new Result(chosen, tech, km, gps);
    }
}
