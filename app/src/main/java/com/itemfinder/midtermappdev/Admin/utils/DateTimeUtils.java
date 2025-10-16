package com.itemfinder.midtermappdev.Admin.utils;

import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateTimeUtils {
    private static final String TAG = "DateTimeUtils";

    /**
     * Convert timestamp (milliseconds) to formatted date string
     * Format: "Jan 15, 2025 - 2:30 PM"
     */
    public static String formatTimestamp(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting timestamp: " + e.getMessage());
            return "Invalid Date";
        }
    }

    /**
     * Convert timestamp to short date format
     * Format: "Jan 15, 2025"
     */
    public static String formatDateOnly(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date: " + e.getMessage());
            return "Invalid Date";
        }
    }

    /**
     * Convert timestamp to time only format
     * Format: "2:30 PM"
     */
    public static String formatTimeOnly(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting time: " + e.getMessage());
            return "Invalid Time";
        }
    }

    /**
     * Get time elapsed since the timestamp
     * Returns: "2 hours ago", "3 days ago", etc.
     */
    public static String getTimeAgo(long timestamp) {
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - timestamp;

            if (elapsedTime < 60000) {
                return "Just now";
            } else if (elapsedTime < 3600000) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else if (elapsedTime < 86400000) {
                long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (elapsedTime < 604800000) {
                long days = TimeUnit.MILLISECONDS.toDays(elapsedTime);
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else {
                return formatDateOnly(timestamp);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error calculating time ago: " + e.getMessage());
            return "Invalid";
        }
    }

    /**
     * Get current timestamp in milliseconds
     */
    public static long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * Check if a date is from today
     */
    public static boolean isToday(long timestamp) {
        Date date = new Date(timestamp);
        Date today = new Date();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date).equals(sdf.format(today));
    }

    /**
     * Check if a date is from yesterday
     */
    public static boolean isYesterday(long timestamp) {
        Date date = new Date(timestamp);
        Date yesterday = new Date(System.currentTimeMillis() - 86400000);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date).equals(sdf.format(yesterday));
    }

    /**
     * Get day name from timestamp
     * Returns: "Monday", "Tuesday", etc.
     */
    public static String getDayName(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error getting day name: " + e.getMessage());
            return "Unknown";
        }
    }

    /**
     * Format time difference between two timestamps
     * Returns: "2 days 3 hours"
     */
    public static String getTimeDifference(long startTime, long endTime) {
        try {
            long diffTime = Math.abs(endTime - startTime);
            long days = TimeUnit.MILLISECONDS.toDays(diffTime);
            long hours = TimeUnit.MILLISECONDS.toHours(diffTime) % 24;
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diffTime) % 60;

            StringBuilder sb = new StringBuilder();
            if (days > 0) {
                sb.append(days).append(" day").append(days > 1 ? "s" : "");
            }
            if (hours > 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(hours).append(" hour").append(hours > 1 ? "s" : "");
            }
            if (minutes > 0 && days == 0) {
                if (sb.length() > 0) sb.append(" ");
                sb.append(minutes).append(" minute").append(minutes > 1 ? "s" : "");
            }

            return sb.length() > 0 ? sb.toString() : "0 minutes";
        } catch (Exception e) {
            Log.e(TAG, "Error calculating time difference: " + e.getMessage());
            return "Invalid";
        }
    }
}