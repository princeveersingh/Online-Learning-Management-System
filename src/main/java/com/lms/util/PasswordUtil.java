package com.lms.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PasswordUtil — Secure password hashing using SHA-256 with salt.
 *
 * Passwords are NEVER stored in plain text. A random salt is generated
 * for each user and stored alongside the hash. This prevents rainbow
 * table attacks even if the database is compromised.
 *
 * Uses java.security.MessageDigest (standard Java — no external libraries
 * needed).
 */
public class PasswordUtil {

    // Use SecureRandom for cryptographically strong salt generation
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int SALT_BYTES = 32;

    // Utility class — private constructor prevents instantiation
    private PasswordUtil() {
    }

    /**
     * Generates a random Base64-encoded salt string.
     * Each user gets a unique salt, stored in the database.
     */
    public static String generateSalt() {
        byte[] saltBytes = new byte[SALT_BYTES];
        SECURE_RANDOM.nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    /**
     * Hashes a plain-text password combined with the user's salt.
     * Algorithm: SHA-256(salt + password) → Base64
     *
     * @param plainPassword the raw password entered by the user
     * @param salt          the user's unique salt from the database
     * @return Base64-encoded SHA-256 hash
     */
    public static String hashPassword(String plainPassword, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // Combine salt + password before hashing
            String combined = salt + plainPassword;
            byte[] hashBytes = digest.digest(combined.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always available in Java — this should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plain-text password against a stored hash and salt.
     * Re-hashes the input and compares — constant-time safe comparison.
     *
     * @param plainPassword the password entered during login
     * @param storedHash    the hash retrieved from the database
     * @param salt          the salt retrieved from the database
     * @return true if the password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash, String salt) {
        String computed = hashPassword(plainPassword, salt);
        return MessageDigest.isEqual(
                computed.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8));
    }
}
