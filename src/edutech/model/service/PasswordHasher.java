package edutech.model.service;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * PasswordHasher
 *
 * Purpose: Creates and checks salted password hashes using JDK's PBKDF2
 * implementation. MessageDigest.isEqual compares derived hashes.
 * NFR-01, NFR-02; Slice A. No external dependencies.
 *
 * @author Md Abdul Kaium
 */
public final class PasswordHasher {
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 256;
    private static final int ITERATIONS = 600_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Utility class: no instances.
    }

    /**
     * Creates a different random salt for each password.
     * @return 16-byte random salt
     */
    public static byte[] newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Derives a slow password hash from a password and salt.
     * @param password plaintext supplied for this attempt
     * @param salt unique random salt (16 bytes)
     * @return PBKDF2-HMAC-SHA256 derived bytes
     * @throws IllegalArgumentException if inputs are invalid
     * @throws IllegalStateException if the JDK cryptographic implementation is unavailable
     */
    public static byte[] hash(String password, byte[] salt) {
        if (password == null || password.isEmpty() || salt == null || salt.length < SALT_BYTES) {
            throw new IllegalArgumentException("A password and valid salt are required.");
        }
        char[] chars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt, ITERATIONS, HASH_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Password hashing is unavailable.", e);
        } finally {
            spec.clearPassword();
            Arrays.fill(chars, '\0');
        }
    }

    /**
     * Checks a password without ordinary array equality, avoiding an early exit.
     * @param password candidate password
     * @param salt previously stored salt
     * @param storedHash previously derived hash
     * @return true if hashes match
     */
    public static boolean matches(String password, byte[] salt, byte[] storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }
        byte[] candidate = hash(password, salt);
        try {
            return MessageDigest.isEqual(candidate, storedHash);
        } finally {
            Arrays.fill(candidate, (byte) 0);
        }
    }
}
