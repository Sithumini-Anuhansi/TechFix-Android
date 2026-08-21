package com.techfix.app.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Offline ContentProvider over SQLite so the MAD deliverable
 * "SQLite, Content Providers & Offline Application" is covered.
 */
public class TechFixProvider extends ContentProvider {
    public static final String AUTHORITY = "com.techfix.app.provider";
    public static final Uri SERVICES_URI = Uri.parse("content://" + AUTHORITY + "/services");
    public static final Uri BRANCHES_URI = Uri.parse("content://" + AUTHORITY + "/branches");

    private static final int SERVICES = 1;
    private static final int BRANCHES = 2;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "services", SERVICES);
        MATCHER.addURI(AUTHORITY, "branches", BRANCHES);
    }

    private DatabaseHelper helper;

    @Override
    public boolean onCreate() {
        helper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();
        int match = MATCHER.match(uri);
        if (match == SERVICES) {
            return db.query(DatabaseHelper.T_SERVICES, projection, selection, selectionArgs, null, null,
                    sortOrder == null ? "name" : sortOrder);
        }
        if (match == BRANCHES) {
            return db.query(DatabaseHelper.T_BRANCHES, projection, selection, selectionArgs, null, null,
                    sortOrder == null ? "city" : sortOrder);
        }
        throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int match = MATCHER.match(uri);
        if (match == SERVICES) {
            return "vnd.android.cursor.dir/vnd.techfix.services";
        }
        if (match == BRANCHES) {
            return "vnd.android.cursor.dir/vnd.techfix.branches";
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }
}
