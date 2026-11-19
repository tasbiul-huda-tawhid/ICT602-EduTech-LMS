package edutech.devtest;

import edutech.controller.CourseController;
import edutech.model.Course;
import edutech.model.CourseStatus;
import edutech.model.Enrolment;
import edutech.model.Instructor;
import edutech.model.Learner;
import edutech.model.LearnerType;
import edutech.model.repository.InMemoryCourseRepository;
import edutech.model.repository.InMemoryEnrolmentRepository;
import edutech.model.service.CourseService;
import edutech.model.service.EnrolmentService;
import edutech.view.CourseView;
import edutech.view.InputHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * Independent Part B check runner for Rahul's Slice B.
 * Test-only code: no production or shared source files are changed.
 * For the submitted Part A slice and starter kit, not the final group build.
 */
public final class PartBRahulCaseRunner {
    private static final Instructor OWNER = new Instructor("I01", "Case Owner", "owner@example.edu", "IT");
    private static final Instructor OTHER = new Instructor("I02", "Other Instructor", "other@example.edu", "IT");
    private static final Learner LEARNER_1 = new Learner("L01", "Learner One", "one@example.edu", LearnerType.STUDENT);
    private static final Learner LEARNER_2 = new Learner("L02", "Learner Two", "two@example.edu", LearnerType.STUDENT);
    private static int assertions;
    private static final class Fixture {
        final InMemoryCourseRepository courseRepo = new InMemoryCourseRepository();
        final InMemoryEnrolmentRepository enrolRepo = new InMemoryEnrolmentRepository();
        final CourseService course = new CourseService(courseRepo);
        final EnrolmentService enrol = new EnrolmentService(enrolRepo, course);
        Course newCourse(int capacity) {
            return course.createCourse(OWNER, "Java basics", "Introductory Java", capacity);
        }
    }
    private static void check(String statement, boolean result) {
        assertions++;
        System.out.println((result ? "PASS" : "FAIL") + " ASSERTION | " + statement);
        if (!result) { throw new AssertionError(statement); }
    }
    private static void expect(String statement, Class<? extends Throwable> type, String message, Runnable runnable) {
        assertions++;
        try {
            runnable.run();
        } catch (Throwable error) {
            boolean matching = type.isInstance(error) && message.equals(error.getMessage());
            System.out.println((matching ? "PASS" : "FAIL") + " EXCEPTION | " + statement +
                    " | actual=" + error.getClass().getSimpleName() + ": " + error.getMessage());
            if (!matching) { throw new AssertionError("Expected " + type.getSimpleName() + ": " + message, error); }
            return;
        }
        System.out.println("FAIL EXCEPTION | " + statement + " | no exception raised");
        throw new AssertionError("Expected " + type.getSimpleName() + ": " + message);
    }
    private static void caseCourse01() {
        Fixture f = new Fixture();
        Course c = f.course.createCourse(OWNER, "  Java basics  ", "  Introductory Java ", 2);
        check("new course ID is C1", c.getCourseId().equals("C1"));
        check("course saved in repository", f.courseRepo.findById("C1") == c);
        check("new course status is DRAFT", c.getStatus() == CourseStatus.DRAFT);
        check("owner and entered fields retained/trimmed", c.getInstructorId().equals("I01")
                && c.getTitle().equals("Java basics") && c.getDescription().equals("Introductory Java"));
        check("capacity=2", c.getCapacity() == 2);
    }
    private static void caseCourse02() {
        Fixture f = new Fixture();
        expect("empty title", IllegalArgumentException.class, "Course title is required.",
                () -> f.course.createCourse(OWNER, "", "Description", 2));
        expect("blank title", IllegalArgumentException.class, "Course title is required.",
                () -> f.course.createCourse(OWNER, "  ", "Description", 2));
        expect("empty description", IllegalArgumentException.class, "Course description is required.",
                () -> f.course.createCourse(OWNER, "Title", "", 2));
        expect("blank description", IllegalArgumentException.class, "Course description is required.",
                () -> f.course.createCourse(OWNER, "Title", "  ", 2));
        check("invalid entries create no courses", f.courseRepo.findAll().isEmpty());
    }
    private static void caseCourse03() {
        Fixture f = new Fixture();
        expect("capacity zero", IllegalArgumentException.class, "Capacity must be positive.",
                () -> f.course.createCourse(OWNER, "Title", "Description", 0));
        Course c = f.course.createCourse(OWNER, "Title", "Description", 1);
        check("capacity one accepted at service boundary", c.getCapacity() == 1);
        String cliInput = "1\nCLI capacity check\nDescription\nabc\n0\n1\n0\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        java.io.InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        try {
            System.setIn(new ByteArrayInputStream(cliInput.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            InputHelper input = new InputHelper(new Scanner(System.in));
            CourseController controller = new CourseController(
                    new CourseView(input), new CourseService(new InMemoryCourseRepository()));
            controller.runInstructorMenu(OWNER);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        String console = buffer.toString(StandardCharsets.UTF_8);
        System.out.println("CLI transcript (provided input: 1, title, description, abc, 0, 1, 0):");
        System.out.print(console);
        int warnings = console.split("Please enter a number from 1 to 1000000\\.", -1).length - 1;
        check("CLI rejects non-numeric and zero with two re-prompts", warnings == 2);
        check("CLI accepts 1 then creates draft and returns to menu", console.contains("Draft created: C1")
                && console.contains("INSTRUCTOR | 1 Create"));
    }
    private static void caseCourse04() {
        Fixture f = new Fixture();
        Course c = f.newCourse(2);
        f.course.publishCourse(c.getCourseId(), OWNER);
        check("owner published draft", c.getStatus() == CourseStatus.PUBLISHED);
        check("published list contains course", f.course.listPublished().contains(c));
    }
    private static void caseCourse05() {
        Fixture f = new Fixture();
        Course c = f.newCourse(2);
        expect("non-owner publishing", IllegalStateException.class, "Only the course owner can publish this course.",
                () -> f.course.publishCourse(c.getCourseId(), OTHER));
        check("non-owner failure retains DRAFT state", c.getStatus() == CourseStatus.DRAFT);
        f.course.publishCourse(c.getCourseId(), OWNER);
        expect("second publish", IllegalStateException.class, "Course already published.",
                () -> f.course.publishCourse(c.getCourseId(), OWNER));
        check("course remains PUBLISHED", c.getStatus() == CourseStatus.PUBLISHED);
    }
    private static void caseCourse06() {
        Fixture f = new Fixture();
        Course draft = f.newCourse(2);
        Course published = f.course.createCourse(OWNER, "Advanced Java", "Follow-up Java", 3);
        f.course.publishCourse(published.getCourseId(), OWNER);
        List<Course> publishedCourses = f.course.listPublished();
        check("one entry in published-only list", publishedCourses.size() == 1);
        check("published course present", publishedCourses.contains(published));
        check("draft course excluded", !publishedCourses.contains(draft));
        check("instructor's own list still includes both", f.course.listByInstructor(OWNER).size() == 2);
    }
    private static void caseEnrol01() {
        Fixture f = new Fixture();
        Course c = f.newCourse(2);
        f.course.publishCourse(c.getCourseId(), OWNER);
        Enrolment e = f.enrol.enrol(LEARNER_1, c.getCourseId());
        check("enrolment references correct learner and course", e.getLearnerId().equals("L01")
                && e.getCourseId().equals(c.getCourseId()));
        check("record saved in repository", f.enrolRepo.exists("L01", c.getCourseId()));
        check("occupied seats increase to one", f.enrolRepo.countByCourse(c.getCourseId()) == 1);
    }
    private static void caseEnrol02() {
        Fixture f = new Fixture();
        Course draft = f.newCourse(2);
        expect("draft course", IllegalStateException.class, "This course is not published.",
                () -> f.enrol.enrol(LEARNER_1, draft.getCourseId()));
        expect("unknown course ID", IllegalArgumentException.class, "Unknown course ID: MISSING",
                () -> f.enrol.enrol(LEARNER_1, "MISSING"));
        check("neither rejection creates enrolment", f.enrolRepo.countByCourse(draft.getCourseId()) == 0);
    }
    private static void caseEnrol03() {
        Fixture f = new Fixture();
        Course c = f.newCourse(1);
        f.course.publishCourse(c.getCourseId(), OWNER);
        f.enrol.enrol(LEARNER_1, c.getCourseId());
        expect("existing learner tries again when capacity already full", IllegalStateException.class,
                "You are already enrolled in this course.",
                () -> f.enrol.enrol(LEARNER_1, c.getCourseId()));
        check("duplicate does not consume another seat", f.enrolRepo.countByCourse(c.getCourseId()) == 1);
    }
    private static void caseEnrol04() {
        Fixture f = new Fixture();
        Course c = f.newCourse(1);
        f.course.publishCourse(c.getCourseId(), OWNER);
        f.enrol.enrol(LEARNER_1, c.getCourseId());
        expect("second learner attempts full capacity", IllegalStateException.class, "This course is full.",
                () -> f.enrol.enrol(LEARNER_2, c.getCourseId()));
        List<Course> mine = f.enrol.myCourses(LEARNER_1);
        check("My courses returns enrolled course", mine.size() == 1 && mine.contains(c));
        check("My courses for rejected learner is empty", f.enrol.myCourses(LEARNER_2).isEmpty());
        check("course has one occupied place after rejection", f.enrolRepo.countByCourse(c.getCourseId()) == 1);
    }
    public static void main(String[] args) {
        if (args.length != 1) { throw new IllegalArgumentException("Supply one case ID."); }
        String id = args[0];
        System.out.println("CASE_ID: " + id);
        System.out.println("SUT: source tree supplied on the command line (see RUN_HEADER.txt for path, commit and JDK)");
        System.out.println("LEVEL: service-level unless CLI transcript explicitly included");
        switch (id) {
            case "COURSE-01": caseCourse01(); break;
            case "COURSE-02": caseCourse02(); break;
            case "COURSE-03": caseCourse03(); break;
            case "COURSE-04": caseCourse04(); break;
            case "COURSE-05": caseCourse05(); break;
            case "COURSE-06": caseCourse06(); break;
            case "ENROL-01": caseEnrol01(); break;
            case "ENROL-02": caseEnrol02(); break;
            case "ENROL-03": caseEnrol03(); break;
            case "ENROL-04": caseEnrol04(); break;
            default: throw new IllegalArgumentException("Unknown test case: " + id);
        }
        System.out.println("RESULT: PASS | assertions=" + assertions);
    }
}
