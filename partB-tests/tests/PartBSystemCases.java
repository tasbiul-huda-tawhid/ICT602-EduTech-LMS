import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * ICT602 A3 Part B: black-box system cases written by Tawhid.
 * Cases: AUTH-01..06 and AUTH-08 (Kaium's designs, re-executed), INT-01..04, REG-02, REG-03,
 * USE-01, UAT-01.
 * Each case starts the real program (edutech.Main) as a separate process with fresh demo data,
 * types the listed input lines into it, captures everything it prints, and checks the expected
 * lines in order. Nothing in the program is changed or stubbed.
 * Usage: java PartBSystemCases CASE-ID SUT-CLASSPATH
 */
public final class PartBSystemCases {
    private static int checks;
    private static String sutClasspath;

    private PartBSystemCases() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java PartBSystemCases CASE-ID SUT-CLASSPATH");
            System.exit(2);
        }
        sutClasspath = args[1];
        System.out.println("CASE_ID: " + args[0]);
        System.out.println("SUT: java -cp <integrated build> edutech.Main, started fresh for each run");
        System.out.println("Input is piped, so typed text is not echoed; the input list is shown first.");
        switch (args[0]) {
            case "AUTH-01": auth01(); break;
            case "AUTH-02": auth02(); break;
            case "AUTH-03": auth03(); break;
            case "AUTH-04": auth04(); break;
            case "AUTH-05": auth05(); break;
            case "AUTH-06": auth06(); break;
            case "AUTH-08": auth08(); break;
            case "INT-01": int01(); break;
            case "INT-02": int02(); break;
            case "INT-03": int03(); break;
            case "INT-04": int04(); break;
            case "REG-02": reg02(); break;
            case "REG-03": reg03(); break;
            case "USE-01": use01(); break;
            case "UAT-01": uat01(); break;
            default:
                System.out.println("Unknown case " + args[0]);
                System.exit(2);
        }
        System.out.println("RESULT: PASS | checks=" + checks);
    }

    // ---------------- process driver and checks ----------------

    private static final class Run {
        final String output;
        final int exitCode;

        Run(String output, int exitCode) {
            this.output = output;
            this.exitCode = exitCode;
        }
    }

    private static Run run(String label, String... lines) throws IOException, InterruptedException {
        String javaBin = System.getProperty("java.home") + "/bin/java";
        ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", sutClasspath, "edutech.Main");
        Map<String, String> env = pb.environment();
        env.remove("JAVA_TOOL_OPTIONS"); // keeps JVM notices out of the transcript
        pb.redirectErrorStream(true);
        Process process = pb.start();
        StringBuilder typed = new StringBuilder();
        for (String line : lines) {
            typed.append(line).append('\n');
        }
        try (OutputStream in = process.getOutputStream()) {
            in.write(typed.toString().getBytes(StandardCharsets.UTF_8));
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream out = process.getInputStream()) {
            out.transferTo(buffer);
        }
        if (!process.waitFor(60, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new AssertionError(label + ": program did not finish within 60 s");
        }
        String output = buffer.toString(StandardCharsets.UTF_8);
        System.out.println();
        System.out.println("==== RUN: " + label + " ====");
        System.out.println("Input lines (in order): " + (lines.length == 0 ? "(none: input ends at once)"
                : String.join(" | ", lines)));
        System.out.println("---- program output ----");
        System.out.print(output);
        System.out.println("---- end output (exit code " + process.exitValue() + ") ----");
        return new Run(output, process.exitValue());
    }

    private static void check(String statement, boolean result) {
        checks++;
        System.out.println((result ? "PASS" : "FAIL") + " CHECK | " + statement);
        if (!result) {
            throw new AssertionError(statement);
        }
    }

    /** Every expected fragment must appear, in this order. */
    private static void inOrder(Run r, String... expected) {
        int from = 0;
        for (String e : expected) {
            int at = r.output.indexOf(e, from);
            check("output shows \"" + e + "\"" + (from > 0 ? " (after the previous line)" : ""), at >= 0);
            from = at + e.length();
        }
    }

    private static void cleanExit(Run r) {
        check("program ends normally with goodbye, exit code 0 and no stack trace",
                r.exitCode == 0 && r.output.contains("Goodbye from EduTech LMS.")
                        && !r.output.contains("Exception in thread") && !r.output.contains("\tat "));
    }

    private static int count(String text, String part) {
        int n = 0;
        for (int i = text.indexOf(part); i >= 0; i = text.indexOf(part, i + part.length())) {
            n++;
        }
        return n;
    }

    /** Output after the start-up banner, so the demo banner is not counted as a leak. */
    private static String afterBanner(Run r) {
        int i = r.output.indexOf("=== EduTech LMS ===");
        return i < 0 ? r.output : r.output.substring(i);
    }

    // ---------------- AUTH: Kaium's console cases, re-executed on the integrated build ----------------

    private static void auth01() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Cloud re-execution of his Windows run. FR-01, NFR-01");
        Run r = run("valid login for each role", "1", "alex", "Learner2026!", "0",
                "1", "priya", "Teach2026!", "0", "1", "sam", "Admin2026!", "3", "2");
        inOrder(r, "Welcome, Alex Chen (Learner).", "LEARNER |", "Welcome, Dr Priya Nair (Instructor).",
                "INSTRUCTOR |", "Welcome, Sam Taylor (Administrator).", "=== Administrator Menu ===");
        cleanExit(r);
    }

    private static void auth02() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Invalid equivalence classes. FR-01, NFR-01");
        Run r = run("known user with wrong password, then unknown user", "1", "alex", "WrongPass1",
                "1", "nobody", "Learner2026!", "2");
        check("both attempts give the same generic message (2 times)",
                count(r.output, "Login not completed: Incorrect username or password.") == 2);
        check("no role menu opens", !r.output.contains("Welcome,") && !r.output.contains("LEARNER |"));
        cleanExit(r);
    }

    private static void auth03() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Username case variants. FR-01");
        Run r = run("ALEX, Alex and aLeX with the correct password", "1", "ALEX", "Learner2026!", "0",
                "1", "Alex", "Learner2026!", "0", "1", "aLeX", "Learner2026!", "0", "2");
        check("all three variants log in as Alex Chen (Learner)",
                count(r.output, "Welcome, Alex Chen (Learner).") == 3);
        cleanExit(r);
    }

    private static void auth04() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Lockout boundary at the 3rd failure. NFR-01");
        Run r = run("three wrong passwords for maya", "1", "maya", "bad-1", "1", "maya", "bad-2",
                "1", "maya", "bad-3", "2");
        inOrder(r, "Login not completed: Incorrect username or password.",
                "Login not completed: Incorrect username or password.",
                "Login not completed: Account locked after 3 incorrect passwords.");
        check("exactly two generic failures before the lock",
                count(r.output, "Incorrect username or password.") == 2);
        cleanExit(r);
    }

    private static void auth05() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Successful login resets the counter. NFR-01");
        Run r = run("2 failures, success, logout, then 3 new failures", "1", "maya", "bad-1",
                "1", "maya", "bad-2", "1", "maya", "Learner2026!", "0",
                "1", "maya", "bad-3", "1", "maya", "bad-4", "1", "maya", "bad-5", "2");
        inOrder(r, "Incorrect username or password.", "Incorrect username or password.",
                "Welcome, Maya Singh (Learner).", "Maya Singh (Learner) has logged out.",
                "Incorrect username or password.", "Incorrect username or password.",
                "Account locked after 3 incorrect passwords.");
        check("the lock happens on the 3rd failure after the reset, not the 4th failure overall",
                count(r.output, "Incorrect username or password.") == 4);
        cleanExit(r);
    }

    private static void auth06() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Correct password after lockout. NFR-01");
        Run r = run("lock priya, then use the correct password", "1", "priya", "x-1", "1", "priya", "x-2",
                "1", "priya", "x-3", "1", "priya", "Teach2026!", "2");
        check("locked message shown twice (at lock and for the correct password)",
                count(r.output, "Login not completed: Account locked after 3 incorrect passwords.") == 2);
        check("no instructor menu opens", !r.output.contains("INSTRUCTOR |"));
        cleanExit(r);
    }

    private static void auth08() throws Exception {
        System.out.println("CASE OWNER: Kaium (design). Duplicate user ID and duplicate username. FR-13");
        Run r = run("administrator tries both duplicates, then lists accounts", "1", "sam", "Admin2026!",
                "1", "1", "U001", "Copy Of Alex", "copy@example.edu", "1", "dupid01", "Duplicate2026!",
                "1", "1", "U040", "Another Alex", "another@example.edu", "1", "alex", "Duplicate2026!",
                "2", "3", "2");
        inOrder(r, "Account action failed: That user ID is already registered.",
                "Account action failed: That username is already registered.",
                "Registered accounts (no password information is displayed):");
        check("U001 listed once and U040 not created",
                count(r.output, " - U001 |") == 1 && !r.output.contains(" - U040 |"));
        cleanExit(r);
    }

    // ---------------- INT: integration between controllers, services and repositories ----------------

    private static void int01() throws Exception {
        System.out.println("PURPOSE: AuthController -> Main.openMenuForRole -> role controller, for all three roles (FR-01, NFR-01)");
        Run r = run("one login per role, then exit",
                "1", "alex", "Learner2026!", "0",
                "1", "priya", "Teach2026!", "0",
                "1", "sam", "Admin2026!", "3",
                "2");
        inOrder(r,
                "Welcome, Alex Chen (Learner).", "LEARNER | 1 Published courses", "Alex Chen (Learner) has logged out.",
                "Welcome, Dr Priya Nair (Instructor).", "INSTRUCTOR | 1 Create", "Dr Priya Nair (Instructor) has logged out.",
                "Welcome, Sam Taylor (Administrator).", "=== Administrator Menu ===",
                "Sam Taylor (Administrator) has logged out.");
        check("each role menu appears exactly once (no user reaches another role's menu)",
                count(r.output, "LEARNER |") == 1 && count(r.output, "INSTRUCTOR |") == 1
                        && count(r.output, "=== Administrator Menu ===") == 1);
        cleanExit(r);
    }

    private static void int02() throws Exception {
        System.out.println("PURPOSE: AdminController -> AuthService -> InMemoryUserRepository -> AuthController (FR-13, FR-01)");
        Run r = run("administrator creates a learner and an instructor, both then log in",
                "1", "sam", "Admin2026!",
                "1", "1", "U010", "Jordan Lee", "jordan@example.edu", "1", "jordan", "Jordan2026!",
                "1", "2", "U011", "Ravi Kumar", "ravi@example.edu", "Data Science", "ravi", "Ravi2026!x",
                "2", "3",
                "1", "jordan", "Jordan2026!", "3", "0",
                "1", "RAVI", "Ravi2026!x", "3", "0",
                "2");
        inOrder(r,
                "Created Jordan Lee (Learner) successfully.",
                "Created Ravi Kumar (Instructor) successfully.",
                "Registered accounts (no password information is displayed):",
                " - U010 | Jordan Lee (Learner)", " - U011 | Ravi Kumar (Instructor)",
                "Welcome, Jordan Lee (Learner).", "LEARNER |", "No courses found.",
                "Welcome, Ravi Kumar (Instructor).", "INSTRUCTOR |", "No courses found.");
        check("the new passwords never appear in the output",
                !r.output.contains("Jordan2026!") && !r.output.contains("Ravi2026!x"));
        cleanExit(r);
    }

    private static void int03() throws Exception {
        System.out.println("PURPOSE: CourseController -> shared CourseService -> EnrolmentController (FR-02, FR-04)");
        Run r = run("learner blocked on draft, instructor publishes, learner then enrols",
                "1", "maya", "Learner2026!", "1", "2", "C2", "0",
                "1", "priya", "Teach2026!", "2", "C2", "3", "0",
                "1", "maya", "Learner2026!", "1", "2", "C2", "3", "0",
                "2");
        inOrder(r,
                "Welcome, Maya Singh (Learner).", "C1 | Introduction to Java | PUBLISHED | 1",
                "Unable to complete action: This course is not published.",
                "Welcome, Dr Priya Nair (Instructor).", "Course published successfully.",
                "C2 | Software Testing Basics | PUBLISHED | 30",
                "Welcome, Maya Singh (Learner).", "C1 | Introduction to Java | PUBLISHED | 1",
                "C2 | Software Testing Basics | PUBLISHED | 30",
                "Enrolled successfully.", "ID | Title | Status | Capacity",
                "C2 | Software Testing Basics | PUBLISHED | 30");
        int firstList = r.output.indexOf("C1 | Introduction to Java");
        int firstEnd = r.output.indexOf("LEARNER |", firstList);
        check("before publishing, the learner's published list does not contain C2",
                !r.output.substring(firstList, firstEnd).contains("C2 |"));
        cleanExit(r);
    }

    private static void int04() throws Exception {
        System.out.println("PURPOSE: enrolment state shared across sessions; capacity-1 demo course C1 (FR-04)");
        Run r = run("alex takes the only seat; maya is refused; alex cannot enrol twice",
                "1", "alex", "Learner2026!", "2", "C1", "3", "0",
                "1", "maya", "Learner2026!", "2", "C1", "3", "0",
                "1", "alex", "Learner2026!", "2", "C1", "0",
                "2");
        inOrder(r,
                "Welcome, Alex Chen (Learner).", "Enrolled successfully.",
                "C1 | Introduction to Java | PUBLISHED | 1", "Alex Chen (Learner) has logged out.",
                "Welcome, Maya Singh (Learner).", "Unable to complete action: This course is full.",
                "No courses found.", "Maya Singh (Learner) has logged out.",
                "Welcome, Alex Chen (Learner).",
                "Unable to complete action: You are already enrolled in this course.");
        cleanExit(r);
    }

    // ---------------- REG: regression and resilience ----------------

    private static void reg02() throws Exception {
        System.out.println("PURPOSE: invalid choices at every menu and prompt are refused without a crash");
        Run r = run("invalid input at login, learner, instructor and administrator menus",
                "3", "x", "", "1", "alex", "Learner2026!",
                "9", "abc", "2", "", "C9", "0",
                "1", "priya", "Teach2026!", "7", "1", "", "Quick course", "Short", "0", "1", "0",
                "1", "sam", "Admin2026!", "4", "1", "0", "1", "", "U020", "Short Name", "s@example.edu",
                "3", "1", "ab", "short", "3",
                "2");
        check("login menu: 3, x and blank each give the range message (3 times)",
                count(r.output, "Please enter a number from 1 to 2.") >= 3);
        check("learner and instructor menus: 9, abc and 7 give the invalid option message (3 times)",
                count(r.output, "Invalid menu option. Please choose 0, 1, 2 or 3.") == 3);
        check("empty text fields are re-asked (course ID, title, user ID: 3 times)",
                count(r.output, "This field cannot be empty. Please try again.") == 3);
        inOrder(r, "Unable to complete action: Unknown course ID: C9", "Draft created: C3",
                "Please enter a number from 1 to 3.", "Please enter a number from 1 to 3.",
                "Please enter a number from 1 to 2.",
                "Account action failed: Username must have at least 3 characters.");
        check("capacity 0 is refused before 1 is accepted",
                r.output.indexOf("Please enter a number from 1 to 1000000.") < r.output.indexOf("Draft created: C3"));
        cleanExit(r);
    }

    private static void reg03() throws Exception {
        System.out.println("PURPOSE: the program ends cleanly when input ends early (Ctrl+D or a short script)");
        String[][] scripts = {
            {},
            {"1", "alex"},
            {"1", "alex", "Learner2026!", "1"},
            {"1", "priya", "Teach2026!", "1", "Half typed"},
            {"1", "sam", "Admin2026!", "1", "1", "U030"},
        };
        String[] labels = {
            "a) input ends immediately",
            "b) input ends at the password prompt",
            "c) input ends inside the learner menu",
            "d) input ends while creating a course",
            "e) input ends while creating an account",
        };
        for (int i = 0; i < scripts.length; i++) {
            Run r = run(labels[i], scripts[i]);
            check(labels[i] + ": shows \"Input stream closed.\"", r.output.contains("Input stream closed."));
            cleanExit(r);
        }
    }

    // ---------------- USE: heuristic usability walkthrough ----------------

    private static void use01() throws Exception {
        System.out.println("PURPOSE: heuristic walkthrough of the console screens (Nielsen's heuristics),"
                + " evidence captured from one scripted session per role");
        Run r = run("learner, instructor and administrator tasks with typical slips",
                "1", "alex", "wrongpass",
                "1", "alex", "Learner2026!", "1", "2", "c1", "2", "C1", "3", "0",
                "1", "priya", "Teach2026!", "1", "UX course", "Heuristics", "0", "25", "2", "C3", "3", "0",
                "1", "sam", "Admin2026!", "2", "3",
                "2");
        System.out.println();
        System.out.println("HEURISTIC CHECKLIST (H = heuristic number in Nielsen's list)");
        check("H1 visibility of status: every action is confirmed (enrolled, draft created, published, logged out)",
                r.output.contains("Enrolled successfully.") && r.output.contains("Draft created: C3")
                        && r.output.contains("Course published successfully.") && r.output.contains("has logged out."));
        check("H2 match with the real world: menus name tasks in plain words (Published courses, Enrol, My courses)",
                r.output.contains("1 Published courses | 2 Enrol | 3 My courses"));
        check("H5 error prevention: an invalid capacity is refused and re-asked, never stored",
                r.output.contains("Please enter a number from 1 to 1000000.")
                        && r.output.contains("C3 | UX course | PUBLISHED | 25"));
        check("H9 recognise and recover from errors: rejections state the reason",
                r.output.contains("Unable to complete action: Unknown course ID: c1"));
        check("Security trade-off kept: login failure does not say which field was wrong",
                r.output.contains("Login not completed: Incorrect username or password."));
        System.out.println("OBSERVATION USE-OBS-01 (H4 consistency): learner and instructor menus use '0 Log out',"
                + " but the administrator menu uses '3. Log out' and the login menu '2. Back / exit'.");
        check("USE-OBS-01 evidence present", r.output.contains("0 Log out") && r.output.contains("3. Log out"));
        System.out.println("OBSERVATION USE-OBS-02 (H4/H9): usernames are case-insensitive but course IDs are not;"
                + " 'c1' is reported as unknown although C1 exists.");
        check("USE-OBS-02 evidence present", r.output.contains("Unknown course ID: c1"));
        System.out.println("OBSERVATION USE-OBS-03 (H5): the 'Capacity:' prompt does not show the allowed range"
                + " until a wrong value is typed.");
        check("USE-OBS-03 evidence present", r.output.contains("Capacity: Please enter a number from 1 to 1000000."));
        cleanExit(r);
    }

    // ---------------- UAT: acceptance walkthrough for each role ----------------

    private static void uat01() throws Exception {
        System.out.println("PURPOSE: business scenario 'open a new short course', checked against acceptance criteria");
        System.out.println("Executed by the group acting as each user role (proxy UAT), not by real end users.");
        Run r = run("administrator sets up users, instructor runs a course, learners enrol until full",
                "1", "sam", "Admin2026!",
                "1", "2", "U050", "Lena Park", "lena@example.edu", "Computing", "lena", "Lena2026!pw",
                "1", "1", "U051", "Omar Ali", "omar@example.edu", "1", "omar", "Omar2026!pw",
                "2", "3",
                "1", "lena", "Lena2026!pw", "1", "Agile Basics", "Scrum and Kanban", "2", "2", "C3", "3", "0",
                "1", "omar", "Omar2026!pw", "1", "2", "C3", "3", "0",
                "1", "maya", "Learner2026!", "2", "C3", "0",
                "1", "alex", "Learner2026!", "2", "C3", "0",
                "2");
        System.out.println();
        System.out.println("ACCEPTANCE CRITERIA (each criterion is met only if all its checks pass)");
        System.out.println("AC-1 (FR-13) administrator creates instructor and learner accounts and sees them listed");
        inOrder(r, "Created Lena Park (Instructor) successfully.", "Created Omar Ali (Learner) successfully.",
                " - U050 | Lena Park (Instructor)", " - U051 | Omar Ali (Learner)");
        System.out.println("AC-2 (FR-01) each new user logs in and gets the menu for their role");
        inOrder(r, "Welcome, Lena Park (Instructor).", "INSTRUCTOR |",
                "Welcome, Omar Ali (Learner).", "LEARNER |");
        System.out.println("AC-3 (FR-02) instructor creates a draft, publishes it and sees it as PUBLISHED");
        inOrder(r, "Draft created: C3", "Course published successfully.", "C3 | Agile Basics | PUBLISHED | 2");
        System.out.println("AC-4 (FR-04) learner sees the course, enrols, and sees it in My courses");
        int omar = r.output.indexOf("Welcome, Omar Ali (Learner).");
        String omarPart = r.output.substring(omar, r.output.indexOf("has logged out.", omar));
        check("Omar's session lists C3 twice (published list, then My courses) with enrolment between",
                count(omarPart, "C3 | Agile Basics | PUBLISHED | 2") == 2
                        && omarPart.indexOf("Enrolled successfully.") > omarPart.indexOf("C3 | Agile Basics"));
        System.out.println("AC-5 (FR-04) the second learner takes the last seat; the third is told the course is full");
        inOrder(r, "Welcome, Maya Singh (Learner).", "Enrolled successfully.",
                "Welcome, Alex Chen (Learner).", "Unable to complete action: This course is full.");
        System.out.println("AC-6 (NFR-01) no screen after the start-up banner shows a password");
        String visible = afterBanner(r);
        check("no password appears after the start-up banner",
                !visible.contains("Lena2026!pw") && !visible.contains("Omar2026!pw")
                        && !visible.contains("Learner2026!") && !visible.contains("Admin2026!"));
        cleanExit(r);
    }
}
