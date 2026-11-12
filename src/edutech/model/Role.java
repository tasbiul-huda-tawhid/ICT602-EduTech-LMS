package edutech.model;

/**
 * Role
 *
 * Purpose: Lists the roles a user can hold in the EduTech LMS. The role decides
 * which menu a user sees after login (FR-01, NFR-01).
 *
 * @author Tasbiul Huda Tawhid
 */
public enum Role {
    LEARNER("Learner"),
    INSTRUCTOR("Instructor"),
    ADMINISTRATOR("Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the role name in a readable form for menus and messages.
     *
     * @return the display name, for example "Learner"
     */
    public String getDisplayName() {
        return displayName;
    }
}
