package edutech.model;

import java.time.LocalDate;

/**
 * Enrolment
 *
 * Purpose: Records one learner's enrolment in one course (FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class Enrolment {
    private final String learnerId;
    private final String courseId;
    private final LocalDate enrolledOn;

    /**
     * Constructs an immutable association between a learner and a course.
     *
     * @param learnerId identifier of the enrolled learner
     * @param courseId identifier of the selected course
     * @throws IllegalArgumentException if either identifier is missing
     */
    public Enrolment(String learnerId, String courseId) {
        if (learnerId == null || learnerId.isBlank()
                || courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Learner ID and course ID are required.");
        }
        this.learnerId = learnerId.trim();
        this.courseId = courseId.trim();
        // Recorded so progress can be tracked from the enrolment date (FR-08)
        this.enrolledOn = LocalDate.now();
    }

    /** @return the learner identifier */
    public String getLearnerId() {
        return learnerId;
    }

    /** @return the course identifier */
    public String getCourseId() {
        return courseId;
    }

    /**
     * Returns the date on which the learner enrolled.
     *
     * @return the enrolment date
     */
    public LocalDate getEnrolledOn() {
        return enrolledOn;
    }
}
