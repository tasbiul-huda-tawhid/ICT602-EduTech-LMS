package edutech.controller;

import edutech.model.Learner;
import edutech.model.service.CourseService;
import edutech.model.service.EnrolmentService;
import edutech.view.CourseView;
import java.util.Objects;

/**
 * EnrolmentController
 *
 * Purpose: Routes learner menu choices to course and enrolment services
 * using CourseView for all interaction (FR-04).
 * Owner: Rahul Subedi, Slice B.
 *
 * @author Rahul Subedi
 */
public final class EnrolmentController {
    private final CourseView view;
    private final CourseService courseService;
    private final EnrolmentService enrolmentService;

    /**
     * Supplies the dependencies used to handle the learner's menu.
     *
     * @param view learner-facing course view
     * @param courseService course lookup service
     * @param enrolmentService enrolment validation service
     * @throws NullPointerException if a dependency is absent
     */
    public EnrolmentController(CourseView view, CourseService courseService,
                               EnrolmentService enrolmentService) {
        this.view = Objects.requireNonNull(view, "view");
        this.courseService = Objects.requireNonNull(courseService, "courseService");
        this.enrolmentService = Objects.requireNonNull(enrolmentService, "enrolmentService");
    }

    /**
     * Runs the learner commands until 'Log out' is selected.
     *
     * @param learner the currently logged-in learner
     * @throws IllegalArgumentException if learner is null
     */
    public void runLearnerMenu(Learner learner) {
        if (learner == null) {
            throw new IllegalArgumentException("Learner is required.");
        }
        while (true) {
            view.showLearnerMenu();
            String choice = view.askChoice("Choice: ");
            try {
                switch (choice) {
                    case "1":
                        view.showCourses(courseService.listPublished());
                        break;
                    case "2":
                        enrolmentService.enrol(learner, view.ask("Course ID: "));
                        view.show("Enrolled successfully.");
                        break;
                    case "3":
                        view.showCourses(enrolmentService.myCourses(learner));
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
