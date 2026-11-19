import edutech.controller.CourseController;
import edutech.model.Course;
import edutech.model.CourseStatus;
import edutech.model.Credential;
import edutech.model.Enrolment;
import edutech.model.Instructor;
import edutech.model.Learner;
import edutech.model.LearnerType;
import edutech.model.User;
import edutech.model.repository.InMemoryCourseRepository;
import edutech.model.repository.InMemoryEnrolmentRepository;
import edutech.model.repository.InMemoryUserRepository;
import edutech.model.repository.UserRepository;
import edutech.model.service.AuthService;
import edutech.model.service.CourseService;
import edutech.model.service.EnrolmentService;
import edutech.model.service.PasswordHasher;
import edutech.view.CourseView;
import edutech.view.InputHelper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * ICT602 A3 Part B: service-level and white-box checks written by Tawhid.
 * Cases: COURSE-07, ENROL-05, WB-01, WB-02, PERF-01.
 * Test code only. It uses the public API of the integrated build and changes no source file.
 * Usage: java -cp out PartBTawhidChecks CASE-ID
 */
public final class PartBTawhidChecks {
    private static final Instructor OWNER =
            new Instructor("I01", "Case Owner", "owner@example.edu", "IT");
    private static final Learner L01 =
            new Learner("L01", "Learner One", "one@example.edu", LearnerType.STUDENT);
    private static final Learner L02 =
            new Learner("L02", "Learner Two", "two@example.edu", LearnerType.STUDENT);
    private static int checks;

    private PartBTawhidChecks() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java PartBTawhidChecks COURSE-07|ENROL-05|WB-01|WB-02|PERF-01");
            System.exit(2);
        }
        System.out.println("CASE_ID: " + args[0]);
        System.out.println("SUT: integrated EduTech LMS build (see RUN_HEADER.txt for commit and JDK)");
        switch (args[0]) {
            case "COURSE-07": course07(); break;
            case "ENROL-05": enrol05(); break;
            case "WB-01": wb01(); break;
            case "WB-02": wb02(); break;
            case "PERF-01": perf01(); break;
            default:
                System.out.println("Unknown case " + args[0]);
                System.exit(2);
        }
        System.out.println("RESULT: PASS | checks=" + checks);
    }

    // ---------- small helpers, same style as the members' runners ----------

    private static void check(String statement, boolean result) {
        checks++;
        System.out.println((result ? "PASS" : "FAIL") + " CHECK | " + statement);
        if (!result) {
            throw new AssertionError(statement);
        }
    }

    private static void expect(String statement, Class<? extends Throwable> type, String message,
                               Runnable action) {
        checks++;
        try {
            action.run();
        } catch (Throwable error) {
            boolean ok = type.isInstance(error) && message.equals(error.getMessage());
            System.out.println((ok ? "PASS" : "FAIL") + " CHECK | " + statement + " | actual="
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            if (!ok) {
                throw new AssertionError(statement, error);
            }
            return;
        }
        System.out.println("FAIL CHECK | " + statement + " | no exception raised");
        throw new AssertionError(statement);
    }

    /** Fresh repositories and services for every rule, so results never depend on order. */
    private static final class Fixture {
        final InMemoryEnrolmentRepository enrolRepo = new InMemoryEnrolmentRepository();
        final CourseService courses = new CourseService(new InMemoryCourseRepository());
        final EnrolmentService enrolments = new EnrolmentService(enrolRepo, courses);

        Course published(int capacity) {
            Course c = courses.createCourse(OWNER, "Java basics", "Introductory Java", capacity);
            courses.publishCourse(c.getCourseId(), OWNER);
            return c;
        }
    }

    // ---------- COURSE-07: capacity upper boundary (BVA) ----------

    private static void course07() {
        System.out.println("TECHNIQUE: boundary value analysis on the upper capacity limit, CLI and service");
        String cli = "1\nMax course\nUpper boundary\n1000001\n2147483648\n-1\n1000000\n"
                + "1\nOne below max\nUpper boundary\n999999\n3\n0\n";
        String out = runInstructorMenu(cli);
        System.out.println("CLI input: 1 / title / description / 1000001 / 2147483648 / -1 / 1000000,"
                + " then 1 / title / description / 999999, then 3 / 0");
        System.out.println("---- CLI transcript ----");
        System.out.println(out);
        System.out.println("---- end transcript ----");
        int reprompts = count(out, "Please enter a number from 1 to 1000000.");
        check("CLI re-prompts for 1000001, 2147483648 (int overflow) and -1: 3 re-prompts, observed "
                + reprompts, reprompts == 3);
        check("CLI accepts 1000000 (upper boundary) and creates C1", out.contains("Draft created: C1"));
        check("CLI accepts 999999 (just below upper boundary) and creates C2",
                out.contains("Draft created: C2"));
        check("My courses lists C1 with capacity 1000000", out.contains("C1 | Max course | DRAFT | 1000000"));
        check("Program did not crash (menu shown again after the last action)",
                out.trim().endsWith("Choice:"));

        CourseService service = new CourseService(new InMemoryCourseRepository());
        Course big = service.createCourse(OWNER, "Service limit", "Direct service call", 1_000_001);
        check("OBSERVATION: service accepts 1000001 directly (upper limit is enforced in the view only)",
                big.getCapacity() == 1_000_001);
        expect("Service still rejects -1", IllegalArgumentException.class,
                "Capacity must be positive.",
                () -> service.createCourse(OWNER, "T", "D", -1));
    }

    private static String runInstructorMenu(String input) {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
            InputHelper helper = new InputHelper(new Scanner(System.in));
            CourseController controller = new CourseController(new CourseView(helper),
                    new CourseService(new InMemoryCourseRepository()));
            try {
                controller.runInstructorMenu(OWNER);
            } catch (IllegalStateException endOfInput) {
                // Input deliberately ends after "0"; nothing else to do.
            }
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    private static int count(String text, String part) {
        int n = 0;
        int i = text.indexOf(part);
        while (i >= 0) {
            n++;
            i = text.indexOf(part, i + part.length());
        }
        return n;
    }

    // ---------- ENROL-05: decision table for EnrolmentService.enrol() ----------

    private static void enrol05() {
        System.out.println("TECHNIQUE: decision table (cause-effect), one fresh fixture per rule");
        System.out.println("Conditions: C1 course exists | C2 published | C3 already enrolled | C4 seat free");

        Fixture r1 = new Fixture();
        expect("R1  C1=N            -> Unknown course ID", IllegalArgumentException.class,
                "Unknown course ID: C99", () -> r1.enrolments.enrol(L01, "C99"));

        Fixture r1b = new Fixture();
        expect("R1b blank course ID -> Course ID is required.", IllegalArgumentException.class,
                "Course ID is required.", () -> r1b.enrolments.enrol(L01, "   "));

        Fixture r2 = new Fixture();
        Course draft = r2.courses.createCourse(OWNER, "Draft", "Not yet published", 5);
        expect("R2  C1=Y C2=N       -> not published", IllegalStateException.class,
                "This course is not published.", () -> r2.enrolments.enrol(L01, draft.getCourseId()));

        Fixture r3 = new Fixture();
        Course roomy = r3.published(2);
        r3.enrolments.enrol(L01, roomy.getCourseId());
        expect("R3  C1=Y C2=Y C3=Y C4=Y -> already enrolled (seat still free)",
                IllegalStateException.class, "You are already enrolled in this course.",
                () -> r3.enrolments.enrol(L01, roomy.getCourseId()));
        check("R3  duplicate did not use a second seat (count stays 1)",
                r3.enrolRepo.countByCourse(roomy.getCourseId()) == 1);

        Fixture r4 = new Fixture();
        Course full = r4.published(1);
        r4.enrolments.enrol(L01, full.getCourseId());
        expect("R4  C1=Y C2=Y C3=Y C4=N -> already enrolled (duplicate checked before capacity)",
                IllegalStateException.class, "You are already enrolled in this course.",
                () -> r4.enrolments.enrol(L01, full.getCourseId()));

        Fixture r5 = new Fixture();
        Course one = r5.published(1);
        r5.enrolments.enrol(L01, one.getCourseId());
        expect("R5  C1=Y C2=Y C3=N C4=N -> course full", IllegalStateException.class,
                "This course is full.", () -> r5.enrolments.enrol(L02, one.getCourseId()));
        check("R5  rejected learner was not saved", !r5.enrolRepo.exists("L02", one.getCourseId()));

        Fixture r6 = new Fixture();
        Course open = r6.published(1);
        Enrolment saved = r6.enrolments.enrol(L01, " " + open.getCourseId() + " ");
        check("R6  C1=Y C2=Y C3=N C4=Y -> enrolled; padded ID ' C1 ' is trimmed and stored as C1",
                saved.getCourseId().equals(open.getCourseId()) && saved.getLearnerId().equals("L01"));
        check("R6  seat count is now 1 = capacity (last seat taken)",
                r6.enrolRepo.countByCourse(open.getCourseId()) == 1);

        Fixture r7 = new Fixture();
        Course any = r7.published(1);
        expect("R7  learner missing (null) -> Learner is required.", IllegalArgumentException.class,
                "Learner is required.", () -> r7.enrolments.enrol(null, any.getCourseId()));

        Fixture r8 = new Fixture();
        Course lower = r8.published(1);
        expect("R8  course ID in lower case ('c1') -> treated as unknown (IDs are case-sensitive)",
                IllegalArgumentException.class, "Unknown course ID: c1",
                () -> r8.enrolments.enrol(L01, lower.getCourseId().toLowerCase()));
    }

    // ---------- WB-01: basis paths of AuthService.login() ----------

    private static void wb01() {
        System.out.println("TECHNIQUE: basis path testing, V(G) = P + 1 = 6 + 1 = 7; E - N + 2 = 19 - 14 + 2 = 7");
        AuthService auth = new AuthService(new InMemoryUserRepository());
        auth.registerUser(new Learner("U1", "Path Learner", "p@example.edu", LearnerType.STUDENT),
                "pathuser", "Correct2026!");

        expect("P1 input guard true (blank username) -> required", IllegalArgumentException.class,
                "Username and password are required.", () -> auth.login("   ", "x"));
        expect("P2 username not found -> generic error", IllegalArgumentException.class,
                "Incorrect username or password.", () -> auth.login("nobody", "Whatever2026!"));
        expect("P4 wrong password, not yet locked -> generic error", IllegalArgumentException.class,
                "Incorrect username or password.", () -> auth.login("pathuser", "wrong-1"));
        User afterOneFailure = auth.login("pathuser", "Correct2026!");
        check("P6 correct password after 1 failure -> returns U1 and resets the counter",
                "U1".equals(afterOneFailure.getUserId()));
        expect("P4 failure 1 after reset", IllegalArgumentException.class,
                "Incorrect username or password.", () -> auth.login("pathuser", "wrong-1"));
        expect("P4 failure 2 after reset: still not locked, which proves the reset"
                        + " (without it this would be failure 3)", IllegalArgumentException.class,
                "Incorrect username or password.", () -> auth.login("pathuser", "wrong-2"));
        expect("P5 failure 3 locks the account -> locked message", IllegalStateException.class,
                "Account locked after 3 incorrect passwords.", () -> auth.login("pathuser", "wrong-3"));
        expect("P3 already locked, correct password -> locked message", IllegalStateException.class,
                "Account locked after 3 incorrect passwords.", () -> auth.login("pathuser", "Correct2026!"));

        AuthService fresh = new AuthService(new InMemoryUserRepository());
        fresh.registerUser(new Learner("U2", "Happy Path", "h@example.edu", LearnerType.STUDENT),
                "happy", "Correct2026!");
        User user = fresh.login("HAPPY", "Correct2026!");
        check("P6 correct credentials on a fresh account -> returns U2", "U2".equals(user.getUserId()));

        AuthService broken = new AuthService(new CredentialWithoutUserRepository());
        expect("P7 credential found but user record missing (test double) -> state error",
                IllegalStateException.class, "Registered user record is missing.",
                () -> broken.login("orphan", "Correct2026!"));

        System.out.println("Condition coverage of the input guard (4 conditions, each made true once):");
        expect("  C1 username == null", IllegalArgumentException.class,
                "Username and password are required.", () -> auth.login(null, "x"));
        expect("  C2 username blank", IllegalArgumentException.class,
                "Username and password are required.", () -> auth.login("", "x"));
        expect("  C3 password == null", IllegalArgumentException.class,
                "Username and password are required.", () -> auth.login("pathuser", null));
        expect("  C4 password empty", IllegalArgumentException.class,
                "Username and password are required.", () -> auth.login("pathuser", ""));
        System.out.println("  All four false: covered by P2 to P7 above.");
    }

    /**
     * Test double: holds a credential for "orphan" but no matching user profile.
     * This state cannot be reached through the real repository, which saves both together.
     */
    private static final class CredentialWithoutUserRepository implements UserRepository {
        private final Credential orphan;

        CredentialWithoutUserRepository() {
            byte[] salt = PasswordHasher.newSalt();
            orphan = new Credential("orphan", "U404", salt, PasswordHasher.hash("Correct2026!", salt));
        }

        @Override
        public void save(User user, Credential credential) {
            throw new UnsupportedOperationException("not used");
        }

        @Override
        public Optional<Credential> findCredentialByUsername(String username) {
            return "orphan".equals(username) ? Optional.of(orphan) : Optional.empty();
        }

        @Override
        public Optional<User> findUserById(String userId) {
            return Optional.empty();
        }

        @Override
        public List<User> findAllUsers() {
            return Collections.emptyList();
        }
    }

    // ---------- WB-02: basis paths of EnrolmentService.enrol() ----------

    private static void wb02() {
        System.out.println("TECHNIQUE: basis path testing, V(G) = P + 1 = 5 + 1 = 6; E - N + 2 = 16 - 12 + 2 = 6");
        Fixture f1 = new Fixture();
        Course c = f1.published(1);
        expect("E1 learner null", IllegalArgumentException.class, "Learner is required.",
                () -> f1.enrolments.enrol(null, c.getCourseId()));

        Fixture f2 = new Fixture();
        expect("E2 course not resolved (getCourse throws)", IllegalArgumentException.class,
                "Unknown course ID: C7", () -> f2.enrolments.enrol(L01, "C7"));

        Fixture f3 = new Fixture();
        Course d = f3.courses.createCourse(OWNER, "Draft", "Draft only", 3);
        expect("E3 course not published", IllegalStateException.class,
                "This course is not published.", () -> f3.enrolments.enrol(L01, d.getCourseId()));

        Fixture f4 = new Fixture();
        Course p4 = f4.published(3);
        f4.enrolments.enrol(L01, p4.getCourseId());
        expect("E4 duplicate enrolment", IllegalStateException.class,
                "You are already enrolled in this course.",
                () -> f4.enrolments.enrol(L01, p4.getCourseId()));

        Fixture f5 = new Fixture();
        Course p5 = f5.published(1);
        f5.enrolments.enrol(L01, p5.getCourseId());
        expect("E5 course full", IllegalStateException.class, "This course is full.",
                () -> f5.enrolments.enrol(L02, p5.getCourseId()));

        Fixture f6 = new Fixture();
        Course p6 = f6.published(2);
        Enrolment e = f6.enrolments.enrol(L02, p6.getCourseId());
        check("E6 success path returns and saves the enrolment",
                f6.enrolRepo.exists("L02", p6.getCourseId()) && "L02".equals(e.getLearnerId()));
        check("E6 enrolment date is recorded", e.getEnrolledOn() != null);
    }

    // ---------- PERF-01: single-user timing baseline ----------

    private static void perf01() {
        System.out.println("TECHNIQUE: single-user timing baseline in the cloud test environment.");
        System.out.println("Targets were set before measuring. This is not a load test of NFR-03/NFR-05.");
        System.out.println("Java runtime: " + System.getProperty("java.version") + " | CPUs: "
                + Runtime.getRuntime().availableProcessors());

        AuthService auth = new AuthService(new InMemoryUserRepository());
        long regStart = System.nanoTime();
        auth.registerUser(new Learner("P1", "Perf Learner", "perf@example.edu", LearnerType.STUDENT),
                "perfuser", "Perf2026!x");
        long regMs = (System.nanoTime() - regStart) / 1_000_000;

        auth.login("perfuser", "Perf2026!x"); // warm-up, not counted
        long[] times = new long[10];
        for (int i = 0; i < times.length; i++) {
            long t0 = System.nanoTime();
            auth.login("perfuser", "Perf2026!x");
            times[i] = (System.nanoTime() - t0) / 1_000_000;
        }
        long[] sorted = times.clone();
        Arrays.sort(sorted);
        long median = (sorted[4] + sorted[5]) / 2;
        System.out.println("Registration (one PBKDF2 hash): " + regMs + " ms");
        System.out.println("Login times over 10 runs (ms): " + Arrays.toString(times));
        System.out.println("Login median: " + median + " ms | max: " + sorted[9] + " ms");
        check("Login median under 3000 ms (indicative target taken from the NFR-03 3 s threshold)",
                median < 3000);
        check("Login is deliberately not instant: median at least 50 ms (600,000 PBKDF2 iterations)",
                median >= 50);

        Fixture f = new Fixture();
        Course big = f.published(1000);
        List<Learner> learners = new ArrayList<>();
        for (int i = 1; i <= 1001; i++) {
            learners.add(new Learner(String.format("B%04d", i), "Bulk " + i, "b" + i + "@example.edu",
                    LearnerType.STUDENT));
        }
        long t0 = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            f.enrolments.enrol(learners.get(i), big.getCourseId());
        }
        long bulkMs = (System.nanoTime() - t0) / 1_000_000;
        System.out.println("1,000 enrolments into one course: " + bulkMs + " ms");
        check("1,000 enrolments complete in under 3000 ms", bulkMs < 3000);
        check("Seat count equals capacity 1000", f.enrolRepo.countByCourse(big.getCourseId()) == 1000);
        expect("1,001st learner rejected at capacity 1000", IllegalStateException.class,
                "This course is full.", () -> f.enrolments.enrol(learners.get(1000), big.getCourseId()));

        CourseService many = new CourseService(new InMemoryCourseRepository());
        for (int i = 0; i < 1000; i++) {
            Course c = many.createCourse(OWNER, "Course " + i, "Bulk", 10);
            if (i % 2 == 0) {
                many.publishCourse(c.getCourseId(), OWNER);
            }
        }
        long l0 = System.nanoTime();
        int published = many.listPublished().size();
        long listMs = (System.nanoTime() - l0) / 1_000_000;
        System.out.println("listPublished() over 1,000 courses: " + listMs + " ms, returned " + published);
        check("listPublished() returns the 500 published courses in under 1000 ms",
                published == 500 && listMs < 1000);
        check("Draft courses are excluded at volume",
                many.listPublished().stream().allMatch(c -> c.getStatus() == CourseStatus.PUBLISHED));
    }
}
