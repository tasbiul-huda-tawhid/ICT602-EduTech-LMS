package edutech.model;

/**
 * User
 *
 * Purpose: Abstract base class for every person who uses the EduTech LMS.
 * Learner, Instructor and Administrator extend it. Login details are NOT kept
 * here; they live in Credential (Slice A), so this class has one job only:
 * describing who the person is (Single Responsibility Principle).
 *
 * @author Tasbiul Huda Tawhid
 */
public abstract class User {

    private final String userId;
    private final String fullName;
    private final String email;

    /**
     * Creates a user after checking that no detail is blank.
     *
     * @param userId   unique identifier, for example "U001"
     * @param fullName the person's full name
     * @param email    the person's email address
     * @throws IllegalArgumentException if any value is null or blank
     */
    protected User(String userId, String fullName, String email) {
        this.userId = requireText(userId, "User ID");
        this.fullName = requireText(fullName, "Full name");
        this.email = requireText(email, "Email");
    }

    /**
     * Returns the role of this user. Each subclass gives its own answer,
     * which lets the controllers route a user without checking class types.
     *
     * @return the user's role
     */
    public abstract Role getRole();

    /**
     * Returns the unique identifier of this user.
     *
     * @return the user ID
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Returns the full name of this user.
     *
     * @return the full name
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Returns the email address of this user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns a short label for menus, for example "Alex Chen (Learner)".
     *
     * @return the display label
     */
    public String getDisplayName() {
        return fullName + " (" + getRole().getDisplayName() + ")";
    }

    /**
     * Checks that a text value is present and returns it trimmed.
     *
     * @param value     the value to check
     * @param fieldName the field name used in the error message
     * @return the trimmed value
     * @throws IllegalArgumentException if the value is null or blank
     */
    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " must not be empty.");
        }
        return value.trim();
    }
}
