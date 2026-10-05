package com.lms.exception;

/**
 * UserNotFoundException — Custom Exception (OOP: Exception Handling)
 *
 * Extends RuntimeException so it doesn't require forced try-catch (unchecked).
 * Thrown by UserService/UserDAO when a user is not found in the database.
 *
 * Custom exceptions make error handling semantic and domain-specific
 * rather than relying on generic SQLException or NullPointerException.
 */
public class UserNotFoundException extends RuntimeException {

    private final int userId;
    private final String email;

    /**
     * Constructor with user ID.
     */
    public UserNotFoundException(int userId) {
        super("User not found with ID: " + userId);
        this.userId = userId;
        this.email = null;
    }

    /**
     * Constructor with email (used during login).
     */
    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
        this.userId = -1;
        this.email = email;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }
}
