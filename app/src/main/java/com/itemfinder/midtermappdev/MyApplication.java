package com.itemfinder.midtermappdev;

import android.app.Application;
import com.itemfinder.midtermappdev.utils.ThemeManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme when app starts
        int savedTheme = ThemeManager.getSavedTheme(this);
        ThemeManager.applyTheme(savedTheme);
    }
}