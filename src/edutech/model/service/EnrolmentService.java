package edutech.model.service;

import edutech.model.Course;
import edutech.model.CourseStatus;
import edutech.model.Enrolment;
import edutech.model.Learner;
import edutech.model.repository.EnrolmentRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * EnrolmentService
 *
 * Purpose: Enforces publication, capacity and duplicate-enrolment rules
 * for the selected command-line module (FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class EnrolmentService {
    private final EnrolmentRepository enrolmentRepository;
    private final CourseService courseService;

    /**
     * Constructs the service using storage and course rules.
     *
     * @param enrolmentRepository repository interface for enrolment records
     * @param courseService service used to retrieve and validate courses
     * @throws NullPointerException if either dependency is null
     */
    public EnrolmentService(EnrolmentRepository enrolmentRepository,
                            CourseService courseService) {
        this.enrolmentRepository = Objects.requireNonNull(
                enrolmentRepository, "enrolmentRepository");
        this.courseService = Objects.requireNonNull(courseService, "courseService");
    }

    /**
     * Enrols a learner in a published course, if there is a free place.
     *
     * @param learner the learner requesting enrolment
     * @param courseId identifier of the selected course
     * @return the saved learner-course association
     * @throws IllegalArgumentException if learner or course ID is invalid
     * @throws IllegalStateException if unpublished, full or already enrolled
     */
    public Enrolment enrol(Learner learner, String courseId) {
        if (learner == null) {
            throw new IllegalArgumentException("Learner is required.");
        }
        Course course = courseService.getCourse(courseId);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new IllegalStateException("This course is not published.");
        }
        String learnerId = learner.getUserId();
        String savedCourseId = course.getCourseId();
        // A duplicate never consumes a second place, even if the course is full.
        if (enrolmentRepository.exists(learnerId, savedCourseId)) {
            throw new IllegalStateException("You are already enrolled in this course.");
        }
        if (enrolmentRepository.countByCourse(savedCourseId) >= course.getCapacity()) {
            throw new IllegalStateException("This course is full.");
        }
        Enrolment enrolment = new Enrolment(learnerId, savedCourseId);
        enrolmentRepository.save(enrolment);
        return enrolment;
    }

    /**
     * Resolves a learner's saved enrolments into course records.
     *
     * @param learner the learner requesting their course list
     * @return courses for which this learner has an enrolment
     * @throws IllegalArgumentException if learner is null
     */
    public List<Course> myCourses(Learner learner) {
        if (learner == null) {
            throw new IllegalArgumentException("Learner is required.");
        }
        List<Course> courses = new ArrayList<>();
        for (Enrolment enrolment :
                enrolmentRepository.findByLearner(learner.getUserId())) {
            courses.add(courseService.getCourse(enrolment.getCourseId()));
        }
        return courses;
    }
}
