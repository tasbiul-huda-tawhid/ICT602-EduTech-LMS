package edutech.view;

import edutech.model.Course;
import java.util.List;
import java.util.Objects;

/**
 * CourseView
 *
 * Purpose: Owns all course and enrolment CLI input and output (FR-02, FR-04).
 * The shared InputHelper belongs to Tawhid and is never replaced here.
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class CourseView {
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 1_000_000;
    private final InputHelper input;

    /**
     * Uses the shared input helper rather than opening another Scanner.
     *
     * @param input common InputHelper created by Main
     * @throws NullPointerException if input is missing
     */
    public CourseView(InputHelper input) {
        this.input = Objects.requireNonNull(input, "input");
    }

    /**
     * Displays a line of text.
     *
     * @param message text to display
     */
    public void show(String message) {
        System.out.println(message);
    }

    /**
     * Shows the instructor menu options.
     */
    public void showInstructorMenu() {
        show("\nINSTRUCTOR | 1 Create | 2 Publish | 3 My courses | 0 Log out");
    }

    /**
     * Shows the learner menu options.
     */
    public void showLearnerMenu() {
        show("\nLEARNER | 1 Published courses | 2 Enrol | 3 My courses | 0 Log out");
    }

    /**
     * Reads a menu choice (including blank choices, which the controller rejects).
     *
     * @param prompt label displayed to the user
     * @return trimmed entered text
     */
    public String askChoice(String prompt) {
        return input.readText(prompt);
    }

    /**
     * Reads a required text field such as a course title or ID.
     *
     * @param prompt label displayed to the user
     * @return a non-empty trimmed value
     */
    public String ask(String prompt) {
        return input.readNonEmptyText(prompt);
    }

    /**
     * Reads a capacity within the module's supported numeric range.
     *
     * @param prompt label displayed to the user
     * @return positive course capacity
     */
    public int askNumber(String prompt) {
        return input.readIntInRange(prompt, MIN_CAPACITY, MAX_CAPACITY);
    }

    /**
     * Presents course records, or a message when none match.
     *
     * @param courses results supplied by a service
     */
    public void showCourses(List<Course> courses) {
        if (courses.isEmpty()) {
            show("No courses found.");
            return;
        }
        show("ID | Title | Status | Capacity");
        for (Course course : courses) {
            show(course.getCourseId() + " | " + course.getTitle() + " | "
                    + course.getStatus() + " | " + course.getCapacity());
        }
    }
}
