package edutech.model;

/**
 * Instructor
 *
 * Purpose: A teacher or trainer who creates and publishes courses (A2 actor
 * "Instructor"). Used by Slice B for course management.
 *
 * @author Tasbiul Huda Tawhid
 */
public class Instructor extends User {

    private final String department;

    /**
     * Creates an instructor.
     *
     * @param userId     unique identifier
     * @param fullName   full name
     * @param email      email address
     * @param department teaching department or training unit
     * @throws IllegalArgumentException if any value is missing
     */
    public Instructor(String userId, String fullName, String email, String department) {
        super(userId, fullName, email);
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Department must not be empty.");
        }
        this.department = department.trim();
    }

    /**
     * Returns the instructor's department.
     *
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * {@inheritDoc}
     *
     * @return Role.INSTRUCTOR
     */
    @Override
    public Role getRole() {
        return Role.INSTRUCTOR;
    }
}
