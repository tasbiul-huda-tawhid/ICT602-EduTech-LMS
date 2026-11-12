package edutech.model;

/**
 * Learner
 *
 * Purpose: A student or corporate trainee who studies courses (A2 actor
 * "Learner"). Used by Slice B for enrolment.
 *
 * @author Tasbiul Huda Tawhid
 */
public class Learner extends User {

    private final LearnerType learnerType;

    /**
     * Creates a learner.
     *
     * @param userId      unique identifier
     * @param fullName    full name
     * @param email       email address
     * @param learnerType STUDENT or CORPORATE_TRAINEE
     * @throws IllegalArgumentException if any value is missing
     */
    public Learner(String userId, String fullName, String email, LearnerType learnerType) {
        super(userId, fullName, email);
        if (learnerType == null) {
            throw new IllegalArgumentException("Learner type must not be empty.");
        }
        this.learnerType = learnerType;
    }

    /**
     * Returns whether this learner is a student or a corporate trainee.
     *
     * @return the learner type
     */
    public LearnerType getLearnerType() {
        return learnerType;
    }

    /**
     * {@inheritDoc}
     *
     * @return Role.LEARNER
     */
    @Override
    public Role getRole() {
        return Role.LEARNER;
    }
}
