package com.itemfinder.midtermappdev.Admin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPrefsManager {
    private static final String TAG = "SharedPrefsManager";
    private static final String PREFS_NAME = "com.itemFinder.realfinalappdev.prefs";

    // Preference Keys
    private static final String KEY_LAST_FILTER = "last_filter";
    private static final String KEY_LAST_CATEGORY = "last_category";
    private static final String KEY_SORT_ORDER = "sort_order";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_IS_ADMIN = "is_admin";
    private static final String KEY_LAST_SYNC = "last_sync";

    private static SharedPreferences sharedPreferences;

    /**
     * Initialize SharedPreferences (call this in Application class or MainActivity)
     */
    public static void init(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Log.d(TAG, "SharedPreferences initialized");
        }
    }

    /**
     * Save last filter used
     */
    public static void setLastFilter(String filter) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_LAST_FILTER, filter).apply();
            Log.d(TAG, "Saved last filter: " + filter);
        }
    }

    /**
     * Get last filter used
     */
    public static String getLastFilter() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_LAST_FILTER, "all");
        }
        return "all";
    }

    /**
     * Save last category viewed
     */
    public static void setLastCategory(String category) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_LAST_CATEGORY, category).apply();
            Log.d(TAG, "Saved last category: " + category);
        }
    }

    /**
     * Get last category viewed
     */
    public static String getLastCategory() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_LAST_CATEGORY, "active");
        }
        return "active";
    }

    /**
     * Save sort order preference
     */
    public static void setSortOrder(String sortOrder) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_SORT_ORDER, sortOrder).apply();
            Log.d(TAG, "Saved sort order: " + sortOrder);
        }
    }

    /**
     * Get sort order preference
     */
    public static String getSortOrder() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_SORT_ORDER, "newest");
        }
        return "newest";
    }

    /**
     * Save theme mode (light/dark)
     */
    public static void setThemeMode(String mode) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_THEME_MODE, mode).apply();
            Log.d(TAG, "Saved theme mode: " + mode);
        }
    }

    /**
     * Get theme mode
     */
    public static String getThemeMode() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_THEME_MODE, "light");
        }
        return "light";
    }

    /**
     * Save user ID
     */
    public static void setUserId(String userId) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
            Log.d(TAG, "Saved user ID");
        }
    }

    /**
     * Get user ID
     */
    public static String getUserId() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_USER_ID, "");
        }
        return "";
    }

    /**
     * Save user name
     */
    public static void setUserName(String userName) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putString(KEY_USER_NAME, userName).apply();
            Log.d(TAG, "Saved user name");
        }
    }

    /**
     * Get user name
     */
    public static String getUserName() {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(KEY_USER_NAME, "Admin");
        }
        return "Admin";
    }

    /**
     * Set admin status
     */
    public static void setIsAdmin(boolean isAdmin) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putBoolean(KEY_IS_ADMIN, isAdmin).apply();
            Log.d(TAG, "Saved admin status: " + isAdmin);
        }
    }

    /**
     * Get admin status
     */
    public static boolean getIsAdmin() {
        if (sharedPreferences != null) {
            return sharedPreferences.getBoolean(KEY_IS_ADMIN, false);
        }
        return false;
    }

    /**
     * Save last sync timestamp
     */
    public static void setLastSync(long timestamp) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().putLong(KEY_LAST_SYNC, timestamp).apply();
            Log.d(TAG, "Saved last sync time");
        }
    }

    /**
     * Get last sync timestamp
     */
    public static long getLastSync() {
        if (sharedPreferences != null) {
            return sharedPreferences.getLong(KEY_LAST_SYNC, 0);
        }
        return 0;
    }

    /**
     * Clear all preferences
     */
    public static void clearAll() {
        if (sharedPreferences != null) {
            sharedPreferences.edit().clear().apply();
            Log.d(TAG, "All preferences cleared");
        }
    }

    /**
     * Clear specific preference
     */
    public static void clear(String key) {
        if (sharedPreferences != null) {
            sharedPreferences.edit().remove(key).apply();
            Log.d(TAG, "Preference cleared: " + key);
        }
    }

    /**
     * Check if a key exists
     */
    public static boolean contains(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.contains(key);
        }
        return false;
    }
}