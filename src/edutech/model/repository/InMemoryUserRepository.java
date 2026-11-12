package edutech.model.repository;

import edutech.model.Credential;
import edutech.model.User;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryUserRepository
 *
 * Purpose: Implements UserRepository with in-memory maps, suitable for a CLI
 * demonstration. Accounts are lost when the process exits (NFR-02 limitation).
 * Covers FR-01, FR-13 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public final class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> usersById;
    private final Map<String, Credential> credentialsByUsername;

    /** Creates an empty in-memory repository. */
    public InMemoryUserRepository() {
        this.usersById = new LinkedHashMap<>();
        this.credentialsByUsername = new LinkedHashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public void save(User user, Credential credential) {
        if (user == null || credential == null) {
            throw new IllegalArgumentException("User and credential are required.");
        }
        String username = credential.getUsername();
        if (!user.getUserId().equals(credential.getUserId())) {
            throw new IllegalArgumentException("User and credential IDs do not match.");
        }
        if (usersById.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("That user ID is already registered.");
        }
        if (credentialsByUsername.containsKey(username)) {
            throw new IllegalArgumentException("That username is already registered.");
        }
        usersById.put(user.getUserId(), user);
        credentialsByUsername.put(username, credential);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Credential> findCredentialByUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(credentialsByUsername.get(username.trim().toLowerCase(Locale.ROOT)));
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findUserById(String userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    /** {@inheritDoc} */
    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(usersById.values());
    }
}
