package edutech.model.repository;

import edutech.model.Course;
import java.util.List;

/**
 * CourseRepository
 *
 * Purpose: Abstracts course storage from business rules (FR-02, FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public interface CourseRepository {
    /**
     * Stores a course under its identifier.
     *
     * @param course course to store
     * @throws IllegalArgumentException if the course is null
     */
    void save(Course course);

    /**
     * Finds a course or returns null when it does not exist.
     *
     * @param courseId identifier to look up
     * @return the matching course, or null
     */
    Course findById(String courseId);

    /**
     * Returns all known courses in a fresh list.
     *
     * @return a list of courses
     */
    List<Course> findAll();
}
