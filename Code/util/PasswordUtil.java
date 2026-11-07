package Projects_java.Banking_System.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for password and input validation operations.
 *
 * Uses SHA-256 (a one-way cryptographic hash function) to securely
 * store passwords and security PINs — plaintext is never stored in the DB.
 */
public class PasswordUtil {

    // ─── Hashing ─────────────────────────────────────────────────────────────

    /**
     * Hashes a plain-text string using SHA-256.
     * @param plain the plain-text string (password or PIN)
     * @return 64-character lowercase hex string (SHA-256 digest)
     */
    public static String hash(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(plain.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed by Java spec; this should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies a plain-text input against a stored SHA-256 hash.
     * @param plain  the user-entered plain-text
     * @param hashed the stored hash from the database
     * @return true if the plain text matches the stored hash
     */
    public static boolean verify(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        return hash(plain).equals(hashed);
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    /**
     * Validates email format using a standard regex pattern.
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * Validates that a security PIN is 4 to 6 numeric digits.
     */
    public static boolean isValidPin(String pin) {
        return pin != null && pin.matches("\\d{4,6}");
    }

    /**
     * Validates that a password meets minimum security requirements (≥ 8 characters).
     */
    public static boolean isStrongPassword(String password) {
        return password != null && password.length() >= 8;
    }
}

