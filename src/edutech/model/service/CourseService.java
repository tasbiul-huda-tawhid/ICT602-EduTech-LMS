package edutech.model.service;

import edutech.model.Course;
import edutech.model.CourseStatus;
import edutech.model.Instructor;
import edutech.model.repository.CourseRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CourseService
 *
 * Purpose: Validates course creation, publishing, ownership and discovery
 * independently from the CLI and data store (FR-02, FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class CourseService {
    private final CourseRepository courseRepository;
    private int nextSequenceNumber;

    /**
     * Constructs the service around an abstract repository.
     *
     * @param courseRepository the course storage interface
     * @throws NullPointerException if the repository is null
     */
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = Objects.requireNonNull(courseRepository, "courseRepository");
        this.nextSequenceNumber = 1;
        for (Course course : courseRepository.findAll()) {
            String id = course.getCourseId();
            if (id.matches("C[0-9]+")) {
                try {
                    nextSequenceNumber = Math.max(nextSequenceNumber,
                            Integer.parseInt(id.substring(1)) + 1);
                } catch (NumberFormatException ignored) {
                    // Unusually large pre-existing IDs do not prevent startup.
                }
            }
        }
    }

    /**
     * Creates an unpublished course owned by the supplied instructor.
     *
     * @param instructor owner creating the course
     * @param title non-empty course title
     * @param description non-empty course summary
     * @param capacity positive maximum number of learners
     * @return the saved draft course
     * @throws IllegalArgumentException if details are invalid
     */
    public Course createCourse(Instructor instructor, String title,
                               String description, int capacity) {
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor is required.");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Course title is required.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Course description is required.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        String courseId = "C" + nextSequenceNumber++;
        Course course = new Course(courseId, instructor.getUserId(),
                title, description, capacity);
        courseRepository.save(course);
        return course;
    }

    /**
     * Publishes a draft, provided the caller owns the course.
     *
     * @param courseId identifier of the draft course
     * @param instructor caller who must be its owner
     * @throws IllegalArgumentException if the course ID does not exist
     * @throws IllegalStateException if caller is not owner or state is invalid
     */
    public void publishCourse(String courseId, Instructor instructor) {
        Course course = getCourse(courseId);
        if (instructor == null
                || !course.getInstructorId().equals(instructor.getUserId())) {
            throw new IllegalStateException("Only the course owner can publish this course.");
        }
        course.publish();
        courseRepository.save(course);
    }

    /**
     * Retrieves a course or explains why lookup failed.
     *
     * @param courseId identifier to find
     * @return the matching course
     * @throws IllegalArgumentException if the ID is blank or unknown
     */
    public Course getCourse(String courseId) {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID is required.");
        }
        Course course = courseRepository.findById(courseId.trim());
        if (course == null) {
            throw new IllegalArgumentException("Unknown course ID: " + courseId.trim());
        }
        return course;
    }

    /**
     * Lists only available published courses.
     *
     * @return published courses, in creation order
     */
    public List<Course> listPublished() {
        List<Course> published = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (course.getStatus() == CourseStatus.PUBLISHED) {
                published.add(course);
            }
        }
        return published;
    }

    /**
     * Lists all courses belonging to one instructor, including drafts.
     *
     * @param instructor the owning instructor
     * @return the matching courses
     * @throws IllegalArgumentException if the instructor is null
     */
    public List<Course> listByInstructor(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor is required.");
        }
        List<Course> mine = new ArrayList<>();
        for (Course course : courseRepository.findAll()) {
            if (course.getInstructorId().equals(instructor.getUserId())) {
                mine.add(course);
            }
        }
        return mine;
    }
}
