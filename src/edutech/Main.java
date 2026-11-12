package edutech;

import edutech.controller.AdminController;
import edutech.controller.AuthController;
import edutech.controller.CourseController;
import edutech.controller.EnrolmentController;
import edutech.model.Administrator;
import edutech.model.Course;
import edutech.model.Instructor;
import edutech.model.Learner;
import edutech.model.LearnerType;
import edutech.model.User;
import edutech.model.repository.InMemoryCourseRepository;
import edutech.model.repository.InMemoryEnrolmentRepository;
import edutech.model.repository.InMemoryUserRepository;
import edutech.model.service.AuthService;
import edutech.model.service.CourseService;
import edutech.model.service.EnrolmentService;
import edutech.view.AdminView;
import edutech.view.CourseView;
import edutech.view.InputHelper;
import edutech.view.LoginView;
import edutech.view.MainMenuView;
import java.util.Scanner;

/**
 * Main
 *
 * Purpose: Entry point and composition root of the EduTech LMS module.
 * It is the only class that creates the in-memory repositories, and it
 * connects models, views and controllers (MVC). After login it sends each
 * user to the menu for their role (FR-01, NFR-01).
 *
 * Owner: Tasbiul Huda Tawhid (Slice C).
 *
 * @author Tasbiul Huda Tawhid
 */
public class Main {

    /** Demo account details shown at start-up so the module can be marked. */
    private static final String[] DEMO_ACCOUNTS = {
        "Learner:       alex  / Learner2026!",
        "Learner:       maya  / Learner2026!",
        "Instructor:    priya / Teach2026!",
        "Administrator: sam   / Admin2026!"
    };

    /** Places in the demo course, kept small so the "course full" rule can be shown. */
    private static final int DEMO_COURSE_CAPACITY = 1;

    /**
     * Builds the application, seeds demo data and runs the login loop.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // One Scanner and one InputHelper for the whole program
        InputHelper input = new InputHelper(new Scanner(System.in));

        // Model: repositories first, then the services that use them
        AuthService authService = new AuthService(new InMemoryUserRepository());
        CourseService courseService = new CourseService(new InMemoryCourseRepository());
        EnrolmentService enrolmentService =
                new EnrolmentService(new InMemoryEnrolmentRepository(), courseService);

        // Views
        MainMenuView mainMenuView = new MainMenuView();
        CourseView courseView = new CourseView(input);

        // Controllers
        AuthController authController = new AuthController(new LoginView(input), authService);
        AdminController adminController = new AdminController(new AdminView(input), authService);
        CourseController courseController = new CourseController(courseView, courseService);
        EnrolmentController enrolmentController =
                new EnrolmentController(courseView, courseService, enrolmentService);

        mainMenuView.showMessage("Starting EduTech LMS (preparing secure demo accounts)...");
        seedDemoData(authService, courseService);
        mainMenuView.showBanner(DEMO_ACCOUNTS);

        try {
            User currentUser = authController.handleLogin();
            while (currentUser != null) {
                openMenuForRole(currentUser, adminController, courseController, enrolmentController);
                mainMenuView.showLoggedOut(currentUser.getDisplayName());
                currentUser = authController.handleLogin();
            }
        } catch (IllegalStateException e) {
            // Thrown by InputHelper when the input stream ends (for example Ctrl+D)
            mainMenuView.showMessage(e.getMessage());
        }
        mainMenuView.showGoodbye();
    }

    /**
     * Creates the demo accounts and courses used for marking and the video.
     * Course C1 is published with one place; course C2 stays a draft.
     *
     * @param authService   service used to register accounts
     * @param courseService service used to create and publish courses
     */
    private static void seedDemoData(AuthService authService, CourseService courseService) {
        Instructor priya = new Instructor("U002", "Dr Priya Nair", "priya@example.edu",
                "Software Engineering");
        authService.registerUser(new Learner("U001", "Alex Chen", "alex@example.edu",
                LearnerType.STUDENT), "alex", "Learner2026!");
        authService.registerUser(new Learner("U004", "Maya Singh", "maya@example.edu",
                LearnerType.CORPORATE_TRAINEE), "maya", "Learner2026!");
        authService.registerUser(priya, "priya", "Teach2026!");
        authService.registerUser(new Administrator("U003", "Sam Taylor", "sam@example.edu"),
                "sam", "Admin2026!");

        Course javaCourse = courseService.createCourse(priya, "Introduction to Java",
                "Core Java syntax and object-oriented basics.", DEMO_COURSE_CAPACITY);
        courseService.publishCourse(javaCourse.getCourseId(), priya);
        courseService.createCourse(priya, "Software Testing Basics",
                "Black-box and white-box testing techniques.", 30);
    }

    /**
     * Opens the menu that matches the user's role. The cast is safe because
     * each role value belongs to exactly one User subclass.
     *
     * @param user                the logged-in user
     * @param adminController     controller for administrators
     * @param courseController    controller for instructors
     * @param enrolmentController controller for learners
     */
    private static void openMenuForRole(User user, AdminController adminController,
            CourseController courseController, EnrolmentController enrolmentController) {
        switch (user.getRole()) {
            case LEARNER:
                enrolmentController.runLearnerMenu((Learner) user);
                break;
            case INSTRUCTOR:
                courseController.runInstructorMenu((Instructor) user);
                break;
            case ADMINISTRATOR:
                adminController.runAdminMenu((Administrator) user);
                break;
            default:
                // Not reachable while Role has three values; kept for safety
                throw new IllegalStateException("Unknown role: " + user.getRole());
        }
    }
}
