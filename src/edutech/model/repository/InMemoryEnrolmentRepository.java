package edutech.model.repository;

import edutech.model.Enrolment;
import java.util.ArrayList;
import java.util.List;

/**
 * InMemoryEnrolmentRepository
 *
 * Purpose: Maintains enrolments during one application run (FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class InMemoryEnrolmentRepository implements EnrolmentRepository {
    private final List<Enrolment> enrolments = new ArrayList<>();

    /** Constructs the empty enrolment store required by Main. */
    public InMemoryEnrolmentRepository() {
    }

    /**
     * Stores an enrolment association.
     *
     * @param enrolment non-null enrolment
     * @throws IllegalArgumentException if the record is null
     */
    @Override
    public void save(Enrolment enrolment) {
        if (enrolment == null) {
            throw new IllegalArgumentException("Enrolment is required.");
        }
        enrolments.add(enrolment);
    }

    /**
     * Checks for an existing learner-course combination.
     *
     * @param learnerId learner identifier
     * @param courseId course identifier
     * @return true if the combination is already saved
     */
    @Override
    public boolean exists(String learnerId, String courseId) {
        for (Enrolment enrolment : enrolments) {
            if (enrolment.getLearnerId().equals(learnerId)
                    && enrolment.getCourseId().equals(courseId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Counts existing seats occupied in a course.
     *
     * @param courseId the course identifier
     * @return occupied places
     */
    @Override
    public int countByCourse(String courseId) {
        int count = 0;
        for (Enrolment enrolment : enrolments) {
            if (enrolment.getCourseId().equals(courseId)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns enrolment records associated with a learner.
     *
     * @param learnerId learner identifier
     * @return a new list of enrolments
     */
    @Override
    public List<Enrolment> findByLearner(String learnerId) {
        List<Enrolment> matches = new ArrayList<>();
        for (Enrolment enrolment : enrolments) {
            if (enrolment.getLearnerId().equals(learnerId)) {
                matches.add(enrolment);
            }
        }
        return matches;
    }
}
