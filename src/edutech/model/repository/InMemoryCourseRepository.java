package edutech.model.repository;

import edutech.model.Course;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InMemoryCourseRepository
 *
 * Purpose: Stores course objects in memory without a database (FR-02, FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class InMemoryCourseRepository implements CourseRepository {
    private final Map<String, Course> courses = new LinkedHashMap<>();

    /** Constructs the empty course store required by Main. */
    public InMemoryCourseRepository() {
    }

    /**
     * Saves or updates an existing course by course ID.
     *
     * @param course course to save
     * @throws IllegalArgumentException if the course is null
     */
    @Override
    public void save(Course course) {
        if (course == null) {
            throw new IllegalArgumentException("Course is required.");
        }
        courses.put(course.getCourseId(), course);
    }

    /**
     * Finds one course.
     *
     * @param courseId the identifier to locate
     * @return a matching course, or null if missing
     */
    @Override
    public Course findById(String courseId) {
        return courses.get(courseId);
    }

    /**
     * Supplies courses in insertion order.
     *
     * @return a defensive list copy
     */
    @Override
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }
}
