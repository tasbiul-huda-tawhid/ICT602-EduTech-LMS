package edutech.model;

/**
 * Course
 *
 * Purpose: Stores course identity, ownership, availability and publication
 * status for instructor publishing and learner enrolment (FR-02, FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class Course {
    private final String courseId;
    private final String instructorId;
    private final String title;
    private final String description;
    private final int capacity;
    private CourseStatus status;

    /**
     * Constructs a draft course with a known instructor and capacity.
     *
     * @param courseId unique identifier assigned by the course service
     * @param instructorId identifier of the owning instructor
     * @param title non-blank course title
     * @param description non-blank course description
     * @param capacity number of enrolments the course permits
     * @throws IllegalArgumentException if supplied details are invalid
     */
    public Course(String courseId, String instructorId, String title,
                  String description, int capacity) {
        this.courseId = requireText(courseId, "Course ID");
        this.instructorId = requireText(instructorId, "Instructor ID");
        this.title = requireText(title, "Title");
        this.description = requireText(description, "Description");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive.");
        }
        this.capacity = capacity;
        this.status = CourseStatus.DRAFT;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be empty.");
        }
        return value.trim();
    }

    /** @return the unique course identifier */
    public String getCourseId() {
        return courseId;
    }

    /** @return the instructor's user identifier */
    public String getInstructorId() {
        return instructorId;
    }

    /** @return the title */
    public String getTitle() {
        return title;
    }

    /** @return the course description */
    public String getDescription() {
        return description;
    }

    /** @return the enrolment limit */
    public int getCapacity() {
        return capacity;
    }

    /** @return the current publication state */
    public CourseStatus getStatus() {
        return status;
    }

    /**
     * Transitions the course from draft to published.
     * Ownership verification is carried out by CourseService before this call.
     *
     * @throws IllegalStateException if the course is already published
     */
    public void publish() {
        if (status != CourseStatus.DRAFT) {
            throw new IllegalStateException("Course already published.");
        }
        status = CourseStatus.PUBLISHED;
    }
}
