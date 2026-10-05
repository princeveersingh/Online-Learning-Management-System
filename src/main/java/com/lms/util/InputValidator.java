package com.lms.util;

/**
 * InputValidator — static utility methods for validating user inputs.
 *
 * Using static methods in a utility class (no state needed) is a common
 * Java pattern. All GUI forms call these before submitting data to services.
 */
public class InputValidator {

    // Utility class — prevent instantiation
    private InputValidator() {
    }

    /**
     * Checks that a string is non-null and not blank (whitespace-only).
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * Validates email format using a simple regex pattern.
     * Accepts formats like user@domain.com
     */
    public static boolean isValidEmail(String email) {
        if (!isNotBlank(email))
            return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validates that marks are between 0 and maxMarks (inclusive).
     */
    public static boolean isValidMarks(int marks, int maxMarks) {
        return marks >= 0 && marks <= maxMarks;
    }

    /**
     * Validates password length (minimum 6 characters).
     */
    public static boolean isValidPassword(String password) {
        return isNotBlank(password) && password.length() >= 6;
    }

    /**
     * Validates that a number is positive (for IDs, order indexes, etc.).
     */
    public static boolean isPositive(int value) {
        return value > 0;
    }

    /**
     * Returns a trimmed, non-null version of the input.
     * Use before storing to DB to clean up user input.
     */
    public static String sanitize(String input) {
        return input == null ? "" : input.trim();
    }
}
