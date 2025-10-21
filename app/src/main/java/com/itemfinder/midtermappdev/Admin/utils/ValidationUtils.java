package com.itemfinder.midtermappdev.Admin.utils;

import android.util.Log;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final String TAG = "ValidationUtils";

    /**
     * Validate if a string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            Log.d(TAG, "Email is empty");
            return false;
        }

        String emailPattern = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }

    /**
     * Validate phone number (basic validation)
     * Accepts formats like: +1-234-567-8900, 123-456-7890, 1234567890
     */
    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            Log.d(TAG, "Phone is empty");
            return false;
        }

        // Remove all non-digit characters
        String digitsOnly = phone.replaceAll("[^0-9+]", "");

        // Check if it has at least 10 digits
        return digitsOnly.length() >= 10;
    }

    /**
     * Validate item name (not empty and minimum length)
     */
    public static boolean isValidItemName(String name) {
        if (!isNotEmpty(name)) {
            Log.d(TAG, "Item name is empty");
            return false;
        }

        return name.length() >= 0 && name.length() <= 100;
    }

    /**
     * Validate description (minimum length)
     */
    public static boolean isValidDescription(String description) {
        if (!isNotEmpty(description)) {
            Log.d(TAG, "Description is empty");
            return false;
        }

        return description.length() >= 5 && description.length() <= 500;
    }

    /**
     * Validate approval/rejection notes (optional but if provided, should have minimum length)
     */
    public static boolean isValidNotes(String notes) {
        // Notes are optional, so empty is allowed
        if (!isNotEmpty(notes)) {
            return true;
        }

        // If provided, must be at least 5 characters
        return notes.length() >= 5 && notes.length() <= 500;
    }

    /**
     * Validate claim details before submission
     */
    public static ValidationResult validateClaim(String claimantName, String claimantEmail,
                                                 String claimantPhone, String description) {
        ValidationResult result = new ValidationResult();

        if (!isNotEmpty(claimantName)) {
            result.isValid = false;
            result.errorMessage = "Claimant name cannot be empty";
            return result;
        }

        if (claimantName.length() < 3) {
            result.isValid = false;
            result.errorMessage = "Claimant name must be at least 3 characters";
            return result;
        }

        if (!isValidEmail(claimantEmail)) {
            result.isValid = false;
            result.errorMessage = "Please enter a valid email address";
            return result;
        }

        if (!isValidPhone(claimantPhone)) {
            result.isValid = false;
            result.errorMessage = "Please enter a valid phone number";
            return result;
        }

        if (!isValidDescription(description)) {
            result.isValid = false;
            result.errorMessage = "Description must be between 10 and 500 characters";
            return result;
        }

        result.isValid = true;
        result.errorMessage = "All validations passed";
        return result;
    }

    /**
     * Validate item details before approval
     */
    public static ValidationResult validateItem(String itemName, String description, String status) {
        ValidationResult result = new ValidationResult();

        if (!isValidItemName(itemName)) {
            result.isValid = false;
            result.errorMessage = "Item name must be between 3 and 100 characters";
            return result;
        }

        if (!isValidDescription(description)) {
            result.isValid = false;
            result.errorMessage = "Item description must be between 10 and 500 characters";
            return result;
        }

        if (!isNotEmpty(status)) {
            result.isValid = false;
            result.errorMessage = "Status cannot be empty";
            return result;
        }

        result.isValid = true;
        result.errorMessage = "Item validation passed";
        return result;
    }

    /**
     * Validate rejection reason
     */
    public static ValidationResult validateRejectionReason(String reason) {
        ValidationResult result = new ValidationResult();

        if (!isNotEmpty(reason)) {
            result.isValid = false;
            result.errorMessage = "Rejection reason cannot be empty";
            return result;
        }

        if (reason.length() < 10) {
            result.isValid = false;
            result.errorMessage = "Rejection reason must be at least 10 characters";
            return result;
        }

        if (reason.length() > 500) {
            result.isValid = false;
            result.errorMessage = "Rejection reason must not exceed 500 characters";
            return result;
        }

        result.isValid = true;
        result.errorMessage = "Rejection reason is valid";
        return result;
    }

    /**
     * Inner class to hold validation results
     */
    public static class ValidationResult {
        public boolean isValid;
        public String errorMessage;

        public ValidationResult() {
            this.isValid = false;
            this.errorMessage = "";
        }

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "isValid=" + isValid +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}