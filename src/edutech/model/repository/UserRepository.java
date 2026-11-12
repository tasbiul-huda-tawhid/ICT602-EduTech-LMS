package edutech.model.repository;

import edutech.model.Credential;
import edutech.model.User;
import java.util.List;
import java.util.Optional;

/**
 * UserRepository
 *
 * Purpose: Storage interface used by the authentication service. It keeps
 * storage choices separate from business rules (dependency inversion).
 * Covers FR-01, FR-13, NFR-01 (Slice A).
 *
 * @author Md Abdul Kaium
 */
public interface UserRepository {
    /**
     * Saves a new user with their associated credentials.
     * @param user new user
     * @param credential login credential for the user
     * @throws IllegalArgumentException if a username or ID exists
     */
    void save(User user, Credential credential);

    /**
     * Finds a credential by normalised username.
     * @param username login username
     * @return credential, if registered
     */
    Optional<Credential> findCredentialByUsername(String username);

    /**
     * Finds a user by their ID.
     * @param userId user identifier
     * @return user, if registered
     */
    Optional<User> findUserById(String userId);

    /**
     * Returns user profiles for an administrator screen.
     * @return snapshot of registered users
     */
    List<User> findAllUsers();
}
