package edutech.model;

import java.util.Locale;

/**
 * Credential
 *
 * Purpose: Stores login data separately from a person's User profile.
 * A unique salt and a derived password hash are stored; no password is kept.
 * Covers FR-01, FR-13, NFR-01, NFR-02 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public final class Credential {
    public static final int MAX_FAILED_ATTEMPTS = 3;

    private final String username;
    private final String userId;
    private final byte[] salt;
    private final byte[] passwordHash;
    private int failedAttempts;
    private boolean locked;

    /**
     * Constructs stored authentication data.
     *
     * @param username unique login name
     * @param userId owner of this credential
     * @param salt randomly generated salt
     * @param passwordHash derived password hash
     * @throws IllegalArgumentException if required values are missing
     */
    public Credential(String username, String userId, byte[] salt, byte[] passwordHash) {
        if (username == null || username.trim().isEmpty() || userId == null
                || userId.trim().isEmpty() || salt == null || salt.length < 16
                || passwordHash == null || passwordHash.length < 32) {
            throw new IllegalArgumentException("Credential details are incomplete.");
        }
        this.username = username.trim().toLowerCase(Locale.ROOT);
        this.userId = userId.trim();
        this.salt = salt.clone();
        this.passwordHash = passwordHash.clone();
    }

    /** @return normalised username */
    public String getUsername() {
        return username;
    }

    /** @return associated user ID */
    public String getUserId() {
        return userId;
    }

    /** @return defensive copy of the salt */
    public byte[] getSalt() {
        return salt.clone();
    }

    /** @return defensive copy of the derived password hash */
    public byte[] getPasswordHash() {
        return passwordHash.clone();
    }

    /** @return true after the configured failure limit is reached */
    public boolean isLocked() {
        return locked;
    }

    /** @return number of consecutive failed login attempts */
    public int getFailedAttempts() {
        return failedAttempts;
    }

    /** Records a failed login and locks the account at the threshold. */
    public void recordFailedAttempt() {
        if (!locked) {
            failedAttempts++;
            if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
                locked = true;
            }
        }
    }

    /** Clears failures after successful authentication. */
    public void resetFailedAttempts() {
        if (!locked) {
            failedAttempts = 0;
        }
    }
}
