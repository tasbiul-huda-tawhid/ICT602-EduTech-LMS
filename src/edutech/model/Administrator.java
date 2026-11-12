package edutech.model;

/**
 * Administrator
 *
 * Purpose: A staff member who maintains accounts and roles (A2 actor
 * "Administrator", FR-13). Used by Slice A.
 *
 * @author Tasbiul Huda Tawhid
 */
public class Administrator extends User {

    /**
     * Creates an administrator.
     *
     * @param userId   unique identifier
     * @param fullName full name
     * @param email    email address
     * @throws IllegalArgumentException if any value is missing
     */
    public Administrator(String userId, String fullName, String email) {
        super(userId, fullName, email);
    }

    /**
     * {@inheritDoc}
     *
     * @return Role.ADMINISTRATOR
     */
    @Override
    public Role getRole() {
        return Role.ADMINISTRATOR;
    }
}
