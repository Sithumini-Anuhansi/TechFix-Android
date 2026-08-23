package com.techfix.app.ui;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.techfix.app.R;
import com.techfix.app.data.TechFixDao;
import com.techfix.app.ui.auth.LoginActivity;
import com.techfix.app.util.SessionManager;

public class UiHelper {
    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    public static void setupToolbar(AppCompatActivity activity, String title, boolean showBack) {
        setupToolbar(activity, title, showBack, true);
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    public static void setupToolbar(AppCompatActivity activity, String title, boolean showBack, boolean showMenu) {
        MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);
        if (toolbar == null) {
            return;
        }

        // Set TechFix brand name with logo colors
        android.text.SpannableString brand = new android.text.SpannableString("TechFix");
        brand.setSpan(new android.text.style.ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(activity, R.color.white)), 0, 4, 0);
        brand.setSpan(new android.text.style.ForegroundColorSpan(androidx.core.content.ContextCompat.getColor(activity, R.color.accent)), 4, 7, 0);
        
        toolbar.setTitle(brand);
        
        // If the subtitle passed is "Dashboard", use it as is. 
        // Otherwise, use the provided title.
        toolbar.setSubtitle(title);

        if (showBack) {
            toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
            toolbar.setNavigationOnClickListener(v -> activity.finish());
        }

        if (showMenu) {
            toolbar.inflateMenu(R.menu.menu_main);
            
            SessionManager session = new SessionManager(activity);
            String role = session.getRole();
            
            MenuItem itemDashboard = toolbar.getMenu().findItem(R.id.action_dashboard);
            MenuItem itemHelp = toolbar.getMenu().findItem(R.id.action_help);
            
            if ("CUSTOMER".equals(role)) {
                if (itemDashboard != null) itemDashboard.setVisible(true);
                if (itemHelp != null) itemHelp.setVisible(true);
            } else {
                // ADMIN or STAFF
                if (itemDashboard != null) itemDashboard.setVisible(true);
                if (itemHelp != null) itemHelp.setVisible(false);
            }

            toolbar.setOnMenuItemClickListener(item -> handleMenu(activity, item));

            updateNotificationBadge(activity, toolbar);
        }
    }

    @OptIn(markerClass = ExperimentalBadgeUtils.class)
    public static void updateNotificationBadge(AppCompatActivity activity, MaterialToolbar toolbar) {
        TechFixDao dao = new TechFixDao(activity);
        SessionManager session = new SessionManager(activity);
        if (session.isLoggedIn() && toolbar.getMenu().findItem(R.id.action_notifications) != null) {
            int unreadCount = 0;
            java.util.List<com.techfix.app.model.Notification> list = dao.getNotifications(session.getUserId());
            for (com.techfix.app.model.Notification n : list) {
                if (n.isRead == 0) unreadCount++;
            }

            if (unreadCount > 0) {
                com.google.android.material.badge.BadgeDrawable badge = com.google.android.material.badge.BadgeDrawable.create(activity);
                badge.setNumber(unreadCount);
                badge.setVisible(true);
                badge.setBackgroundColor(androidx.core.content.ContextCompat.getColor(activity, R.color.accent));
                badge.setBadgeTextColor(androidx.core.content.ContextCompat.getColor(activity, R.color.white));
                com.google.android.material.badge.BadgeUtils.attachBadgeDrawable(badge, toolbar, R.id.action_notifications);
            } else {
                // To clear a badge from a Toolbar MenuItem when you don't have the Drawable reference:
                // The most reliable way is to force a menu refresh or remove the badge from the view overlay.
                // For this CW implementation, we'll try to detach using the BadgeUtils with a null drawable 
                // but correctly targeting the toolbar.
                try {
                    com.google.android.material.badge.BadgeUtils.detachBadgeDrawable(null, toolbar, R.id.action_notifications);
                } catch (Exception ignored) {}
            }
        }
    }

    private static boolean handleMenu(AppCompatActivity activity, MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new SessionManager(activity).logout();
            Intent intent = new Intent(activity, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
            return true;
        } else if (id == R.id.action_profile) {
            activity.startActivity(new Intent(activity, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_dashboard) {
            SessionManager session = new SessionManager(activity);
            String role = session.getRole();
            if ("ADMIN".equals(role)) {
                activity.startActivity(new Intent(activity, com.techfix.app.ui.admin.AdminDashboardActivity.class));
            } else if ("STAFF".equals(role)) {
                activity.startActivity(new Intent(activity, com.techfix.app.ui.staff.StaffDashboardActivity.class));
            } else {
                activity.startActivity(new Intent(activity, com.techfix.app.ui.customer.CustomerHomeActivity.class));
            }
            return true;
        } else if (id == R.id.action_notifications) {
            new TechFixDao(activity).markNotificationsRead(new SessionManager(activity).getUserId());
            activity.startActivity(new Intent(activity, com.techfix.app.ui.customer.NotificationsActivity.class));
            return true;
        } else if (id == R.id.action_help) {
            Toast.makeText(activity, R.string.coming_soon, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static String money(android.content.Context context, double value) {
        return context.getString(R.string.currency_format, value);
    }
}
