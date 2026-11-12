package edutech.model.repository;

import edutech.model.Enrolment;
import java.util.List;

/**
 * EnrolmentRepository
 *
 * Purpose: Provides storage operations needed by learner enrolment (FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public interface EnrolmentRepository {
    /**
     * Saves an enrolment record.
     *
     * @param enrolment the record to store
     * @throws IllegalArgumentException if the record is null
     */
    void save(Enrolment enrolment);

    /**
     * Tests whether a learner already belongs to a course.
     *
     * @param learnerId learner identifier
     * @param courseId course identifier
     * @return true when an enrolment exists
     */
    boolean exists(String learnerId, String courseId);

    /**
     * Counts enrolments occupying a course.
     *
     * @param courseId course identifier
     * @return number of existing enrolments
     */
    int countByCourse(String courseId);

    /**
     * Finds the enrolments for one learner.
     *
     * @param learnerId learner identifier
     * @return a fresh list of matching records
     */
    List<Enrolment> findByLearner(String learnerId);
}
