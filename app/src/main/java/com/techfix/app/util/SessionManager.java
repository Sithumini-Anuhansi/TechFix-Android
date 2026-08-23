package com.techfix.app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.techfix.app.model.User;

public class SessionManager {
    private static final String PREF = "techfix_session";
    private static final String K_ID = "user_id";
    private static final String K_NAME = "user_name";
    private static final String K_ROLE = "user_role";
    private static final String K_EMAIL = "user_email";
    private static final String K_BRANCH_ID = "branch_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public void save(User user) {
        prefs.edit()
                .putLong(K_ID, user.id)
                .putString(K_NAME, user.name)
                .putString(K_ROLE, user.role)
                .putString(K_EMAIL, user.email)
                .putLong(K_BRANCH_ID, user.branchId)
                .putString("branch_name", user.branchName)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong(K_ID, 0) > 0;
    }

    public long getUserId() {
        return prefs.getLong(K_ID, 0);
    }

    public String getName() {
        return prefs.getString(K_NAME, "");
    }

    public String getRole() {
        return prefs.getString(K_ROLE, "");
    }

    public boolean isStaff() {
        return "STAFF".equals(getRole());
    }

    public boolean isManager() {
        return "MANAGER".equals(getRole()) || "BRANCH_MANAGER".equals(getRole());
    }

    public boolean isAdmin() {
        return "ADMIN".equals(getRole());
    }

    public long getBranchId() {
        return prefs.getLong(K_BRANCH_ID, 0);
    }

    public String getBranchName() {
        return prefs.getString("branch_name", "");
    }

    public void logout() {
        prefs.edit().clear().apply();
    }
}
