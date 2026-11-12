package edutech.model.service;

import edutech.model.Credential;
import edutech.model.User;
import edutech.model.repository.UserRepository;
import java.util.List;

/**
 * AuthService
 *
 * Purpose: Registers accounts and validates login credentials. The service
 * depends on the UserRepository interface rather than storage details.
 * Covers FR-01, FR-13, NFR-01, NFR-02 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public final class AuthService {
    private static final int MIN_PASSWORD_LENGTH = 8;
    private final UserRepository userRepository;

    /**
     * Creates the authentication service with repository abstraction.
     * @param userRepository account storage contract
     */
    public AuthService(UserRepository userRepository) {
        if (userRepository == null) {
            throw new IllegalArgumentException("User repository is required.");
        }
        this.userRepository = userRepository;
    }

    /**
     * Registers an existing domain user with a username and a password.
     * Used for demo accounts during start-up and administrator-created accounts.
     * @param user the Learner, Instructor or Administrator profile
     * @param username new unique login username
     * @param plainPassword password to derive a hash from (never stored)
     * @throws IllegalArgumentException if the input is invalid or duplicate
     */
    public void registerUser(User user, String username, String plainPassword) {
        if (user == null) {
            throw new IllegalArgumentException("User is required.");
        }
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must have at least 3 characters.");
        }
        if (plainPassword == null || plainPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("Password must have at least 8 characters.");
        }
        if (userRepository.findCredentialByUsername(username).isPresent()) {
            throw new IllegalArgumentException("That username is already registered.");
        }
        if (userRepository.findUserById(user.getUserId()).isPresent()) {
            throw new IllegalArgumentException("That user ID is already registered.");
        }
        byte[] salt = PasswordHasher.newSalt();
        byte[] hash = PasswordHasher.hash(plainPassword, salt);
        Credential credential = new Credential(username, user.getUserId(), salt, hash);
        userRepository.save(user, credential);
    }

    /**
     * Validates a username and password, including account lockout.
     * @param username login username
     * @param plainPassword candidate password
     * @return user profile if authentication succeeds
     * @throws IllegalArgumentException for missing input or wrong credentials
     * @throws IllegalStateException if the account is locked
     */
    public User login(String username, String plainPassword) {
        if (username == null || username.trim().isEmpty()
                || plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Username and password are required.");
        }
        Credential credential = userRepository.findCredentialByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Incorrect username or password."));
        if (credential.isLocked()) {
            throw new IllegalStateException(lockedMessage());
        }
        if (!PasswordHasher.matches(plainPassword, credential.getSalt(),
                credential.getPasswordHash())) {
            credential.recordFailedAttempt();
            if (credential.isLocked()) {
                throw new IllegalStateException(lockedMessage());
            }
            throw new IllegalArgumentException("Incorrect username or password.");
        }
        credential.resetFailedAttempts();
        return userRepository.findUserById(credential.getUserId())
                .orElseThrow(() -> new IllegalStateException("Registered user record is missing."));
    }

    /**
     * Provides account list for an authenticated administrator controller.
     * @return snapshot of registered users
     */
    public List<User> getRegisteredUsers() {
        return userRepository.findAllUsers();
    }

    /**
     * Builds the lockout message from the configured failure limit.
     * @return message shown when an account is locked
     */
    private static String lockedMessage() {
        return "Account locked after " + Credential.MAX_FAILED_ATTEMPTS
                + " incorrect passwords.";
    }
}
