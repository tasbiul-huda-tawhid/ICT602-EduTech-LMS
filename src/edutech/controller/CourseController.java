package edutech.controller;

import edutech.model.Course;
import edutech.model.Instructor;
import edutech.model.service.CourseService;
import edutech.view.CourseView;
import java.util.Objects;

/**
 * CourseController
 *
 * Purpose: Coordinates instructor menu choices through the view and service
 * without storing data or reading the keyboard directly (FR-02).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class CourseController {
    private final CourseView view;
    private final CourseService courseService;

    /**
     * Supplies the view and service dependencies called by Main.
     *
     * @param view instructor course view
     * @param courseService course business service
     * @throws NullPointerException if a dependency is absent
     */
    public CourseController(CourseView view, CourseService courseService) {
        this.view = Objects.requireNonNull(view, "view");
        this.courseService = Objects.requireNonNull(courseService, "courseService");
    }

    /**
     * Runs the instructor commands until 'Log out' is selected.
     *
     * @param instructor the currently logged-in instructor
     * @throws IllegalArgumentException if instructor is null
     */
    public void runInstructorMenu(Instructor instructor) {
        if (instructor == null) {
            throw new IllegalArgumentException("Instructor is required.");
        }
        while (true) {
            view.showInstructorMenu();
            String choice = view.askChoice("Choice: ");
            try {
                switch (choice) {
                    case "1":
                        Course course = courseService.createCourse(instructor,
                                view.ask("Title: "), view.ask("Description: "),
                                view.askNumber("Capacity: "));
                        view.show("Draft created: " + course.getCourseId());
                        break;
                    case "2":
                        courseService.publishCourse(view.ask("Course ID: "), instructor);
                        view.show("Course published successfully.");
                        break;
                    case "3":
                        view.showCourses(courseService.listByInstructor(instructor));
                        break;
                    case "0":
                        return;
                    default:
                        view.show("Invalid menu option. Please choose 0, 1, 2 or 3.");
                }
            } catch (IllegalArgumentException | IllegalStateException exception) {
                view.show("Unable to complete action: " + exception.getMessage());
            }
        }
    }
}
